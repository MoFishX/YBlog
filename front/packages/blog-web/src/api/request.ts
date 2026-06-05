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

instance.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const { config: originalRequest, response } = error

    if (response?.status === 401 && !(originalRequest as any)._retry && originalRequest.url !== '/auth/refresh') {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return instance(originalRequest)
        })
      }

      ;(originalRequest as any)._retry = true
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
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default instance
