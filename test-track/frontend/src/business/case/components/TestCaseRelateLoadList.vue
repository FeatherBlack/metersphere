<template>
  <div>

    <el-input :placeholder="$t('commons.search_by_name_or_id')" @blur="initTable"
              @keyup.enter.native="initTable" class="search-input" size="small" v-model="condition.name"/>
    <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                             v-if="condition.components !== undefined && condition.components.length > 0"
                             @search="initTable"/>

    <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" margin-right="20"
                    class="search-input"/>

    <ms-table v-loading="result.loading" :data="tableData" :condition="condition" :page-size="pageSize"
              :total="total"
              :showSelectAll="false"
              :screenHeight="screenHeight"
              @selectCountChange="selectCountChange"
              @refresh="initTable"
              ref="table">

      <ms-table-column
        prop="num"
        label="ID"
        width="100px"
        sortable=true>
      </ms-table-column>

      <ms-table-column
        prop="name"
        :label="$t('commons.name')"/>

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
        prop="status"
        :label="$t('commons.status')"
        min-width="80">
        <template v-slot:default="{row}">
          <ms-performance-test-status :row="row"/>
        </template>
      </ms-table-column>
      <ms-table-column
        prop="updateTime"
        :label="$t('commons.update_time')"
        min-width="150px">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>
      <ms-table-column prop="createTime"
                       :label="$t('commons.create_time')"
                       min-width="150px">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

    </ms-table>
    <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </div>

</template>

<script>

import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
// import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import {TEST_CASE_RELEVANCE_LOAD_CASE} from "metersphere-frontend/src/components/search/search-components";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {getVersionFilters} from "@/api/project";
import {getTestCaseRelevanceLoadList} from "@/api/testCase";

export default {
  name: "TestCaseRelateLoadList",
  components: {
    // MsPerformanceTestStatus,
    MsTablePagination,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    'VersionSelect': MxVersionSelect,
  },
  data() {
    return {
      condition: {
        components: TEST_CASE_RELEVANCE_LOAD_CASE
      },
      result: {},
      screenHeight: '100vh - 400px',//屏幕高度
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      versionFilters: [],
    }
  },
  props: {
    projectId: String,
    versionEnable: Boolean,
    notInIds: {
      type: Array,
      default: null
    }
  },
  created: function () {
    this.initTable();
    this.getVersionOptions();
  },
  watch: {
    projectId() {
      this.condition.versionId = null;
      this.getVersionOptions();
      this.initTable();
    }
  },
  computed: {
    selectRows() {
      if (this.$refs.table) {
        return this.$refs.table.getSelectRows();
      } else {
        return new Set();
      }
    }
  },
  methods: {
    selectCountChange(data) {
      this.$emit("selectCountChange", data);
    },
    initTable(projectId) {
      this.condition.status = "";
      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }
      this.condition.notInIds = this.notInIds;
      getTestCaseRelevanceLoadList(this.currentPage, this.pageSize, this.condition)
        .then(response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    buildPagePath(path) {
      return path + this.currentPage + "/" + this.pageSize;
    },
    getSelectIds() {
      return this.$refs.table.selectIds;
    },
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
      }
    },
    getVersionOptions() {
      getVersionFilters(this.projectId)
        .then((data) => {
          this.versionFilters = data;
        });
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.initTable();
    }
  },
}
</script>

<style scoped>
.search-input {
  float: right;
  width: 300px;
  margin-right: 20px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}
</style>
