import axios from 'axios'
import type { AIConfiguration, Agent } from '@/types'
import type { ApiResponse } from '@/types/api'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
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
  getAll: () => api.get<ApiResponse<AIConfiguration[]>>('/configurations'),
  getByProvider: (provider: string) => api.get<ApiResponse<AIConfiguration>>(`/configurations/${provider}`),
  create: (config: AIConfiguration) => api.post<ApiResponse<AIConfiguration>>('/configurations', config),
  update: (id: number, config: AIConfiguration) => api.put<ApiResponse<AIConfiguration>>(`/configurations/${id}`, config),
  delete: (id: number) => api.delete<ApiResponse<void>>(`/configurations/${id}`),
  testConnection: (provider: string) => api.post<ApiResponse<any>>(`/configurations/${provider}/test`)
}

export const aiTestApi = {
  testConnection: (provider: string) => api.post<ApiResponse<any>>(`/ai-test/${provider}/connection`),
  testChat: (provider: string, message: string) => api.post<ApiResponse<any>>(`/ai-test/${provider}/chat`, { message }),
}

export const agentApi = {
  getAll: () => api.get<ApiResponse<Agent[]>>('/agents'),
  getByType: (type: string) => api.get<ApiResponse<Agent[]>>(`/agents/type/${type}`),
  getDeployed: () => api.get<ApiResponse<Agent[]>>('/agents/deployed'),
  getById: (id: number) => api.get<ApiResponse<Agent>>(`/agents/${id}`),
  create: (agent: Agent) => api.post<ApiResponse<Agent>>('/agents', agent),
  update: (id: number, agent: Agent) => api.put<ApiResponse<Agent>>(`/agents/${id}`, agent),
  delete: (id: number) => api.delete<ApiResponse<void>>(`/agents/${id}`),
  deploy: (id: number) => api.post<ApiResponse<Agent>>(`/agents/${id}/deploy`),
  undeploy: (id: number) => api.post<ApiResponse<Agent>>(`/agents/${id}/undeploy`),
  incrementCallCount: (id: number) => api.post<ApiResponse<Agent>>(`/agents/${id}/call`)
}

export const orchestrationApi = {
  getAll: () => api.get<ApiResponse<any[]>>('/orchestrations'),
  getById: (id: number) => api.get<ApiResponse<any>>(`/orchestrations/${id}`),
  create: (orchestration: any) => api.post<ApiResponse<any>>('/orchestrations', orchestration),
  update: (id: number, orchestration: any) => api.put<ApiResponse<any>>(`/orchestrations/${id}`, orchestration),
  delete: (id: number) => api.delete<ApiResponse<void>>(`/orchestrations/${id}`),
  updateFlow: (id: number, flowData: string) => api.put<ApiResponse<any>>(`/orchestrations/${id}/flow`, flowData),
  getNodes: (orchestrationId: number) => api.get<ApiResponse<any[]>>(`/orchestrations/${orchestrationId}/nodes`),
  createNode: (orchestrationId: number, node: any) => api.post<ApiResponse<any>>(`/orchestrations/${orchestrationId}/nodes`, node),
  updateNode: (id: number, node: any) => api.put<ApiResponse<any>>(`/nodes/${id}`, node),
  updateNodeYaml: (id: number, yamlConfig: string) => api.put<ApiResponse<any>>(`/nodes/${id}/yaml`, yamlConfig),
  deleteNode: (id: number) => api.delete<ApiResponse<void>>(`/nodes/${id}`)
}

export const chatApi = {
  getAgents: () => api.get<ApiResponse<any[]>>('/chat/agents'),
  getOrchestrationAgents: () => api.get<ApiResponse<any[]>>('/orchestrations'),
  getSessions: (agentId: number) => api.get<ApiResponse<any[]>>(`/chat/sessions/${agentId}`),
  createSession: (session: any) => api.post<ApiResponse<any>>('/chat/sessions', session),
  getSessionMessages: (sessionId: number) => api.get<ApiResponse<any[]>>(`/chat/sessions/${sessionId}/messages`),
  sendMessage: (request: any) => api.post<ApiResponse<any>>('/chat/send', request),
  sendMessageStream: (request: any) => {
    const token = localStorage.getItem('token')
    return fetch('/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(request)
    })
  },
  getDefaultSession: (agentId: number) => api.get<ApiResponse<any>>(`/chat/default-session/${agentId}`),
  deleteSession: (sessionId: number) => api.delete<ApiResponse<void>>(`/chat/sessions/${sessionId}`)
}