<template>
  <el-card class="ms-cards" v-if="request && request.responseResult">
    <div class="request-result">
      <div @click="active">
        <el-row :gutter="18" type="flex" align="middle" class="info">
          <el-col class="ms-req-name-col" :span="18" v-if="indexNumber!=undefined">
            <el-tooltip :content="getName(request.name)" placement="top">
              <div class="method ms-req-name">
                <div class="el-step__icon is-text ms-api-col-create">
                  <div class="el-step__icon-inner"> {{ indexNumber }}</div>
                </div>
                <i class="icon el-icon-arrow-right" :class="{'is-active': showActive}" @click="active" @click.stop/>
                <el-link class="report-label-req" @click="isLink" v-if="redirect && resourceId">
                  {{ request.name }}
                </el-link>
                <span v-else>{{ getName(request.name) }}</span>
              </div>
            </el-tooltip>
          </el-col>
          <!-- 误报 / 异常状态显示 -->
          <el-col :span="3">
            <el-tooltip effect="dark" v-if="baseErrorCode && baseErrorCode !==''" :content="baseErrorCode"
                        style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" placement="bottom"
                        :open-delay="800">
              <div :style="{color: statusColor(totalStatus ? totalStatus : request.status)}">
                {{ baseErrorCode }}
              </div>
            </el-tooltip>
          </el-col>
          <!-- 请求返回状态 -->
          <el-col :span="6">
            <el-tooltip effect="dark" :content="request.responseResult.responseCode"
                        style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" placement="bottom"
                        :open-delay="800">
              <div :style="{color: statusColor(totalStatus ? totalStatus : request.status)}">
                {{ request.responseResult.responseCode }}
              </div>
            </el-tooltip>
          </el-col>
          <!-- 请求响应时间 -->
          <el-col :span="3">
              <div :style="{color: statusColor(totalStatus ? totalStatus : request.status)}">
                {{ request.responseResult.responseTime }}
              </div>
          </el-col>
          <el-col :span="2">
            <el-tag v-if="request.testing" class="ms-test-running" size="mini">
              <i class="el-icon-loading" style="font-size: 16px"/>
              {{ $t('commons.testing') }}
            </el-tag>
            <ms-api-report-status :status="totalStatus || request.status" v-else/>
          </el-col>
        </el-row>
      </div>

      <el-collapse-transition>
        <div v-show="showActive && !request.unexecute" style="width: 99%">
          <ms-request-result-tail
            v-loading="requestInfo.loading"
            :scenario-name="scenarioName"
            :request-type="requestType"
            :request="requestInfo"
            :console="console"
            v-if="showActive"/>
        </div>
      </el-collapse-transition>
    </div>
  </el-card>
</template>

<script>
import MsRequestMetric from "./RequestMetric";
import MsAssertionResults from "./AssertionResults";
import MsRequestText from "./RequestText";
import MsResponseText from "./ResponseText";
import MsRequestResultTail from "./RequestResultTail";
import {getCurrentByResourceId} from "@/api/user";
import {getShareContent} from "@/api/share";
import {getScenarioReportStepDetail} from "@/api/scenario-report";
import MsApiReportStatus from "../ApiReportStatus";


