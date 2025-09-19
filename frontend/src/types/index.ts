export interface AIConfiguration {
  id?: number
  provider: string
  apiKey: string
  baseUrl?: string
  model?: string
  isActive?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface Agent {
  id?: number
  name: string
  type: 'engineer' | 'tester' | 'sales'
  description?: string
  configuration?: string
  status: 'CREATED' | 'DEPLOYED' | 'STOPPED' | 'ERROR'
  callCount: number
  lastCalled?: string
  isDeployed: boolean
  createdAt?: string
  updatedAt?: string
}