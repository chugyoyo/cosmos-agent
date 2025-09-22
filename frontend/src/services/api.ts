import axios from 'axios'
import type { AIConfiguration } from '@/types'
import type { ApiResponse } from '@/types/api'

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
  getByProvider: (provider: string) => api.get<ApiResponse<AIConfiguration>>('/configurations/getConfigurationByProvider', {
    params: { provider }
  }),
  create: (config: AIConfiguration) => api.post<ApiResponse<AIConfiguration>>('/configurations/createConfiguration', config),
  saveUpdateConfiguration: (config: AIConfiguration) => api.put<ApiResponse<AIConfiguration>>('/configurations/saveUpdateConfiguration', config),
  deleteConfiguration: (id: number) => api.delete<ApiResponse<void>>('/configurations/deleteConfiguration', {
    params: { id }
  }),
  testConnection: (provider: string) => api.get<ApiResponse<any>>('/configurations/testConnectionByProvider', {
    params: { provider }
  })
}

export const agentApi = {
  getAll: () => api.get<ApiResponse<any[]>>('/agents/getAllAgents'),
  getById: (id: number) => api.get<ApiResponse<any>>('/agents/getAgentById', {
    params: { id }
  }),
  create: (agent: any) => api.post<ApiResponse<any>>('/agents/createAgent', agent),
  saveUpdateAgent: (agent: any) => api.put<ApiResponse<any>>('/agents/saveUpdateAgent', agent),
  deleteAgent: (id: number) => api.delete<ApiResponse<void>>('/agents/deleteAgent', {
    params: { id }
  }),
  getNodes: (agentId: number) => api.get<ApiResponse<any[]>>('/agents/getNodesByAgentId', {
    params: { "id" : agentId }
  }),
  saveUpdateNode: (node: any) => api.post<ApiResponse<any>>(`/agents/agentId/${node.agentId}/saveUpdateNode`, node),
  deleteNode: (nodeId: number) => api.delete<ApiResponse<void>>('/agents/deleteNode', {
    params: { nodeId }
  }),
  getLinks: (agentId: number) => api.get<ApiResponse<any[]>>('/agents/getLinksByAgentId', {
    params: { id: agentId }
  }),
  saveUpdateLink: (link: any) => api.post<ApiResponse<any>>('/agents/saveUpdateLink', link),
  deleteLink: (linkId: number) => api.delete<ApiResponse<void>>(`/agents/links/${linkId}`),
  getLinkById: (linkId: number) => api.get<ApiResponse<any>>(`/agents/links/${linkId}`),
  updateLink: (linkId: number, link: any) => api.put<ApiResponse<any>>(`/agents/links/${linkId}`, link),
  getLinksBySourceNode: (nodeId: number) => api.get<ApiResponse<any[]>>(`/agents/nodes/${nodeId}/sourceLinks`),
  getLinksByTargetNode: (nodeId: number) => api.get<ApiResponse<any[]>>(`/agents/nodes/${nodeId}/targetLinks`),
  getLinksByNode: (nodeId: number) => api.get<ApiResponse<any[]>>(`/agents/nodes/${nodeId}/allLinks`),
  checkLinkExists: (agentId: number, sourceNodeId: number, targetNodeId: number) => 
    api.get<ApiResponse<boolean>>('/agents/checkLinkExists', {
      params: { agentId, sourceNodeId, targetNodeId }
    }),
  executeWorkflow: (workflowData: any) => api.post<ApiResponse<any>>('/agents/executeWorkflow', workflowData),
  getAIConfigurations: () => api.get<ApiResponse<any[]>>('/configurations/getAllConfigurations')
}

export const chatApi = {
  getChatAgents: () => api.get<ApiResponse<any[]>>('/chat/getChatAgents'),
  getAgents: () => api.get<ApiResponse<any[]>>('/agents/getAllAgents'),
  getSessions: (agentId: number) => api.get<ApiResponse<any[]>>('/chat/getSessionsByAgentId', {
    params: { agentId }
  }),
  createSession: (session: any) => api.post<ApiResponse<any>>('/chat/createSession', session),
  getSessionMessages: (sessionId: number) => api.get<ApiResponse<any[]>>('/chat/getSessionMessages', {
    params: { sessionId }
  }),
  sendMessage: (request: any) => api.post<ApiResponse<any>>('/chat/sendMessage', request),
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
  getDefaultSession: (agentId: number) => api.get<ApiResponse<any>>('/chat/getDefaultSession', {
    params: { agentId }
  }),
  deleteSession: (sessionId: number) => api.delete<ApiResponse<void>>('/chat/deleteSession', {
    params: { sessionId }
  })
}