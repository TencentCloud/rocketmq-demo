import request from './request'
import type { ApiResponse, RegionInfo } from './types'

export const configApi = {
  listRegions(): Promise<ApiResponse<RegionInfo[]>> {
    return request.get('/v1/config/regions').then(res => res.data)
  }
}
