import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push({ name: 'Login' })
    }
    return Promise.reject(error)
  }
)

export default instance
