<template>
  <el-menu menu-trigger="click" :default-active="$route.path"
           :default-openeds="opends"
           router class="setting">
    <el-submenu index="1" v-permission="systemPermission">
      <template v-slot:title>
        <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
        <span>{{ $t('commons.system') }}</span>
      </template>
      <el-menu-item v-for="menu in systems" :key="menu.index" v-permission="menu.permissions"
                    :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>
    <el-submenu index="2" v-permission="workspacePermission">
      <template v-slot:title>
        <font-awesome-icon class="icon workspace" :icon="['far', 'list-alt']" size="lg"/>
        <span>{{ $t('commons.workspace') }}</span>
      </template>
      <el-menu-item v-for="menu in workspaces" v-permission="menu.permissions" :key="menu.index"
                    :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
import Setting from "@/router/modules/setting";
import {LicenseKey} from 'metersphere-frontend/src/utils/constants';
import {hasLicense} from 'metersphere-frontend/src/utils/permission';

const component = null;


export default {
  name: "MsSettingMenu",
  data() {
    let getMenus = function (group) {
      let menus = [];
      for (let child of Setting.children) {
        if (child.meta.xpack && !hasLicense()) {
          continue;
        }
        if (child.meta[group] === true) {
          let menu = {index: Setting.path + "/" + child.path};
          menu.title = child.meta.title;
          menu.roles = child.meta.roles;
          menu.permissions = child.meta.permissions;
          menu.valid = child.meta.valid;
          menus.push(menu);
        }
      }
      return menus;
    };
    return {
      opends: ['0','1','2'],
      systems: getMenus('system'),
      workspaces: getMenus('workspace'),
      project: getMenus('project'),
      workspaceTemplate: getMenus('workspaceTemplate'),
      systemPermission: [
        'SYSTEM_USER:READ', 'SYSTEM_ORGANIZATION:READ', 'SYSTEM_GROUP:READ',
        'ORGANIZATION_GROUP:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_TEST_POOL:READ',
        'SYSTEM_SETTING:READ', 'SYSTEM_QUOTA:READ', 'SYSTEM_AUTH:READ'
      ],
      workspacePermission: ['WORKSPACE_USER:READ', 'WORKSPACE_SERVICE:READ',
        'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ',
        'WORKSPACE_OPERATING_LOG:READ']
    };
  },
  methods: {
    valid() {
      Promise.all([component.default.valid(this)]).then(() => {
        let license = localStorage.getItem(LicenseKey);
        if (license !== "valid") {
          this.systems.forEach(item => {
            if (item.valid === true) {
              this.systems.splice(this.systems.indexOf(item), 1);
            }
          });
          this.workspaces.forEach(item => {
            if (item.valid === true) {
              this.workspaces.splice(this.workspaces.indexOf(item), 1);
            }
          });
        }
      });
    }
  },
  mounted() {
    if (component != null) {
      this.valid();
    }
  },
};
</script>

<style scoped>

.setting {
  border-right: 0;
}

.setting .setting-item {
  height: 40px;
  line-height: 40px;
}

.icon {
  width: 24px;
  margin-right: 10px;
}

.account {
  color: #5a78f0;
}

.lower_submenu_title :deep(.el-submenu__title) {
  height: 40px;
  line-height: 40px;
}

.workspace {
  color: #44b349;
}
</style>
