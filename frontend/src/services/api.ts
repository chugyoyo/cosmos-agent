import axios from 'axios'
import type { AIConfiguration, Agent } from '@/types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const configurationApi = {
  getAll: () => api.get<AIConfiguration[]>('/configurations'),
  getByProvider: (provider: string) => api.get<AIConfiguration>(`/configurations/${provider}`),
  create: (config: AIConfiguration) => api.post<AIConfiguration>('/configurations', config),
  update: (id: number, config: AIConfiguration) => api.put<AIConfiguration>(`/configurations/${id}`, config),
  delete: (id: number) => api.delete(`/configurations/${id}`),
  testConnection: (provider: string) => api.post<boolean>(`/configurations/${provider}/test`)
}

export const agentApi = {
  getAll: () => api.get<Agent[]>('/agents'),
  getByType: (type: string) => api.get<Agent[]>(`/agents/type/${type}`),
  getDeployed: () => api.get<Agent[]>('/agents/deployed'),
  getById: (id: number) => api.get<Agent>(`/agents/${id}`),
  create: (agent: Agent) => api.post<Agent>('/agents', agent),
  update: (id: number, agent: Agent) => api.put<Agent>(`/agents/${id}`, agent),
  delete: (id: number) => api.delete(`/agents/${id}`),
  deploy: (id: number) => api.post<Agent>(`/agents/${id}/deploy`),
  undeploy: (id: number) => api.post<Agent>(`/agents/${id}/undeploy`),
  incrementCallCount: (id: number) => api.post<Agent>(`/agents/${id}/call`)
}