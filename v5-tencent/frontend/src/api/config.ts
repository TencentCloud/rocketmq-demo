import request from './request'
import type { ApiResponse, CredentialsRequest, RegionInfo } from './types'

export const configApi = {
  saveCredentials(data: CredentialsRequest): Promise<ApiResponse<void>> {
    return request.post('/v1/config/credentials', data).then(res => res.data)
  },

  getCredentials(): Promise<ApiResponse<CredentialsRequest>> {
    return request.get('/v1/config/credentials').then(res => res.data)
  },

  listRegions(): Promise<ApiResponse<RegionInfo[]>> {
    return request.get('/v1/config/regions').then(res => res.data)
  },

  testConnection(data: CredentialsRequest): Promise<ApiResponse<boolean>> {
    return request.post('/v1/config/test', data).then(res => res.data)
  }
}
