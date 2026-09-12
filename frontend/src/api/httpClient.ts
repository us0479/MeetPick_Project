import axios from 'axios'
import { tokenStore } from './tokenStore'

// [COMMON FRONTEND]
// 모든 A/B/C feature API는 이 Axios instance를 재사용한다.
// feature마다 axios.create()를 새로 만들면 baseURL/auth/error 정책이 갈라지므로 금지한다.
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

export const httpClient = axios.create({
  baseURL: apiBaseUrl,
  // A가 Refresh Token을 HttpOnly Cookie로 확정하는 경우에도 동작하도록 미리 true로 둔다.
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

httpClient.interceptors.request.use((config) => {
  // Access Token은 현재 in-memory skeleton이다.
  // A가 JWT 구현 후 로그인 성공 시 tokenStore에 넣고 Authorization header는 여기서 일괄 처리한다.
  const token = tokenStore.getAccessToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

// IMPORTANT(A): 401 재발급/Rotation 정책은 인증 ADR 확정 후 response interceptor에 구현한다.
// B/C는 여기서 임의의 refresh 로직을 추가하지 않는다.
