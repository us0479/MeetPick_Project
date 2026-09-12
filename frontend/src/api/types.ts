// [COMMON FRONTEND API CONTRACT]
// Spring의 ApiResponse / ErrorResponse와 1:1로 맞춘 타입이다.
// 각 feature가 별도의 응답 envelope를 정의하지 않는다.
export type ApiSuccess<T> = {
  success: true
  data: T
}

export type ApiError = {
  success: false
  error: {
    code: string
    message: string
  }
}
