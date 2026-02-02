export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  success: boolean
}

export interface CredentialsRequest {
  secretId: string
  secretKey: string
  region: string
}

export interface RegionInfo {
  regionId: string
  regionName: string
  available?: boolean
}

export interface ClusterInfo {
  clusterId: string
  clusterName: string
  region: string
  status: string
  createTime: string
  remark?: string
}

export interface TopicInfo {
  topicName: string
  topicId: string
  messageType: string
  partitionNum: number
  remark?: string
  createTime: string
}

export interface ProducerInfo {
  clientId: string
  clientAddress: string
  language: string
  version: string
}

export interface GroupInfo {
  groupId: string
  groupName: string
  consumeEnable: boolean
  consumeType: string
  maxRetryTimes: number
  description?: string
  createTime: string
}

export interface ConsumerClientInfo {
  clientId: string
  clientAddress: string
  language: string
  version: string
  connectionTime: string
}

export interface ConsumerLagInfo {
  topicName: string
  partitionId: number
  lag: number
}

export interface MessageInfo {
  messageId: string
  topicName: string
  bornTime: string
  storeTime: string
  body: string
  properties: Record<string, string>
  tags?: string
  keys?: string
}

export interface MessageTraceInfo {
  traceId: string
  action: string
  timestamp: string
  status: string
  clientHost: string
}

export interface RoleInfo {
  roleName: string
  permissionType: string
  remark?: string
  createTime: string
}

export interface DashboardOverview {
  totalClusters: number
  totalTopics: number
  totalGroups: number
  totalMessages: number
}

export interface DashboardTrends {
  timestamp: string
  value: number
}

export interface TopLagGroup {
  groupName: string
  topicName: string
  lag: number
}

export interface CreateClusterRequest {
  clusterName: string
  region: string
  remark?: string
}

export interface UpdateClusterRequest {
  clusterName?: string
  remark?: string
}

export interface CreateTopicRequest {
  clusterId: string
  topicName: string
  messageType: string
  partitionNum: number
  remark?: string
}

export interface UpdateTopicRequest {
  partitionNum?: number
  remark?: string
}

export interface CreateGroupRequest {
  clusterId: string
  groupName: string
  consumeType: string
  maxRetryTimes?: number
  description?: string
}

export interface UpdateGroupRequest {
  consumeEnable?: boolean
  maxRetryTimes?: number
  description?: string
}

export interface SendMessageRequest {
  clusterId: string
  topicName: string
  body: string
  tags?: string
  keys?: string
  properties?: Record<string, string>
}

export interface QueryMessagesRequest {
  clusterId: string
  topicName: string
  startTime?: string
  endTime?: string
  messageId?: string
  keys?: string
  tags?: string
}

export interface ResetOffsetRequest {
  clusterId: string
  groupName: string
  topicName: string
  timestamp: string
}

export interface CreateRoleRequest {
  clusterId: string
  roleName: string
  permissionType: string
  remark?: string
}

export interface UpdateRoleRequest {
  permissionType?: string
  remark?: string
}
