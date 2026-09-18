import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'DataAnalysis' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/ProjectList.vue'),
        meta: { title: '项目管理', icon: 'Folder' }
      },
      {
        path: 'test-cases',
        name: 'TestCases',
        component: () => import('@/views/TestCaseList.vue'),
        meta: { title: '测试用例', icon: 'Document' }
      },
      {
        path: 'api-cases',
        name: 'ApiCases',
        component: () => import('@/views/ApiCaseList.vue'),
        meta: { title: '接口测试', icon: 'Connection' }
      },
      {
        path: 'api-execute',
        name: 'ApiExecute',
        component: () => import('@/views/ApiExecute.vue'),
        meta: { title: '接口执行', icon: 'VideoPlay' }
      },
      {
        path: 'bugs',
        name: 'Bugs',
        component: () => import('@/views/BugList.vue'),
        meta: { title: '缺陷管理', icon: 'Warning' }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('@/views/AiAssistant.vue'),
        meta: { title: 'AI助手', icon: 'MagicStick' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - TestHub` : 'TestHub'
  next()
})

export default router
