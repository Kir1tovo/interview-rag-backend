import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/jd',
    children: [
      {
        path: 'jd',
        name: 'JdList',
        component: () => import('@/views/jd/JdList.vue'),
        meta: { title: 'JD 列表' }
      },
      {
        path: 'jd/parse',
        name: 'JdParse',
        component: () => import('@/views/jd/JdParse.vue'),
        meta: { title: 'JD 解析' }
      },
      {
        path: 'jd/detail/:id',
        name: 'JdDetail',
        component: () => import('@/views/jd/JdDetail.vue'),
        meta: { title: 'JD 详情' }
      },
      {
        path: 'skill',
        name: 'SkillManage',
        component: () => import('@/views/skill/SkillManage.vue'),
        meta: { title: '技能管理' }
      },
      {
        path: 'match',
        name: 'MatchAnalysis',
        component: () => import('@/views/match/MatchAnalysis.vue'),
        meta: { title: '匹配分析' }
      },
      {
        path: 'match/history',
        name: 'MatchHistory',
        component: () => import('@/views/match/MatchHistory.vue'),
        meta: { title: '匹配历史' }
      },
      {
        path: 'plan',
        name: 'LearningPlan',
        component: () => import('@/views/plan/LearningPlan.vue'),
        meta: { title: '学习计划' }
      },
      {
        path: 'interview',
        name: 'InterviewSearch',
        component: () => import('@/views/interview/InterviewSearch.vue'),
        meta: { title: '面经检索' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'AI 面试准备平台'
  const token = getToken()
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else {
    next()
  }
})

export default router