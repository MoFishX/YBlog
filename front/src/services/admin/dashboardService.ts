import { dashboardApi } from '@/api/admin/modules/dashboard'
import type { DashboardStats, TrendItem } from '@/api/admin/modules/dashboard'

const CACHE_TTL = 5 * 60 * 1000
let statsCache: { data: DashboardStats; timestamp: number } | null = null

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    if (statsCache && Date.now() - statsCache.timestamp < CACHE_TTL) {
      return statsCache.data
    }
    const res = await dashboardApi.getStats()
    statsCache = { data: res.data, timestamp: Date.now() }
    return res.data
  },

  async getWeeklyTrend(): Promise<TrendItem[]> {
    const res = await dashboardApi.getWeeklyTrend()
    return res.data
  }
}
