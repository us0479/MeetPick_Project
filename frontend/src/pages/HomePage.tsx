import { useEffect, useState } from 'react'
import { getApiError } from '../api/apiError'
import { getBackendHealth } from '../api/healthApi'

export function HomePage() {
  const [status, setStatus] = useState('CHECKING')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    getBackendHealth()
      .then((data) => {
        setStatus(data.status)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        setStatus('DOWN')
        setErrorMessage(getApiError(error).message)
      })
  }, [])

  return (
    <section>
      <h1>MeetPick Backbone</h1>
      <p>Frontend → Spring Boot 연결 상태: <strong>{status}</strong></p>
      {errorMessage && <p>{errorMessage}</p>}
      <p>이 화면이 뜨고 상태가 UP이면 공통 프론트 백본과 smoke test가 완료된 것입니다.</p>
    </section>
  )
}
