<template>
  <div class="messages-page">
    <PageHeader title="Message Management" description="Query and manage messages">
      <template #actions>
        <t-button theme="primary" @click="collapsed = !collapsed">
          <template #icon><t-icon :name="collapsed ? 'chevron-down' : 'chevron-up'" /></template>
          {{ collapsed ? 'Show' : 'Hide' }} Filter
        </t-button>
      </template>
    </PageHeader>

    <t-card v-show="!collapsed" class="filter-card">
      <t-form ref="queryFormRef" :data="queryForm" label-width="120px" layout="inline">
        <t-form-item label="Cluster">
          <t-select v-model="queryForm.clusterId" placeholder="Select cluster" style="width: 200px">
            <t-option v-for="cluster in clusters" :key="cluster.clusterId" :value="cluster.clusterId" :label="cluster.clusterName" />
          </t-select>
        </t-form-item>
        <t-form-item label="Topic">
          <t-input v-model="queryForm.topicName" placeholder="Topic name" style="width: 200px" />
        </t-form-item>
        <t-form-item label="Message ID">
          <t-input v-model="queryForm.messageId" placeholder="Message ID" style="width: 200px" />
        </t-form-item>
        <t-form-item label="Keys">
          <t-input v-model="queryForm.keys" placeholder="Message keys" style="width: 200px" />
        </t-form-item>
        <t-form-item label="Tags">
          <t-input v-model="queryForm.tags" placeholder="Message tags" style="width: 200px" />
        </t-form-item>
        <t-form-item label="Start Time">
          <t-date-picker v-model="queryForm.startTime" mode="date-time" placeholder="Start time" style="width: 200px" />
        </t-form-item>
        <t-form-item label="End Time">
          <t-date-picker v-model="queryForm.endTime" mode="date-time" placeholder="End time" style="width: 200px" />
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" @click="handleQuery" :loading="querying">Query</t-button>
            <t-button theme="default" @click="handleReset">Reset</t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && messages.length > 0" class="result-card">
      <t-table :data="messages" :columns="columns" row-key="messageId" :loading="querying">
        <template #bornTime="{ row }">{{ formatTime(row.bornTime) }}</template>
        <template #storeTime="{ row }">{{ formatTime(row.storeTime) }}</template>
        <template #action="{ row }">
          <t-space>
            <t-link theme="primary" @click="handleViewDetail(row)">Detail</t-link>
            <t-link theme="primary" @click="handleViewTrace(row)">Trace</t-link>
            <t-link theme="primary" @click="handleResend(row)">Resend</t-link>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <EmptyState v-else-if="!loading && messages.length === 0" message="No messages found" />

    <t-drawer v-model:visible="showDetailDrawer" header="Message Details" size="large" :footer="false">
      <div v-if="selectedMessage">
        <t-descriptions bordered title="Message Information">
          <t-descriptions-item label="Message ID" :span="2">{{ selectedMessage.messageId }}</t-descriptions-item>
          <t-descriptions-item label="Topic">{{ selectedMessage.topicName }}</t-descriptions-item>
          <t-descriptions-item label="Born Time">{{ formatTime(selectedMessage.bornTime) }}</t-descriptions-item>
          <t-descriptions-item label="Store Time">{{ formatTime(selectedMessage.storeTime) }}</t-descriptions-item>
          <t-descriptions-item label="Tags">{{ selectedMessage.tags || '-' }}</t-descriptions-item>
          <t-descriptions-item label="Keys">{{ selectedMessage.keys || '-' }}</t-descriptions-item>
        </t-descriptions>

        <t-divider />
        <h3>Message Body</h3>
        <pre class="message-body">{{ selectedMessage.body }}</pre>

        <t-divider />
        <h3>Properties</h3>
        <t-descriptions bordered>
          <t-descriptions-item v-for="(value, key) in selectedMessage.properties" :key="key" :label="key">{{ value }}</t-descriptions-item>
        </t-descriptions>
      </div>
    </t-drawer>

    <t-drawer v-model:visible="showTraceDrawer" header="Message Trace" size="large" :footer="false">
      <t-timeline v-if="traceInfo.length > 0" :loading="loadingTrace">
        <t-timeline-item v-for="trace in traceInfo" :key="trace.traceId">
          <template #dot>
            <t-icon :name="trace.status === 'SUCCESS' ? 'check-circle' : 'close-circle'" :style="{ color: trace.status === 'SUCCESS' ? '#00a870' : '#e34d59' }" />
          </template>
          <div class="trace-item">
            <div class="trace-action">{{ trace.action }}</div>
            <div class="trace-time">{{ formatTime(trace.timestamp) }}</div>
            <div class="trace-host">{{ trace.clientHost }}</div>
            <t-tag :theme="trace.status === 'SUCCESS' ? 'success' : 'danger'" variant="light">{{ trace.status }}</t-tag>
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
import type { FormInstanceFunctions, TableColumnData } from 'tdesign-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { messageApi } from '@/api/message'
import { clusterApi } from '@/api/cluster'
import type { MessageInfo, MessageTraceInfo, QueryMessagesRequest, ClusterInfo } from '@/api/types'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const querying = ref(false)
const loadingTrace = ref(false)
const collapsed = ref(false)

const clusters = ref<ClusterInfo[]>([])
const messages = ref<MessageInfo[]>([])
const selectedMessage = ref<MessageInfo | null>(null)
const traceInfo = ref<MessageTraceInfo[]>([])

const showDetailDrawer = ref(false)
const showTraceDrawer = ref(false)

const queryFormRef = ref<FormInstanceFunctions>()

const queryForm = ref<QueryMessagesRequest>({
  clusterId: '',
  topicName: '',
  messageId: '',
  keys: '',
  tags: '',
  startTime: '',
  endTime: ''
})

const columns: TableColumnData[] = [
  { colKey: 'messageId', title: 'Message ID', width: 200 },
  { colKey: 'topicName', title: 'Topic', width: 150 },
  { colKey: 'tags', title: 'Tags', width: 120 },
  { colKey: 'keys', title: 'Keys', width: 120 },
  { colKey: 'bornTime', title: 'Born Time', cell: 'bornTime', width: 180 },
  { colKey: 'storeTime', title: 'Store Time', cell: 'storeTime', width: 180 },
  { colKey: 'action', title: 'Actions', cell: 'action', width: 200, fixed: 'right' }
]

const loadClusters = async () => {
  try {
    const response = await clusterApi.listClusters()
    if (response.success) {
      clusters.value = response.data
      if (clusters.value.length > 0) {
        queryForm.value.clusterId = clusters.value[0].clusterId
      }
    }
  } catch (error) {
    MessagePlugin.error('Failed to load clusters')
  }
}

const handleQuery = async () => {
  if (!queryForm.value.clusterId || !queryForm.value.topicName) {
    MessagePlugin.warning('Please select cluster and enter topic name')
    return
  }

  querying.value = true
  try {
    const response = await messageApi.queryMessages(queryForm.value)
    if (response.success) {
      messages.value = response.data
      MessagePlugin.success(`Found ${response.data.length} messages`)
    }
  } catch (error) {
    MessagePlugin.error('Failed to query messages')
  } finally {
    querying.value = false
  }
}

const handleReset = () => {
  queryFormRef.value?.reset()
  messages.value = []
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
}

.filter-card, .result-card {
  margin-top: 16px;
}

.message-body {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow: auto;
  max-height: 400px;
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

.trace-time, .trace-host {
  font-size: 12px;
  color: #666;
}

h3 {
  margin: 16px 0;
  font-size: 16px;
  font-weight: 600;
}
</style>
