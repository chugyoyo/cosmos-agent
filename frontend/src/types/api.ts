export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export class ApiWrapper {
  static success<T>(data: T): ApiResponse<T> {
    return {
      code: 200,
      message: 'Success',
      data
    }
  }

  static error<T = any>(message: string, code: number = 500): ApiResponse<T> {
    return {
      code,
      message,
      data: null as T
    }
  }

  static notFound<T = any>(message: string = 'Not found'): ApiResponse<T> {
    return {
      code: 404,
      message,
      data: null as T
    }
  }

  static badRequest<T = any>(message: string = 'Bad request'): ApiResponse<T> {
    return {
      code: 400,
      message,
      data: null as T
    }
  }
}

export function isApiSuccess<T>(response: ApiResponse<T>): response is ApiResponse<T> & { data: NonNullable<T> } {
  return response.code === 200 && response.data !== null
}

export function isApiError<T>(response: ApiResponse<T>): boolean {
  return response.code !== 200
}

export function getApiErrorMessage<T>(response: ApiResponse<T>): string {
  return response.message || 'Unknown error'
}

export function handleApiResponse<T>(response: ApiResponse<T>): T {
  if (isApiSuccess(response)) {
    return response.data
  }
  throw new Error(getApiErrorMessage(response))
}