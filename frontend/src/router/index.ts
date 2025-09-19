import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue')
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/Settings.vue')
    },
        {
      path: '/testing',
      name: 'Testing',
      component: () => import('@/views/Testing.vue')
    },
        {
      path: '/orchestration',
      name: 'Orchestration',
      component: () => import('@/views/Orchestration.vue')
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/views/Chat.vue')
    }
  ]
})

export default router