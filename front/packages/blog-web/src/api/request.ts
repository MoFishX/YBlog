import axios, { type AxiosInstance } from 'axios'
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
