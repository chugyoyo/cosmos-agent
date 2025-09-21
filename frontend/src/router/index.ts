import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: () => import('@/views/Home.vue')
        },
        {
            path: '/settings',
            name: 'Settings',
            component: () => import('@/views/Settings.vue')
        },
        {
            path: '/configurations/getAllConfigurations',
            name: 'ConfigurationsList',
            component: () => import('@/views/Settings.vue'),
            props: true
        },
        {
            path: '/agent',
            name: 'Agent',
            component: () => import('@/views/Agent.vue')
        },
        {
            path: '/agents/agentId/:id',
            name: 'AgentDetail',
            component: () => import('@/views/Agent.vue'),
            props: true
        },
        {
            path: '/chat',
            name: 'Chat',
            component: () => import('@/views/Chat.vue')
        },
        {
            path: '/chat/agentId/:agentId/sessionId/:sessionId',
            name: 'ChatSession',
            component: () => import('@/views/Chat.vue'),
            props: true
        }
    ]
})

export default router