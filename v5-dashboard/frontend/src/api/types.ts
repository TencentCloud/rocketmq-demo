export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  success: boolean
}

export interface RegionInfo {
  regionId: string
  regionName: string
  available?: boolean
}

export interface ClusterInfo {
  clusterId: string
  clusterName: string
  description?: string
  region: string
  clusterType?: string
  status: string
  maxTps?: number
  topicCount?: number
  groupCount?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface TopicInfo {
  topicName: string
  clusterId: string
  topicType: string
  description?: string
  queueNum: number
  retentionHours?: number
  createTime: string
  updateTime?: string
}

export interface ProducerInfo {
  clientId: string
  clientAddress: string
  language: string
  version: string
}

export interface GroupInfo {
  groupName: string
  clusterId: string
  description?: string
  consumeEnable: boolean
  consumeType: string
  maxRetryTimes: number
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
  queueId?: number | null
  queueOffset?: number | null
  body?: string | null
  tags?: string | null
  keys?: string | null
  properties?: Record<string, string>
  bodySize?: number | null
  storeTimestamp?: number | null
  bornTimestamp?: number | null
  bornHost?: string | null
  storeHost?: string | null
  reconsumeTimes?: number | null
  storeTime?: string | null
  bornTime?: string | null
}

export interface MessageTraceInfo {
  traceType: string
  status: string
  timestamp?: number | null
  time?: string | null
  clientHost?: string | null
  groupName?: string | null
  messageId?: string | null
  keys?: string | null
  costTime?: number | null
  regionId?: string | null
  topicName?: string | null
  storeHost?: string | null
}

export interface RoleInfo {
  roleName: string
  description?: string
  accessKey?: string
  permissions?: string[]
  enabled?: boolean
  createTime?: string
  updateTime?: string
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
  topicType: string
  queueNum: number
  description?: string
  retentionHours?: number
}

export interface UpdateTopicRequest {
  queueNum?: number
  description?: string
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
  queryType: 'BY_ID' | 'BY_TIME' | 'RECENT'
  messageId?: string
  msgKey?: string
  tag?: string
  startTime?: number
  endTime?: number
  recentNum?: number
  offset?: number
  limit?: number
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
