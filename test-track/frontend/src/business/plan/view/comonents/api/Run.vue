<template>
  <span></span>
</template>
<script>
// import {baseSocket} from "@/api/base-network";
// import {strMapToObj} from "metersphere-frontend/src/utils";
// import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
// import ThreadGroup from "./jmeter/components/thread-group";
// import TestPlan from "./jmeter/components/test-plan";
// import {TYPE_TO_C} from "@/business/automation/scenario/Setting";
// import {apiDefinitionRunDebug} from "@/api/remote/api/api-definition";

export default {
  name: 'MsRun',
  components: {},
  props: {
    editCaseRequest: {
      type: Boolean,
      default: false
    },
    debug: Boolean,
    reportId: String,
    runData: Array,
    type: String,
    envMap: Map,
    isStop: Boolean,
  },
  data() {
    return {
      result: false,
      loading: false,
      requestResult: {responseResult: {}},
      reqNumber: 0,
      websocket: {}
    }
  },

  watch: {
    reportId() {
      // 开启链接
      this.socketSync();
    },
    isStop() {
      if (!this.isStop && this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
    }
  },
  methods: {
    socketSync() {
      this.websocket = baseSocket(this.reportId);
      this.websocket.onmessage = this.onMessages;
      this.websocket.onerror = this.onError;
    },
    onError() {
      this.$emit('runRefresh', "");
      this.$error(this.$t('api_test.automation.rerun_warning'));
    },
    onMessages(e) {
      // 确认连接建立成功，开始执行
      if (e && e.data === "CONN_SUCCEEDED") {
        this.run();
      }

      if (e.data && e.data.startsWith("result_")) {
        try {
          let data = e.data.substring(7);
          this.websocket.close();
          this.$emit('runRefresh', JSON.parse(data));
        } catch (e) {
          this.websocket.close();
          this.$emit('runRefresh', "");
        }
      } else if (e.data === "MS_TEST_END") {
        this.$emit('runRefresh', "");
      }
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}
            };
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    run() {
      let projectId = getCurrentProjectID();
      // 如果envMap不存在，是单接口调用
      if (!this.envMap || this.envMap.size === 0) {
        projectId = getCurrentProjectID();
      } else {
        // 场景步骤下接口调用
        if (this.runData.projectId) {
          projectId = this.runData.projectId;
        }
      }

      let testPlan = new TestPlan();
      testPlan.clazzName = TYPE_TO_C.get(testPlan.type);
      let threadGroup = new ThreadGroup();
      threadGroup.clazzName = TYPE_TO_C.get(threadGroup.type);
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      this.runData.forEach(item => {
        item.projectId = projectId;
        if (!item.clazzName) {
          item.clazzName = TYPE_TO_C.get(item.type);
        }
        threadGroup.hashTree.push(item);
      })
      this.sort(testPlan.hashTree);
      this.requestResult.reportId = this.reportId;
      let reqObj = {
        id: this.reportId,
        testElement: testPlan,
        type: this.type,
        clazzName: this.clazzName ? this.clazzName : TYPE_TO_C.get(this.type),
        projectId: projectId,
        environmentMap: strMapToObj(this.envMap)
      };
      let bodyFiles = this.getBodyUploadFiles(reqObj, this.runData);
      reqObj.editCaseRequest = this.editCaseRequest;
      reqObj.debug = this.debug;
      if (this.runData[0].url) {
        reqObj.name = this.runData[0].url;
      } else {
        reqObj.name = this.runData[0].path;
      }
      if (this.runData[0].useEnvironment) {
        reqObj.environmentId = this.runData[0].useEnvironment;
      }
      reqObj.reportId = this.reportId;
      if (!this.debug) {
        reqObj.syncResult = true;
      }
      apiDefinitionRunDebug(null, bodyFiles, reqObj).then(response => {
        this.requestResult = response.data;
        this.$emit('autoCheckStatus');  //   执行结束后，自动更新计划状态
      }, error => {
        this.$emit('errorRefresh', {});
      });
    },
    getBodyUploadFiles(obj, runData) {
      let bodyUploadFiles = [];
      obj.bodyUploadIds = [];
      if (runData) {
        if (runData instanceof Array) {
          runData.forEach(request => {
            obj.requestId = request.id;
            this._getBodyUploadFiles(request, bodyUploadFiles, obj);
          });
        } else {
          obj.requestId = runData.id;
          this._getBodyUploadFiles(runData, bodyUploadFiles, obj);
        }
      }
      return bodyUploadFiles;
    },
    _getBodyUploadFiles(request, bodyUploadFiles, obj) {
      let body = null;
      if (request.hashTree && request.hashTree.length > 0 && request.hashTree[0] && request.hashTree[0].body) {
        obj.requestId = request.hashTree[0].id;
        body = request.hashTree[0].body;
      } else if (request.body) {
        obj.requestId = request.id;
        body = request.body;
      }
      if (body) {
        if (body.kvs) {
          body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  item.name = item.file.name ? item.file.name : item.name;
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        if (body.binary) {
          body.binary.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  item.name = item.file.name ? item.file.name : item.name;
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
      }
    }
  }
}
</script>
