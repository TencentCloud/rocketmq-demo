import request from './request'
import type { ApiResponse, DashboardOverview, DashboardTrends, TopLagGroup } from './types'

export const dashboardApi = {
  getOverview(): Promise<ApiResponse<DashboardOverview>> {
    return request.get('/v1/dashboard/overview').then(res => res.data)
  },

  getTrends(timeRange: string): Promise<ApiResponse<DashboardTrends[]>> {
    return request.get('/v1/dashboard/trends', { params: { timeRange } }).then(res => res.data)
  },

  getTopLagGroups(limit: number): Promise<ApiResponse<TopLagGroup[]>> {
    return request.get('/v1/dashboard/top-lag', { params: { limit } }).then(res => res.data)
  }
}
