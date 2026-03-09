<template>
  <div class="message-detail-page">
    <PageHeader :title="messageId" :description="t('message.messageDetail')">
      <template #actions>
        <t-space>
          <t-button theme="default" variant="outline" @click="router.back()">
            <template #icon><t-icon name="arrow-left" /></template>
            {{ t('common.back') }}
          </t-button>
          <t-button theme="warning" variant="outline" @click="handleResend" :loading="resending">
            <template #icon><t-icon name="send" /></template>
            {{ t('message.resend') }}
          </t-button>
        </t-space>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <div v-if="!loading && message">
      <!-- Basic Info -->
      <t-card class="detail-card">
        <t-descriptions bordered :title="t('message.messageInformation')">
          <t-descriptions-item :label="t('message.messageId')" :span="2">
            {{ message.messageId }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.topic')">
            {{ message.topicName }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.tags')">
            {{ message.tags || '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.keys')">
            {{ message.keys || '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.bornTime')">
            {{ formatTime(message.bornTime) }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.storeTime')">
            {{ formatTime(message.storeTime) }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.bornHost')">
            {{ message.bornHost || '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.storeHost')">
            {{ message.storeHost || '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.bodySize')">
            {{ message.bodySize != null ? formatSize(message.bodySize) : '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('message.reconsumeTimes')">
            {{ message.reconsumeTimes ?? '-' }}
          </t-descriptions-item>
        </t-descriptions>
      </t-card>

      <!-- Message Body -->
      <t-card class="detail-card">
        <template #title>{{ t('message.messageBody') }}</template>
        <pre v-if="message.body" class="message-body">{{ message.body }}</pre>
        <t-empty v-else :description="t('message.noBodyContent')" />
      </t-card>

      <!-- Properties -->
      <t-card class="detail-card">
        <template #title>{{ t('message.properties') }}</template>
        <t-descriptions
          v-if="message.properties && Object.keys(message.properties).length > 0"
          bordered
        >
          <t-descriptions-item
            v-for="(value, key) in message.properties"
            :key="key"
            :label="String(key)"
          >{{ value }}</t-descriptions-item>
        </t-descriptions>
        <t-empty v-else :description="t('message.noProperties')" />
      </t-card>

      <!-- Message Trace -->
      <t-card class="detail-card">
        <template #title>{{ t('message.messageTraceTitle') }}</template>
        <LoadingOverlay :visible="loadingTrace" />
        <t-timeline v-if="!loadingTrace && traceInfo.length > 0">
          <t-timeline-item v-for="(trace, idx) in traceInfo" :key="idx">
            <template #dot>
              <t-icon
                :name="trace.status === 'SUCCESS' ? 'check-circle' : 'close-circle'"
                :style="{ color: trace.status === 'SUCCESS' ? '#00a870' : '#e34d59' }"
              />
            </template>
            <div class="trace-item">
              <div class="trace-header">
                <span class="trace-type">{{ trace.traceType }}</span>
                <t-tag
                  :theme="trace.status === 'SUCCESS' ? 'success' : 'danger'"
                  variant="light"
                  size="small"
                >{{ trace.status }}</t-tag>
              </div>
              <div class="trace-details">
                <span v-if="trace.time" class="trace-detail">
                  <t-icon name="time" size="14px" /> {{ formatTime(trace.time) }}
                </span>
                <span v-if="trace.clientHost" class="trace-detail">
                  <t-icon name="desktop" size="14px" /> {{ trace.clientHost }}
                </span>
                <span v-if="trace.groupName" class="trace-detail">
                  <t-icon name="usergroup" size="14px" /> {{ trace.groupName }}
                </span>
                <span v-if="trace.costTime != null" class="trace-detail">
                  <t-icon name="chart-line" size="14px" /> {{ trace.costTime }}ms
                </span>
                <span v-if="trace.keys" class="trace-detail">
                  <t-icon name="bookmark" size="14px" /> {{ trace.keys }}
                </span>
              </div>
            </div>
          </t-timeline-item>
        </t-timeline>
        <t-empty v-else-if="!loadingTrace" :description="t('message.noTraceInfo')" />
      </t-card>
    </div>

    <EmptyState
      v-else-if="!loading && !message"
      :message="t('message.messageNotFound')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { messageApi } from '@/api/message'
import type { MessageInfo, MessageTraceInfo } from '@/api/types'
import { formatTime, formatSize } from '@/utils/format'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const messageId = computed(() => route.params.messageId as string)
const clusterId = computed(() => route.query.clusterId as string)
const topicName = computed(() => route.query.topicName as string)

const loading = ref(true)
const loadingTrace = ref(false)
const resending = ref(false)
const message = ref<MessageInfo | null>(null)
const traceInfo = ref<MessageTraceInfo[]>([])

const loadMessage = async () => {
  try {
    const response = await messageApi.getMessage(messageId.value, clusterId.value, topicName.value)
    if (response.success) {
      message.value = response.data
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || t('message.failedToQueryMessages'))
  }
}

const loadTrace = async () => {
  loadingTrace.value = true
  try {
    const response = await messageApi.getMessageTrace(messageId.value, clusterId.value, topicName.value)
    if (response.success) {
      traceInfo.value = response.data
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || t('message.failedToLoadTrace'))
  } finally {
    loadingTrace.value = false
  }
}

const handleResend = async () => {
  resending.value = true
  try {
    const response = await messageApi.resendMessage(messageId.value, clusterId.value)
    if (response.success) {
      MessagePlugin.success(t('message.messageSentSuccess'))
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || t('message.failedToResendMessage'))
  } finally {
    resending.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadMessage()
  loading.value = false
  loadTrace()
})
</script>

<style scoped>
.message-detail-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.detail-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: var(--gap-md);
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
  margin: 0;
}

.trace-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trace-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trace-type {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.trace-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.trace-detail {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
