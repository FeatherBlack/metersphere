<template>
  <el-dialog class="user-import" :title="$t('commons.import_user')" :visible.sync="dialogVisible"
             @close="close">
    <el-row>
      <el-alert
        :title="$t('user.import_user_tip')"
        type="info"
        show-icon
        :closable="false"
      >
      </el-alert>
    </el-row>
    <el-row>
      <el-link type="primary" class="download-template"
               @click="downloadTemplate"
      >{{ $t('test_track.case.import.download_template') }}
      </el-link>
    </el-row>
    <el-row>
      <el-upload
        style="width: 100%"
        v-loading="loading"
        :element-loading-text="$t('test_track.case.import.importing')"
        element-loading-spinner="el-icon-loading"
        class="upload-demo"
        multiple
        drag
        :limit="1"
        action=""
        :on-exceed="handleExceed"
        :beforeUpload="uploadValidate"
        :on-error="handleError"
        :show-file-list="false"
        :http-request="upload"
        :file-list="fileList">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
        <div class="el-upload__tip" slot="tip">{{ $t('api_test.api_import.file_size_limit') }}</div>
      </el-upload>
    </el-row>
    <el-row>
      <ul>
        <li v-for="errFile in errList" :key="errFile.rowNum">
          {{ errFile.errMsg }}
        </li>
      </ul>
    </el-row>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {exportUserExample, userImport} from "../../../api/user";

export default {
  name: "UserImport",
  components: {ElUploadList, MsTableButton},
  data() {
    return {
      result: {},
      loading: false,
      activeName: 'excelImport',
      dialogVisible: false,
      fileList: [],
      errList: [],
      isLoading: false
    }
  },
  methods: {
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    uploadValidate(file) {
      let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
      if (suffix !== 'xls' && suffix !== 'xlsx') {
        this.$warning(this.$t('test_track.case.import.upload_limit_format'));
        return false;
      }
      if (file.size / 1024 / 1024 > 20) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'));
        return false;
      }
      this.isLoading = true;
      this.errList = [];
      return true;
    },
    handleError(err, file, fileList) {
      this.isLoading = false;
      this.$error(err.message);
    },
    open() {
      listenGoBack(this.close);
      this.projectId = getCurrentProjectID();
      this.dialogVisible = true;
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogVisible = false;
      this.fileList = [];
      this.errList = [];
    },
    downloadTemplate() {
      exportUserExample();
    },
    upload(file) {
      this.isLoading = false;
      this.fileList.push(file.file);
      this.loading = userImport(file.file, null, {}).then(response => {
        let res = response.data;
        if (res.success) {
          this.$success(this.$t('commons.import_success'));
          this.dialogVisible = false;
          this.$emit("refreshAll");
        } else {
          this.errList = res.errList;
        }
        this.fileList = [];
      }).catch(() => {
        this.fileList = [];
      })
    },
  }
}
</script>

<style scoped>

.download-template {
  padding-top: 10px;
  padding-bottom: 5px;
}

.user-import :deep(.el-dialog) {
  width: 400px;
}

.user-import :deep(.el-dialog__body) {
  padding: 5px 20px;
}

.user-import :deep(.el-alert__content) {
  padding: 0 0 0 8px

}
</style>
