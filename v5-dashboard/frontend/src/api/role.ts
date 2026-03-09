import request from './request'
import type { ApiResponse, RoleInfo, CreateRoleRequest, UpdateRoleRequest } from './types'

export const roleApi = {
  listRoles(clusterId: string): Promise<ApiResponse<RoleInfo[]>> {
    return request.get('/v1/roles', { params: { clusterId } }).then(res => res.data)
  },

  createRole(data: CreateRoleRequest): Promise<ApiResponse<RoleInfo>> {
    return request.post('/v1/roles', data).then(res => res.data)
  },

  updateRole(name: string, data: UpdateRoleRequest): Promise<ApiResponse<RoleInfo>> {
    return request.put(`/v1/roles/${name}`, data).then(res => res.data)
  },

  deleteRole(name: string, clusterId: string): Promise<ApiResponse<void>> {
    return request.delete(`/v1/roles/${name}`, { params: { clusterId } }).then(res => res.data)
  }
}
