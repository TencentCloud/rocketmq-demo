<template>
  <div class="messages-page">
    <PageHeader :title="t('message.pageTitle')" :description="t('message.pageDescription')" />

    <t-card class="filter-card">
      <t-form ref="queryFormRef" :data="queryForm" :rules="formRules" label-width="120px">
        <t-row :gutter="16">
          <t-col :span="5">
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
          <t-col :span="5">
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
          <t-col :span="14">
            <t-form-item :label="t('message.queryType')" name="queryType">
              <t-radio-group v-model="queryForm.queryType" variant="default-filled">
                <t-radio-button value="BY_ID">{{ t('message.queryById') }}</t-radio-button>
                <t-radio-button value="BY_TIME">{{ t('message.queryByTime') }}</t-radio-button>
                <t-radio-button value="RECENT">{{ t('message.queryByRecent') }}</t-radio-button>
              </t-radio-group>
            </t-form-item>
          </t-col>
        </t-row>

        <!-- BY_ID -->
        <t-row v-if="queryForm.queryType === 'BY_ID'" :gutter="16">
          <t-col :span="19">
            <t-form-item :label="t('message.messageId')" name="messageId">
              <t-input v-model="queryForm.messageId" :placeholder="t('message.enterMessageId')" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="5">
            <t-form-item label=" " label-width="120px">
              <t-space>
                <t-button theme="primary" @click="handleQuery" :loading="querying">
                  <template #icon><t-icon name="search" /></template>{{ t('message.query') }}
                </t-button>
                <t-button theme="default" @click="handleReset">
                  <template #icon><t-icon name="refresh" /></template>{{ t('message.reset') }}
                </t-button>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>

        <!-- BY_TIME -->
        <t-row v-if="queryForm.queryType === 'BY_TIME'" :gutter="16">
          <t-col :span="10">
            <t-form-item :label="t('message.timeRange')" name="timeRange">
              <t-date-range-picker
                v-model="queryForm.timeRange"
                enable-time-picker
                format="YYYY-MM-DD HH:mm:ss"
                :placeholder="[t('message.startTime'), t('message.endTime')]"
                style="width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :span="4">
            <t-form-item :label="t('message.msgKey')" name="msgKey">
              <t-input v-model="queryForm.msgKey" :placeholder="t('message.msgKeyPlaceholder')" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="4">
            <t-form-item :label="t('message.tagFilter')" name="tag">
              <t-input v-model="queryForm.tag" :placeholder="t('message.tagPlaceholder')" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label=" " label-width="120px">
              <t-space>
                <t-button theme="primary" @click="handleQuery" :loading="querying">
                  <template #icon><t-icon name="search" /></template>{{ t('message.query') }}
                </t-button>
                <t-button theme="default" @click="handleReset">
                  <template #icon><t-icon name="refresh" /></template>{{ t('message.reset') }}
                </t-button>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>

        <!-- RECENT -->
        <t-row v-if="queryForm.queryType === 'RECENT'" :gutter="16">
          <t-col :span="9">
            <t-form-item :label="t('message.recentNum')" name="recentNum">
              <t-input-number
                v-model="queryForm.recentNum"
                :min="1"
                :max="1024"
                :placeholder="t('message.recentNumPlaceholder')"
                style="width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :span="4">
            <t-form-item :label="t('message.tagFilter')" name="tag">
              <t-input v-model="queryForm.tag" :placeholder="t('message.tagPlaceholder')" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="11">
            <t-form-item label=" " label-width="120px">
              <t-space>
                <t-button theme="primary" @click="handleQuery" :loading="querying">
                  <template #icon><t-icon name="search" /></template>{{ t('message.query') }}
                </t-button>
                <t-button theme="default" @click="handleReset">
                  <template #icon><t-icon name="refresh" /></template>{{ t('message.reset') }}
                </t-button>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-card>

    <!-- 复用按钮片段 -->
    <template #QueryButtons>
      <t-button theme="primary" @click="handleQuery" :loading="querying">
        <template #icon><t-icon name="search" /></template>
        {{ t('message.query') }}
      </t-button>
      <t-button theme="default" @click="handleReset">
        <template #icon><t-icon name="refresh" /></template>
        {{ t('message.reset') }}
      </t-button>
    </template>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && messages.length > 0" class="result-card">
      <template #header>
        <div class="result-header">
          <span>{{ t('message.queryResults') }}（{{ messages.length }} {{ t('message.messages') }}）</span>
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
  queryType: 'BY_ID' | 'BY_TIME' | 'RECENT'
  messageId: string
  msgKey: string
  tag: string
  timeRange: [string, string] | []
  recentNum: number
}

