<template>
  <div class="messages-page">
    <PageHeader :title="t('message.pageTitle')" :description="t('message.pageDescription')" />

    <t-card class="filter-card">
      <t-form ref="queryFormRef" :data="queryForm" :rules="formRules" label-width="120px">
        <t-row :gutter="16">
          <t-col :span="6">
            <t-form-item :label="t('message.cluster')" name="clusterId">
              <t-select
                v-model="queryForm.clusterId"
                :placeholder="t('message.selectCluster')"
                @change="handleClusterChange"
              >
                <t-option
                  v-for="cluster in clusters"
                  :key="cluster.clusterId"
                  :value="cluster.clusterId"
                  :label="`${cluster.clusterId}${cluster.clusterName ? ' (' + cluster.clusterName + ')' : ''}`"
                />
              </t-select>
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item :label="t('message.topic')" name="topicName">
              <t-select
                v-model="queryForm.topicName"
                :placeholder="t('message.selectOrEnterTopic')"
                filterable
                creatable
                :loading="loadingTopics"
                :disabled="!queryForm.clusterId"
              >
                <t-option
                  v-for="topic in topics"
                  :key="topic.topicName"
                  :value="topic.topicName"
                  :label="topic.topicName"
                />
              </t-select>
            </t-form-item>
          </t-col>
          <t-col :span="8">
            <t-form-item :label="t('message.messageId')" name="messageId">
              <t-input
                v-model="queryForm.messageId"
                :placeholder="t('message.enterMessageId')"
                clearable
              />
            </t-form-item>
          </t-col>
          <t-col :span="4">
            <t-form-item label=" " label-width="0">
              <t-space>
                <t-button theme="primary" @click="handleQuery" :loading="querying">
                  <template #icon><t-icon name="search" /></template>
                  {{ t('message.query') }}
                </t-button>
                <t-button theme="default" @click="handleReset">
                  <template #icon><t-icon name="refresh" /></template>
                  {{ t('message.reset') }}
                </t-button>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-card>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && messages.length > 0" class="result-card">
      <template #header>
        <div class="result-header">
          <span
            >Query Results ({{ messages.length }} message{{ messages.length > 1 ? 's' : '' }})</span
          >
        </div>
      </template>
      <t-table :data="messages" :columns="columns" row-key="messageId" :loading="querying">
        <template #bornTime="{ row }">{{ formatTime(row.bornTime) }}</template>
        <template #storeTime="{ row }">{{ formatTime(row.storeTime) }}</template>
        <template #action="{ row }">
          <t-space :size="8">
            <t-button theme="default" variant="outline" size="small" @click="handleViewDetail(row)">
              <template #icon><t-icon name="view-module" /></template>
              {{ t('message.detail') }}
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleViewTrace(row)">
              <template #icon><t-icon name="chart-scatter" /></template>
              {{ t('message.trace') }}
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleResend(row)">
              <template #icon><t-icon name="send" /></template>
              {{ t('message.resend') }}
            </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && messages.length === 0 && hasQueried"
      message="No messages found. Please check the message ID and try again."
    />

    <t-drawer
      v-model:visible="showDetailDrawer"
      header="Message Details"
      size="large"
      :footer="false"
    >
      <div v-if="selectedMessage">
        <t-descriptions bordered title="Message Information">
          <t-descriptions-item label="Message ID" :span="2">{{
            selectedMessage.messageId
          }}</t-descriptions-item>
          <t-descriptions-item label="Topic">{{ selectedMessage.topicName }}</t-descriptions-item>
          <t-descriptions-item label="Born Time">{{
            formatTime(selectedMessage.bornTime)
          }}</t-descriptions-item>
          <t-descriptions-item label="Store Time">{{
            formatTime(selectedMessage.storeTime)
          }}</t-descriptions-item>
          <t-descriptions-item label="Tags">{{ selectedMessage.tags || '-' }}</t-descriptions-item>
          <t-descriptions-item label="Keys">{{ selectedMessage.keys || '-' }}</t-descriptions-item>
          <t-descriptions-item label="Born Host">{{
            selectedMessage.bornHost || '-'
          }}</t-descriptions-item>
        </t-descriptions>

        <t-divider />
        <h3>Message Body</h3>
        <pre class="message-body">{{ selectedMessage.body || 'No body content' }}</pre>

        <t-divider />
        <h3>Properties</h3>
        <t-descriptions
          v-if="selectedMessage.properties && Object.keys(selectedMessage.properties).length > 0"
          bordered
        >
          <t-descriptions-item
            v-for="(value, key) in selectedMessage.properties"
            :key="key"
            :label="key"
            >{{ value }}</t-descriptions-item
          >
        </t-descriptions>
        <t-empty v-else description="No properties" />
      </div>
    </t-drawer>

    <t-drawer v-model:visible="showTraceDrawer" header="Message Trace" size="large" :footer="false">
      <t-timeline v-if="traceInfo.length > 0" :loading="loadingTrace">
        <t-timeline-item v-for="trace in traceInfo" :key="trace.traceId">
          <template #dot>
            <t-icon
              :name="trace.status === 'SUCCESS' ? 'check-circle' : 'close-circle'"
              :style="{ color: trace.status === 'SUCCESS' ? '#00a870' : '#e34d59' }"
            />
          </template>
          <div class="trace-item">
            <div class="trace-action">{{ trace.traceType }}</div>
            <div class="trace-time">{{ formatTime(trace.time) }}</div>
            <div class="trace-host">{{ trace.clientHost }}</div>
            <t-tag :theme="trace.status === 'SUCCESS' ? 'success' : 'danger'" variant="light">{{
              trace.status
            }}</t-tag>
          </div>
        </t-timeline-item>
      </t-timeline>
      <EmptyState v-else message="No trace information available" />
    </t-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import type { FormInstanceFunctions, FormRule, PrimaryTableCol } from 'tdesign-vue-next'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { messageApi } from '@/api/message'
