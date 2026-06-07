import { dashboardApi } from '@/api/admin/modules/dashboard'
import type { DashboardStats, TrendItem } from '@/api/admin/modules/dashboard'

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const res = await dashboardApi.getStats()
    return res.data
  },

  async getWeeklyTrend(): Promise<TrendItem[]> {
    const res = await dashboardApi.getWeeklyTrend()
    return res.data
  }
}
