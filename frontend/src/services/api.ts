import axios from 'axios'
import type {AIConfiguration} from '@/types'
import type {ApiResponse} from '@/types/api'

const api = axios.create({
    baseURL: '/api',
    timeout: 60000
})

api.interceptors.response.use(
    response => {
        const apiResponse: ApiResponse = response.data
        if (apiResponse.code !== 200) {
            return Promise.reject(new Error(apiResponse.message || 'API Error'))
        }
        return response
    },
    error => {
        console.error('API Error:', error)
        return Promise.reject(error)
    }
)

export const configurationApi = {
    getAll: () => api.get<ApiResponse<AIConfiguration[]>>('/configurations/getAllConfigurations'),
    create: (config: AIConfiguration) => api.post<ApiResponse<AIConfiguration>>('/configurations/createConfiguration', config),
    deleteConfiguration: (id: number) => api.delete<ApiResponse<void>>('/configurations/deleteConfiguration', {
        params: {id}
    }),
    testConnection: (provider: string) => api.get<ApiResponse<any>>('/configurations/testConnectionByProvider', {
        params: {provider}
    })
}

export const agentApi = {
    getAll: () => api.get<ApiResponse<any[]>>('/agents/getAllAgents'),
    create: (agent: any) => api.post<ApiResponse<any>>('/agents/createAgent', agent),
    updateAgent: (id: number, agent: any) => api.put<ApiResponse<any>>(`/agents/${id}/updateAgent`, agent),
    deleteAgent: (id: number) => api.delete<ApiResponse<void>>('/agents/deleteAgent', {
        params: {id}
    }),
    getNodes: (agentId: number) => api.get<ApiResponse<any[]>>('/agents/getNodesByAgentId', {
        params: {"id": agentId}
    }),
    saveUpdateNode: (node: any) => api.post<ApiResponse<any>>(`/agents/agentId/${node.agentId}/saveUpdateNode`, node),
    deleteNode: (nodeId: number) => api.delete<ApiResponse<void>>('/agents/deleteNode', {
        params: {nodeId}
    }),
    getLinks: (agentId: number) => api.get<ApiResponse<any[]>>('/agents/getLinksByAgentId', {
        params: {id: agentId}
    }),
    saveUpdateLink: (link: any) => api.post<ApiResponse<any>>('/agents/saveUpdateLink', link),
    deleteLink: (linkId: number) => api.delete<ApiResponse<void>>(`/agents/links/${linkId}`),
    executeWorkflow: (workflowData: any) => api.post<ApiResponse<any>>('/agents/executeWorkflow', workflowData),
    getAIConfigurations: () => api.get<ApiResponse<any[]>>('/configurations/getAllConfigurations')
}

export const chatApi = {
    getAgents: () => api.get<ApiResponse<any[]>>('/agents/getAllAgents'),
    getSessions: (agentId: number) => api.get<ApiResponse<any[]>>('/chat/getSessionsByAgentId', {
        params: {agentId}
    }),
    createSession: (session: any) => api.post<ApiResponse<any>>('/chat/createSession', session),
    getSessionMessages: (sessionId: number) => api.get<ApiResponse<any[]>>('/chat/getSessionMessages', {
        params: {sessionId}
    }),
    sendMessageStream: (request: any) => {
        const token = localStorage.getItem('token')
        return fetch('/api/chat/sendMessageStream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: JSON.stringify(request)
        })
    },
    deleteSession: (sessionId: number) => api.delete<ApiResponse<void>>('/chat/deleteSession', {
        params: {sessionId}
    })
}