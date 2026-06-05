import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { setupMock } from '@shared/mock/mockData'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

setupMock(instance)

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

let isRefreshing = false
let failedQueue: Array<{ resolve: Function; reject: Function }> = []

function processQueue(error: any, token: string | null = null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

async function handle401Refresh(originalRequest: any) {
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject })
    }).then((token) => {
      originalRequest.headers.Authorization = `Bearer ${token}`
      return instance(originalRequest)
    })
  }

  originalRequest._retry = true
  isRefreshing = true

  try {
    const userStore = useUserStore()
    const res: any = await instance.post('/auth/refresh')
    const { token, expiresIn } = res.data

    userStore.setAuth(token, expiresIn, userStore.user!)
    processQueue(null, token)

    originalRequest.headers.Authorization = `Bearer ${token}`
    return instance(originalRequest)
  } catch (refreshError) {
    processQueue(refreshError)
    const userStore = useUserStore()
    userStore.logout()
    router.push({ name: 'Login' })
    throw refreshError
  } finally {
    isRefreshing = false
  }
}

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const { response } = error

    if (response?.status === 401 && !(error.config as any)._retry && error.config?.url !== '/auth/refresh') {
      return handle401Refresh(error.config)
    }

    return Promise.reject(error)
  }
)

export default instance
