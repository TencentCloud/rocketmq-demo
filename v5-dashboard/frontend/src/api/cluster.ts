import request from './request'
import type {
  ApiResponse,
  ClusterInfo,
  CreateClusterRequest,
  UpdateClusterRequest
} from './types'

export const clusterApi = {
  listClusters(): Promise<ApiResponse<ClusterInfo[]>> {
    return request.get('/v1/clusters').then(res => res.data)
  },

  getCluster(clusterId: string): Promise<ApiResponse<ClusterInfo>> {
    return request.get(`/v1/clusters/${clusterId}`).then(res => res.data)
  },

  createCluster(data: CreateClusterRequest): Promise<ApiResponse<ClusterInfo>> {
    return request.post('/v1/clusters', data).then(res => res.data)
  },

  updateCluster(
    clusterId: string,
    data: UpdateClusterRequest
  ): Promise<ApiResponse<ClusterInfo>> {
    return request.put(`/v1/clusters/${clusterId}`, data).then(res => res.data)
  },

  deleteCluster(clusterId: string): Promise<ApiResponse<void>> {
    return request.delete(`/v1/clusters/${clusterId}`).then(res => res.data)
  }
}