import { clusterApi } from '@/api/cluster'
import { topicApi } from '@/api/topic'
import type {
  MessageInfo,
  MessageTraceInfo,
  QueryMessagesRequest,
  ClusterInfo,
  TopicInfo
} from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const querying = ref(false)
const loadingTrace = ref(false)
const loadingTopics = ref(false)
const hasQueried = ref(false)

const clusters = ref<ClusterInfo[]>([])
const topics = ref<TopicInfo[]>([])
const messages = ref<MessageInfo[]>([])
const selectedMessage = ref<MessageInfo | null>(null)
const traceInfo = ref<MessageTraceInfo[]>([])

const showDetailDrawer = ref(false)
const showTraceDrawer = ref(false)

const queryFormRef = ref<FormInstanceFunctions>()

interface QueryForm {
  clusterId: string
  topicName: string
  messageId: string
}

const queryForm = ref<QueryForm>({
  clusterId: '',
  topicName: '',
  messageId: ''
})

const formRules: Record<string, FormRule[]> = {
  clusterId: [{ required: true, message: t('message.clusterRequired'), type: 'error' }],
  topicName: [{ required: true, message: t('message.topicRequired'), type: 'error' }],
  messageId: [{ required: true, message: t('message.messageIdRequired'), type: 'error' }]
}

const columns: PrimaryTableCol[] = [
  { colKey: 'messageId', title: t('message.messageId'), width: 250, ellipsis: true },
  { colKey: 'topicName', title: t('message.topic'), width: 180 },
  { colKey: 'tags', title: t('message.tags'), width: 120 },
  { colKey: 'keys', title: t('message.keys'), width: 120 },
  { colKey: 'bornTime', title: t('message.bornTime'), cell: 'bornTime', width: 180 },
  { colKey: 'storeTime', title: t('message.storeTime'), cell: 'storeTime', width: 180 },
  { colKey: 'action', title: t('cluster.actions'), cell: 'action', width: 220, fixed: 'right' }
]

