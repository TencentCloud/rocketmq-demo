import request from './request'
import type {
  ApiResponse,
  TopicInfo,
  ProducerInfo,
  CreateTopicRequest,
  UpdateTopicRequest
} from './types'

export const topicApi = {
  listTopics(clusterId: string, topicName?: string): Promise<ApiResponse<TopicInfo[]>> {
    return request.get('/v1/topics', { params: { clusterId, topicName } }).then(res => res.data)
  },

  getTopic(name: string, clusterId: string): Promise<ApiResponse<TopicInfo>> {
    return request.get(`/v1/topics/${name}`, { params: { clusterId } }).then(res => res.data)
  },

  createTopic(data: CreateTopicRequest): Promise<ApiResponse<TopicInfo>> {
    return request.post('/v1/topics', data).then(res => res.data)
  },

  updateTopic(name: string, data: UpdateTopicRequest): Promise<ApiResponse<TopicInfo>> {
    return request.put(`/v1/topics/${name}`, data).then(res => res.data)
  },

  deleteTopic(name: string, clusterId: string): Promise<ApiResponse<void>> {
    return request.delete(`/v1/topics/${name}`, { params: { clusterId } }).then(res => res.data)
  },

  listProducers(name: string, clusterId: string): Promise<ApiResponse<ProducerInfo[]>> {
    return request
      .get(`/v1/topics/${name}/producers`, { params: { clusterId } })
      .then(res => res.data)
  }
}