const {getReportStatusColor} = require("@/business/commons/js/commons");
export default {
  name: "MsRequestResult",
  components: {
    MsApiReportStatus,
    MsResponseText,
    MsRequestText,
    MsAssertionResults,
    MsRequestMetric,
    MsRequestResultTail
  },
  props: {
    request: Object,
    resourceId: String,
    scenarioName: String,
    stepId: String,
    indexNumber: Number,
    console: String,
    totalStatus: String,
    redirect: Boolean,
    errorCode: {
      type: String,
      default: ""
    },
    isActive: {
      type: Boolean,
      default: false
    },
    isShare: Boolean,
    shareId: String,
  },
  created() {
    this.showActive = this.isActive;
    this.baseErrorCode = this.errorCode;
  },
  data() {
    return {
      requestType: "",
      color: {
        type: String,
        default() {
          return "#B8741A";
        }
      },
      requestInfo: {
        loading: true,
        hasData: false,
        responseResult: {},
        subRequestResults: [],
      },
      baseErrorCode: "",
      backgroundColor: {
        type: String,
        default() {
          return "#F9F1EA";
        }
      },
      showActive: false,
    }
  },
  watch: {
    isActive() {
      this.loadRequestInfoExpand();
      this.showActive = this.isActive;
    },
    errorCode() {
      this.baseErrorCode = this.errorCode;
    },
    request: {
      deep: true,
      handler(n) {
        if (this.request.errorCode) {
          this.baseErrorCode = this.request.errorCode;
        } else if (this.request.attachInfoMap && this.request.attachInfoMap.errorReportResult) {
          if (this.request.attachInfoMap.errorReportResult !== "") {
            this.baseErrorCode = this.request.attachInfoMap.errorReportResult;
          }
        }
      },
    }
  },
  methods: {
    statusColor(status) {
      return getReportStatusColor(status);
    },
    isLink() {
      let uri = "/#/api/definition?caseId=" + this.resourceId;
      this.clickResource(uri)
    },
    clickResource(uri) {
      getCurrentByResourceId(this.resourceId).then(() => {
        this.toPage(uri);
      });
    },
    toPage(uri) {
      let id = "new_a";
      let a = document.createElement("a");
      a.setAttribute("href", uri);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    },
    loadRequestInfoExpand() {
      if (!this.request.responseResult || this.request.responseResult.body === null || this.request.responseResult.body === undefined) {
        if (this.isShare) {
          getShareContent(this.shareId, this.stepId).then(response => {
            this.requestInfo = response.data;
            this.$nextTick(() => {
              this.requestInfo.loading = false;
            });
          });
        } else {
          getScenarioReportStepDetail(this.stepId).then(response => {
            this.requestInfo = response.data;
            this.$nextTick(() => {
              this.requestInfo.loading = false;
            });
          });
        }

      } else {
        this.requestInfo = this.request;
      }
    },
    active() {
      if (this.request.unexecute) {
        this.showActive = false;
      } else {
        this.showActive = !this.showActive;
      }
      if (this.showActive) {
        this.loadRequestInfoExpand();
      }
    },
    getName(name) {
      if (name && name.indexOf("<->") !== -1) {
        return name.split("<->")[0];
      }
      if (name && name.indexOf("^@~@^") !== -1) {
        let arr = name.split("^@~@^");
        let value = arr[arr.length - 1];
        if (value.indexOf("UUID=") !== -1) {
          return value.split("UUID=")[0];
        }
        if (value && value.startsWith("UUID=")) {
          return "";
        }
        if (value && value.indexOf("<->") !== -1) {
          return value.split("<->")[0];
        }
        return value;
      }
      if (name && name.startsWith("UUID=")) {
        return "";
      }
      return name;
    }
  },
}
</script>

<style scoped>
.request-result {
  min-height: 30px;
  padding: 2px 0;
}

.request-result .info {
  margin-left: 20px;
  cursor: pointer;
}

.request-result .method {
  color: #1E90FF;
  font-size: 14px;
  font-weight: 500;
  line-height: 35px;
  padding-left: 5px;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-result .url {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.request-result .tab .el-tabs__header {
  margin: 0;
}

.request-result .text {
  height: 300px;
  overflow-y: auto;
}

.sub-result .info {
  background-color: #FFF;
}

.sub-result .method {
  border-left: 5px solid #1E90FF;
  padding-left: 20px;
}

.ms-cards :deep(.el-card__body) {
  padding: 1px;
}

.sub-result:last-child {
  border-bottom: 1px solid #EBEEF5;
}

.ms-test-running {
  color: #783887;
}

.ms-test-error_code {
  color: #F6972A;
  background-color: #FDF5EA;
  border-color: #FDF5EA;
}

.ms-api-col {
  background-color: #EFF0F0;
  border-color: #EFF0F0;
  margin-right: 10px;
  font-size: 12px;
  color: #64666A;
}

.ms-api-col-create {
  background-color: #EBF2F2;
  border-color: #008080;
  margin-right: 10px;
  font-size: 12px;
  color: #008080;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 12px;
}

.el-divider--horizontal {
  margin: 2px 0;
  background: 0 0;
  border-top: 1px solid #e8eaec;
}

.icon.is-active {
  transform: rotate(90deg);
}

.ms-req-name {
  display: inline-block;
  margin: 0 5px;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 350px;
}

.ms-req-name-col {
  overflow-x: hidden;
}

.report-label-req {
  height: 20px;
  border-bottom: 1px solid #303133;
}
</style>
