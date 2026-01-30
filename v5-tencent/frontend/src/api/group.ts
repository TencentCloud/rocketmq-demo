import request from './request'
import type {
  ApiResponse,
  GroupInfo,
  ConsumerClientInfo,
  ConsumerLagInfo,
  CreateGroupRequest,
  UpdateGroupRequest,
  ResetOffsetRequest
} from './types'

export const groupApi = {
  listGroups(clusterId: string): Promise<ApiResponse<GroupInfo[]>> {
    return request.get('/v1/groups', { params: { clusterId } }).then(res => res.data)
  },

  getGroup(name: string, clusterId: string): Promise<ApiResponse<GroupInfo>> {
    return request.get(`/v1/groups/${name}`, { params: { clusterId } }).then(res => res.data)
  },

  createGroup(data: CreateGroupRequest): Promise<ApiResponse<GroupInfo>> {
    return request.post('/v1/groups', data).then(res => res.data)
  },

  updateGroup(name: string, data: UpdateGroupRequest): Promise<ApiResponse<GroupInfo>> {
    return request.put(`/v1/groups/${name}`, data).then(res => res.data)
  },

  deleteGroup(name: string, clusterId: string): Promise<ApiResponse<void>> {
    return request.delete(`/v1/groups/${name}`, { params: { clusterId } }).then(res => res.data)
  },

  listClients(name: string, clusterId: string): Promise<ApiResponse<ConsumerClientInfo[]>> {
    return request
      .get(`/v1/groups/${name}/clients`, { params: { clusterId } })
      .then(res => res.data)
  },

  resetOffset(name: string, data: ResetOffsetRequest): Promise<ApiResponse<void>> {
    return request.post(`/v1/groups/${name}/reset`, data).then(res => res.data)
  },

  getLag(name: string, clusterId: string): Promise<ApiResponse<ConsumerLagInfo[]>> {
    return request.get(`/v1/groups/${name}/lag`, { params: { clusterId } }).then(res => res.data)
  }
}
