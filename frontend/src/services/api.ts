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
  testConnection: (provider: string) => api.post<ApiResponse<boolean>>(`/configurations/${provider}/test`)
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