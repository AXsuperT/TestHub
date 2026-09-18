<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><DataAnalysis /></el-icon>
        <span>TestHub</span>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#b7c0cd" active-text-color="#409eff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="avatar">{{ realName.charAt(0) }}</el-avatar>
              <span style="margin-left:8px">{{ realName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, Folder, Document, Connection, VideoPlay, Warning, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const realName = ref(localStorage.getItem('testhub_real_name') || '用户')
const avatar = ref(localStorage.getItem('testhub_avatar') || '')

const menus = [
  { path: '/dashboard', title: '工作台', icon: 'DataAnalysis' },
  { path: '/projects', title: '项目管理', icon: 'Folder' },
  { path: '/test-cases', title: '测试用例', icon: 'Document' },
  { path: '/api-cases', title: '接口测试', icon: 'Connection' },
  { path: '/api-execute', title: '接口执行', icon: 'VideoPlay' },
  { path: '/bugs', title: '缺陷管理', icon: 'Bug' },
  { path: '/ai-assistant', title: 'AI助手', icon: 'MagicStick' }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => {
  const m = menus.find(i => i.path === route.path)
  return m ? m.title : 'TestHub'
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; overflow: hidden; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  gap: 8px;
}
.el-menu { border-right: none; }
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}
.breadcrumb { font-size: 18px; font-weight: 600; color: #303133; }
.user-info { display: flex; align-items: center; cursor: pointer; }
.main { background: #f0f2f5; padding: 0; overflow-y: auto; }
</style>
