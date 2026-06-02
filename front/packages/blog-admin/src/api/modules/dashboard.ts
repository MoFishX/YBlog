import request from '../request'
import type { ApiResponse } from '@shared/types/api'

export interface DashboardStats {
  userCount: number
  articleCount: number
  commentCount: number
  totalViews: number
  totalLikes: number
  todayViews: number
  todayNewUsers: number
  todayNewArticles: number
}

export interface TrendItem {
  date: string
  count: number
}

export const dashboardApi = {
  getStats(): Promise<ApiResponse<DashboardStats>> {
    return request.get('/admin/stats')
  },

  getWeeklyTrend(): Promise<ApiResponse<TrendItem[]>> {
    return request.get('/admin/stats/weekly-trend')
  }
}
