<template>
  <div :style="{ height: h + 'px'}">
    <el-container v-loading="loading" id="reportAnalysis" style="overflow: scroll">
      <el-container class="ms-row">
        <el-aside :width="!isHide ?'235px':'0px'" style="margin-left: 5px; max-height: 843px">
          <history-report-data report-type="TEST_CASE_ANALYSIS"
                               @selectReport="selectReport" @removeHistoryReportId="removeHistoryReportId"
                               ref="historyReport"/>
        </el-aside>
        <el-main class="ms-main">
          <div>
            <test-analysis-chart @hidePage="hidePage" @orderCharts="orderCharts" ref="analysisChart"
                                 :load-option="loadOption"/>
          </div>
          <div class="ms-row" v-if="!isHide">
            <test-analysis-table :tableData="tableData"/>
          </div>
        </el-main>
        <el-aside :width="!isHide ?'485px':'0px'">
          <test-analysis-filter @filterCharts="filterCharts" ref="analysisFilter"/>
        </el-aside>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import TestAnalysisChart from "./chart/TestAnalysisChart";
import TestAnalysisTable from "./table/TestAnalysisTable";
import TestAnalysisFilter from "./filter/TestAnalysisFilter";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {exportPdf} from "metersphere-frontend/src/utils";
import {getAnalysisReport} from "@/api/report";
import html2canvas from 'html2canvas';
import HistoryReportData from "../../base/HistoryReportData";
import {createHistoryReport, selectHistoryReportById, updateHistoryReport} from "@/api/history-report";

export default {
  name: "TestAnalysisContainer",
  components: {TestAnalysisChart, TestAnalysisTable, TestAnalysisFilter, HistoryReportData},
  data() {
    return {
      isHide: false,
      loading: false,
      options: {},
      loadOption: {
        legend: {},
        xAxis: {},
        yAxis: {},
        label: {},
        tooltip: {},
        series: []
      },
      tableData: [],
      h: document.documentElement.clientHeight - 40,
    }
  },
  methods: {
    handleExport() {
      let name = this.$t('commons.report_statistics.test_case_analysis');
      this.$nextTick(function () {
        setTimeout(() => {
          html2canvas(document.getElementById('reportAnalysis'), {
            scale: 2
          }).then(function (canvas) {
            exportPdf(name, [canvas]);
          });
        }, 1000);
      });
    },
    hidePage(isHide) {
      this.isHide = isHide;
    },
    close() {
      this.$emit('closePage');
    },
    init(opt) {
      this.loading = true;
      this.options = opt;
      getAnalysisReport(opt).then(response => {
        let data = response.data.chartDTO;
        let tableDTOs = response.data.tableDTOs;
        this.initPic(data, tableDTOs);
        this.loading = false;
      });
    },
    filterCharts(opt) {
      this.init(opt);
    },
    orderCharts(order) {
      this.options.order = order;
      this.filterCharts(this.options);
    },
    updateReport(reportId) {
      let obj = {};
      obj.id = reportId;
      obj.projectId = getCurrentProjectID();
      obj.selectOption = JSON.stringify(this.options);
      let dataOptionObj = {
        loadOption: this.loadOption,
        pieOption: this.pieOption,
        tableData: this.tableData,
      };
      obj.dataOption = JSON.stringify(dataOptionObj);
      obj.reportType = 'TEST_CASE_ANALYSIS';
      updateHistoryReport(obj).then(() => {
        this.$alert(this.$t('commons.save_success'));
        this.$refs.historyReport.initReportData();
      });
    },
    saveReport(reportName) {
      let obj = {};
      obj.name = reportName;
      obj.projectId = getCurrentProjectID();
      obj.selectOption = JSON.stringify(this.options);
      let dataOptionObj = {
        loadOption: this.loadOption,
        pieOption: this.pieOption,
        tableData: this.tableData,
      };
      obj.dataOption = JSON.stringify(dataOptionObj);
      obj.reportType = 'TEST_CASE_ANALYSIS';
      createHistoryReport(obj).then(() => {
        this.$alert(this.$t('commons.save_success'));
        this.$refs.historyReport.initReportData();
      });
    },
    initPic(loadOptionParam, tableData) {
      this.loading = true;
      if (loadOptionParam) {
        this.loadOption.legend = loadOptionParam.legend;
        this.loadOption.xAxis = loadOptionParam.xaxis;
        this.loadOption.series = loadOptionParam.series;
        this.loadOption.grid = {
          bottom: '75px',//距离下边距
        }
        this.loadOption.series.forEach(item => {
          item.type = this.$refs.analysisChart.chartType;
        })
      }
      if (tableData) {
        this.tableData = tableData;
      }
      this.loading = false;
    },
    selectReport(selectId) {
      let selectName = "";
      if (selectId) {
        this.loading = true;
        let paramObj = {
          id: selectId
        }
        selectHistoryReportById(paramObj).then(response => {
          let reportData = response.data;
          if (reportData) {
            selectName = reportData.name;
            if (reportData.dataOption) {
              let dataOptionObj = JSON.parse(reportData.dataOption);
              this.initPic(dataOptionObj.loadOption, dataOptionObj.pieOption, dataOptionObj.tableData);
            }
            if (reportData.selectOption) {
              let selectOptionObj = JSON.parse(reportData.selectOption);
              this.$refs.analysisFilter.initSelectOption(selectOptionObj);
            }
          }
          this.$emit('initHistoryReportId', selectId, selectName);
          this.loading = false;
        }).catch(() => {
          this.loading = false;
          this.removeHistoryReportId();
        });

      }
    },
    removeHistoryReportId() {
      this.$emit('initHistoryReportId', "", "");
    },
    selectAndSaveReport(reportName) {
      let opt = this.$refs.analysisFilter.getOption();
      this.options = opt;
      this.saveReport(reportName);
    },
    saveAndSaveAsReport(reportName, saveType) {
      if (saveType === 'save') {
        this.saveReport(reportName);
      } else if (saveType === 'saveAs') {
        this.selectAndSaveReport(reportName);
      }
    }
  },
}
</script>

<style scoped>
.ms-row {
  padding-top: 10px;
}

:deep(.el-main ) {
  padding: 0px 20px 0px;
}
</style>
