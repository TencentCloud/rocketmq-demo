import request from './request'
import type {
  ApiResponse,
  MessageInfo,
  MessageTraceInfo,
  SendMessageRequest,
  QueryMessagesRequest
} from './types'

export const messageApi = {
  queryMessages(params: QueryMessagesRequest): Promise<ApiResponse<MessageInfo[]>> {
    return request.get('/v1/messages', { params }).then(res => res.data)
  },

  getMessage(id: string, clusterId: string, topicName: string): Promise<ApiResponse<MessageInfo>> {
    return request.get(`/v1/messages/${id}`, { params: { clusterId, topicName } }).then(res => res.data)
  },

  getMessageTrace(id: string, clusterId: string, topicName: string): Promise<ApiResponse<MessageTraceInfo[]>> {
    return request
      .get(`/v1/messages/${id}/trace`, { params: { clusterId, topicName } })
      .then(res => res.data)
  },

  sendMessage(data: SendMessageRequest): Promise<ApiResponse<string>> {
    return request.post('/v1/messages/send', data).then(res => res.data)
  },

  resendMessage(id: string, clusterId: string): Promise<ApiResponse<void>> {
    return request.post(`/v1/messages/${id}/resend`, { clusterId }).then(res => res.data)
  },

  verifyMessage(messageId: string, clusterId: string): Promise<ApiResponse<boolean>> {
    return request.post('/v1/messages/verify', { messageId, clusterId }).then(res => res.data)
  }
}
