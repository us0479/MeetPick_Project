import axios from 'axios'

type HealthResponse = {
  status: string
}

export async function getBackendHealth(): Promise<HealthResponse> {
  const response = await axios.get<HealthResponse>('/actuator/health')
  return response.data
}
