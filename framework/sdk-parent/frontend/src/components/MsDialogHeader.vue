<template>

  <div class="msDialogHeader">
    <span style="float: left;font-size: 18px;color: #303133;">{{ title }}</span>

    <div v-if="!hideButton" style="float: right;width: fit-content;">
      <div style="float: left; margin-right: 8px;">
        <slot name="other"></slot>
      </div>
      <div class="ms_btn">
        <el-button v-if="enableCancel" @click="cancel" :size="btnSize">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="confirm" @keydown.enter.native.prevent v-prevent-re-click :size="btnSize">
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </div>

  </div>

</template>

<script>
export default {
  name: "MsDialogHeader",
  props: {
    title: String,
    btnSize: {
      type: String,
      default() {
        return 'small';
      }
    },
    hideButton: Boolean,
    enableCancel: Boolean
  },
  methods: {
    cancel() {
      this.$emit("cancel");
    },
    confirm() {
      this.$emit("confirm");
    }
  }
}
</script>

<style scoped>
.ms_btn {
  float: right;
  margin-right: 50px;
}

.msDialogHeader {
  margin-bottom: 5px;
}
</style>
