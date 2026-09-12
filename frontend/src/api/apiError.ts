import axios from 'axios'
import type { ApiError } from './types'

export type ApiErrorDetail = ApiError['error']

const UNKNOWN_ERROR: ApiErrorDetail = {
  code: 'CLIENT_UNKNOWN',
  message: '요청 처리 중 오류가 발생했습니다.',
}

export function getApiError(error: unknown): ApiErrorDetail {
  if (!axios.isAxiosError<ApiError>(error)) {
    return UNKNOWN_ERROR
  }

  const payload = error.response?.data

  if (payload?.success === false && payload.error) {
    return payload.error
  }

  if (!error.response) {
    return {
      code: 'CLIENT_NETWORK',
      message: '서버에 연결할 수 없습니다.',
    }
  }

  return UNKNOWN_ERROR
}
