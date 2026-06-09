import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/adminUser'
import router from '@/router'
import { setupMock } from '@/mock/mockData'

declare module 'axios' {
  interface InternalAxiosRequestConfig {
    _retry?: boolean
  }
}

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

setupMock(instance)

let isRefreshing = false
let pendingQueue: Array<{
  resolve: (token: string) => void
  reject: (err: unknown) => void
}> = []

function processQueue(error: unknown, token: string | null = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token!)
  })
  pendingQueue = []
}

instance.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  if (config.url?.includes('/auth/refresh')) return config
  const userStore = useUserStore()
  if (!userStore.token) return config
  const validToken = await userStore.getValidToken()
  if (validToken) {
    config.headers.Authorization = `Bearer ${validToken}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const { config, response } = error

    if (response?.status === 401 && config && !config._retry) {
      if (config.url?.includes('/auth/refresh')) return Promise.reject(error)
      const userStore = useUserStore()

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({
            resolve: (token: string) => {
              config.headers.Authorization = `Bearer ${token}`
              resolve(instance(config))
            },
            reject
          })
        })
      }

      isRefreshing = true
      config._retry = true

      try {
        const newToken = await userStore.refreshAccessToken()
        processQueue(null, newToken)
        config.headers.Authorization = `Bearer ${newToken}`
        return instance(config)
      } catch {
        processQueue(new Error('refresh failed'))
        userStore.logout()
        router.push('/admin/login')
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    } else if (response?.status && response.status !== 401) {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default instance
