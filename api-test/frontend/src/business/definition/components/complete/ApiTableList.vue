<template>
  <span>
    <slot name="header"></slot>
    <ms-search
      :condition.sync="condition"
      @search="search">
    </ms-search>

    <ms-table :data="tableData" :select-node-ids="selectNodeIds" :condition="condition" :page-size="pageSize"
              :total="total" enableSelection @selectCountChange="selectCountChange"
              :screenHeight="screenHeight"
              operator-width="170px"
              @order="initTable"
              @filter="search"
              ref="apitable">
      <ms-table-column
        prop="num"
        label="ID"
        min-width="80px"
        sortable>

      </ms-table-column>
      <ms-table-column
        prop="name"
        :label="$t('api_test.definition.api_name')"
        sortable
        width="120px"/>

      <ms-table-column
        prop="method"
        sortable="custom"
        column-key="method"
        :filters="methodFilters"
        :label="getApiRequestTypeName"
        width="120px">
        <template v-slot:default="scope">
          <el-tag size="mini"
                  :style="{'background-color': getColor(true, scope.row.method), border: getColor(true, scope.row.method)}"
                  class="api-el-tag">
            {{ scope.row.method }}
          </el-tag>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="userName"
        sortable="custom"
        :filters="userFilters"
        column-key="user_id"
        :label="$t('api_test.definition.api_principal')"
        width="100px"/>

      <ms-table-column
        prop="path"
        width="120px"
        :label="$t('api_test.definition.api_path')"/>

      <ms-table-column
        prop="tags"
        :label="$t('commons.tag')"
        width="120px">
        <template v-slot:default="scope">
          <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                  :show-tooltip="true" :content="itemName"
                  style="margin-left: 0px; margin-right: 2px"/>
        </template>
      </ms-table-column>

      <ms-table-column
        v-if="versionEnable"
        :label="$t('project.version.name')"
        :filters="versionFilters"
        min-width="100px"
        prop="versionId">
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </ms-table-column>

       <ms-table-column
         sortable="createTime"
         width="160px"
         :label="$t('commons.create_time')"
         prop="createTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

      <ms-table-column
        width="160"
        :label="$t('api_test.definition.api_last_time')"
        sortable="custom"
        prop="updateTime">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="caseTotal"
        width="80px"
        :label="$t('api_test.definition.api_case_number')"/>

    </ms-table>
    <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </span>

</template>

<script>

import {versionEnableByProjectId} from "@/api/xpack";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsBottomContainer from "../../../definition/components/BottomContainer";
import ShowMoreBtn from "@/business/commons/ShowMoreBtn";
import MsBatchEdit from "../../../definition/components/basis/BatchEdit";
import {API_METHOD_COLOUR} from "../../../definition/model/JsonData";
import PriorityTableItem from "@/business/commons/PriorityTableItem";
import MsEnvironmentSelect from "../../../definition/components/case/MsEnvironmentSelect";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import {getProtocolFilter} from "@/business/definition/api-definition";
import {getProjectMemberById} from "@/api/project";
import TableSelectCountBar from "@/business/automation/scenario/api/TableSelectCountBar";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";

export default {
  name: "ApiTableList",
  components: {
    TableSelectCountBar,
    MsEnvironmentSelect,
    PriorityTableItem,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    MsSearch
  },
  data() {
    return {
      moduleId: "",
      deletePath: "/test/case/delete",
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodColorMap: new Map(API_METHOD_COLOUR),
      methodFilters: [],
      userFilters: [],
      currentPage: 1,
      pageSize: 10,
      versionEnable: false,
    };
  },
  props: {
    currentProtocol: String,
    projectId: String,
    selectNodeIds: Array,
    tableData: Array,
    condition: Object,
    total: Number,
    versionFilters: Array,
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 400px)';
      }
    }
  },
  created: function () {
    this.getUserFilter();
    this.getProtocolFilter();
    this.checkVersionEnable();
  },
  watch: {
    currentProtocol() {
      this.getProtocolFilter();
    },
    projectId() {
      this.checkVersionEnable();
      this.getUserFilter();
    }
  },
  mounted() {
    if (this.$refs.apitable) {
      this.$emit('setSelectRow', this.$refs.apitable.getSelectRows());
    } else {
      this.$emit('setSelectRow', new Set());
    }

    if (this.$refs.apitable) {
      this.$refs.apitable.doLayout();
    }
  },
  computed: {
    getApiRequestTypeName() {
      if (this.currentProtocol === 'TCP') {
        return this.$t('api_test.definition.api_agreement');
      } else {
        return this.$t('api_test.definition.api_type');
      }
    },
  },
  methods: {
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    selectCountChange(value) {
      this.$emit('selectCountChange', value)
    },
    getColor(flag, method) {
      return this.methodColorMap.get(method);
    },
    getProtocolFilter() {
      this.methodFilters = getProtocolFilter(this.currentProtocol);
    },
    getSelectIds() {
      return this.$refs.apitable.selectIds;
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.currentPage = 1;
      this.initTable();
    },
    initTable() {
      this.$emit('refreshTable');
    },
    clear() {
      if (this.$refs.apitable) {
        this.$refs.apitable.clear();
      }
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(this.projectId).then(response => {
          this.versionEnable = response.data;
        });
      }
    },
    getUserFilter() {
      if (this.projectId) {
        getProjectMemberById(this.projectId).then((res) => {
          let data = res.data;
          this.userFilters = data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
      }
    }
  },
};
</script>

<style scoped>

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 200px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}

</style>