const loadClusters = async () => {
  try {
    const response = await clusterApi.listClusters()
    if (response.success) {
      clusters.value = response.data
      if (clusters.value.length > 0) {
        queryForm.value.clusterId = clusters.value[0]?.clusterId || ''
        await loadTopics()
      }
    }
  } catch (error) {
    MessagePlugin.error('Failed to load clusters')
  }
}

const loadTopics = async () => {
  if (!queryForm.value.clusterId) return

  loadingTopics.value = true
  try {
    const response = await topicApi.listTopics(queryForm.value.clusterId)
    if (response.success) {
      topics.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load topics')
  } finally {
    loadingTopics.value = false
  }
}

const handleClusterChange = () => {
  queryForm.value.topicName = ''
  topics.value = []
  loadTopics()
}

const handleQuery = async () => {
  const valid = await queryFormRef.value?.validate()
  if (!valid) return

  if (!queryForm.value.messageId || queryForm.value.messageId.trim() === '') {
    MessagePlugin.warning('Please enter message ID')
    return
  }

  querying.value = true
  hasQueried.value = true
  try {
    const params: QueryMessagesRequest = {
      clusterId: queryForm.value.clusterId,
      topicName: queryForm.value.topicName,
      messageId: queryForm.value.messageId.trim()
    }

    const response = await messageApi.queryMessages(params)
    if (response.success) {
      messages.value = response.data
      if (response.data.length === 0) {
        MessagePlugin.warning('Message not found. Please check the message ID.')
      } else {
        MessagePlugin.success(`Found ${response.data.length} message(s)`)
      }
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || 'Failed to query messages')
  } finally {
    querying.value = false
  }
}

const handleReset = () => {
  queryFormRef.value?.reset()
  messages.value = []
  hasQueried.value = false
  queryForm.value = {
    clusterId: clusters.value[0]?.clusterId || '',
    topicName: '',
    messageId: ''
  }
  if (queryForm.value.clusterId) {
    loadTopics()
  }
}

const handleViewDetail = (message: MessageInfo) => {
  selectedMessage.value = message
  showDetailDrawer.value = true
}

const handleViewTrace = async (message: MessageInfo) => {
  selectedMessage.value = message
  showTraceDrawer.value = true
  loadingTrace.value = true
  try {
    const response = await messageApi.getMessageTrace(message.messageId, queryForm.value.clusterId)
    if (response.success) {
      traceInfo.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load message trace')
  } finally {
    loadingTrace.value = false
  }
}

const handleResend = async (message: MessageInfo) => {
  try {
    const response = await messageApi.resendMessage(message.messageId, queryForm.value.clusterId)
    if (response.success) {
      MessagePlugin.success('Message resent successfully')
    }
  } catch (error) {
    MessagePlugin.error('Failed to resend message')
  }
}

onMounted(async () => {
  loading.value = true
  await loadClusters()
  loading.value = false
})
</script>

<style scoped>
.messages-page {
  height: 100%;
  padding: 24px;
  width: 100%;
}

.filter-card,
.result-card {
  margin-top: 16px;
}

.result-header {
  font-weight: 600;
  font-size: 14px;
}

.message-body {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow: auto;
  max-height: 400px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.trace-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trace-action {
  font-weight: 600;
  font-size: 14px;
}

.trace-time,
.trace-host {
  font-size: 12px;
  color: #666;
}

h3 {
  margin: 16px 0;
  font-size: 16px;
  font-weight: 600;
}

/* 响应式适配 */
@media (min-width: 1920px) {
  .messages-page {
    padding: 32px;
  }
}

@media (min-width: 2560px) {
  .messages-page {
    padding: 40px;
  }
}
</style>