const queryForm = ref<QueryForm>({
  clusterId: '',
  topicName: '',
  queryType: 'BY_ID',
  messageId: '',
  msgKey: '',
  tag: '',
  timeRange: [],
  recentNum: 20
})

const formRules: Record<string, FormRule[]> = {
  clusterId: [{ required: true, message: t('message.clusterRequired'), type: 'error' }],
  topicName: [{ required: true, message: t('message.topicRequired'), type: 'error' }],
  messageId: [{ required: true, message: t('message.messageIdRequired'), type: 'error', validator: () => queryForm.value.queryType !== 'BY_ID' || !!queryForm.value.messageId }],
  timeRange: [{ required: true, message: t('message.timeRangeRequired'), type: 'error', validator: () => queryForm.value.queryType !== 'BY_TIME' || queryForm.value.timeRange.length === 2 }],
  recentNum: [{ required: true, message: t('message.recentNumRequired'), type: 'error', validator: () => queryForm.value.queryType !== 'RECENT' || !!queryForm.value.recentNum }]
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

  querying.value = true
  hasQueried.value = true
  try {
    const params: QueryMessagesRequest = {
      clusterId: queryForm.value.clusterId,
      topicName: queryForm.value.topicName,
      queryType: queryForm.value.queryType
    }

    if (queryForm.value.queryType === 'BY_ID') {
      params.messageId = queryForm.value.messageId.trim()
    } else if (queryForm.value.queryType === 'BY_TIME') {
      const [start, end] = queryForm.value.timeRange as [string, string]
      params.startTime = new Date(start).getTime()
      params.endTime = new Date(end).getTime()
      if (queryForm.value.msgKey) params.msgKey = queryForm.value.msgKey
      if (queryForm.value.tag) params.tag = queryForm.value.tag
    } else if (queryForm.value.queryType === 'RECENT') {
      params.recentNum = queryForm.value.recentNum
      if (queryForm.value.tag) params.tag = queryForm.value.tag
    }

    const response = await messageApi.queryMessages(params)
    if (response.success) {
      messages.value = response.data
      if (response.data.length === 0) {
        MessagePlugin.warning(t('message.messageNotFound'))
      } else {
        MessagePlugin.success(`Found ${response.data.length} message(s)`)
      }
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || t('message.failedToQueryMessages'))
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
    queryType: 'BY_ID',
    messageId: '',
    msgKey: '',
    tag: '',
    timeRange: [],
    recentNum: 20
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
  padding: var(--gap-lg);
  position: relative;
}

.filter-card {
  margin-bottom: var(--gap-md);
}

.filter-card :deep(.t-form__item) {
  margin-bottom: 0;
  padding: 12px 0;
}

.result-header {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.message-body {
  background: #f9fafb;
  padding: var(--gap-md);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-default);
  overflow: auto;
  max-height: 400px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.trace-item {
  display: flex;
  flex-direction: column;
  gap: var(--gap-xs);
}

.trace-action {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.trace-time,
.trace-host {
  font-size: 12px;
  color: var(--color-text-secondary);
}

h3 {
  margin: var(--gap-md) 0 var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}
</style>
