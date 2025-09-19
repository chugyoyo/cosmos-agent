import type { ApiResponse } from '@/types/api'

export function extractData<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data
}

export function handleApiError(error: any): string {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  if (error.message) {
    return error.message
  }
  return 'Unknown error occurred'
}