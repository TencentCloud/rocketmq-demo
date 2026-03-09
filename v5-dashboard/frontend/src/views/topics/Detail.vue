<template>
  <div class="topic-detail-page">
    <PageHeader :title="topicName" :description="t('topic.topicDetailsTitle')">
      <template #actions>
        <t-button theme="default" variant="outline" @click="router.back()">
          <template #icon><t-icon name="arrow-left" /></template>
          {{ t('common.back') }}
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <div v-if="!loading && topic">
      <t-card class="detail-card">
        <t-descriptions bordered :title="t('topic.basicInformation')">
          <t-descriptions-item :label="t('topic.topicName')">{{ topic.topicName }}</t-descriptions-item>
          <t-descriptions-item :label="t('topic.type')">{{ (topic as any).topicType }}</t-descriptions-item>
          <t-descriptions-item :label="t('topic.partitions')">{{ (topic as any).queueNum }}</t-descriptions-item>
          <t-descriptions-item :label="t('topic.retentionHours')">{{ (topic as any).retentionHours }}</t-descriptions-item>
          <t-descriptions-item :label="t('common.createTime')">{{ formatTime(topic.createTime) }}</t-descriptions-item>
          <t-descriptions-item :label="t('topic.remark')" :span="2">{{ topic.description || '-' }}</t-descriptions-item>
        </t-descriptions>
      </t-card>

      <t-card class="detail-card">
        <template #title>{{ t('topic.producers') }}</template>
        <t-table
          :data="producers"
          :columns="producerColumns"
          :loading="loadingProducers"
          row-key="clientId"
          :empty="t('topic.noProducersConnected')"
          size="small"
        />
      </t-card>
    </div>

    <EmptyState
      v-else-if="!loading && !topic"
      :message="t('topic.noTopicsFound')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol } from 'tdesign-vue-next'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { topicApi } from '@/api/topic'
import type { TopicInfo, ProducerInfo } from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const topicName = computed(() => route.params.topicName as string)
const clusterId = computed(() => route.query.clusterId as string)

const loading = ref(true)
const loadingProducers = ref(false)
const topic = ref<TopicInfo | null>(null)
const producers = ref<ProducerInfo[]>([])

const producerColumns: PrimaryTableCol[] = [
  { colKey: 'clientId', title: t('topic.clientId') },
  { colKey: 'clientAddress', title: t('topic.clientAddress') },
  { colKey: 'language', title: t('topic.language') },
  { colKey: 'version', title: t('topic.version') }
]

const loadTopic = async () => {
  try {
    const response = await topicApi.listTopics(clusterId.value, topicName.value)
    if (response.success && response.data.length > 0) {
      topic.value = response.data[0]
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToLoadTopics'))
  }
}

const loadProducers = async () => {
  loadingProducers.value = true
  try {
    const response = await topicApi.listProducers(topicName.value, clusterId.value)
    if (response.success) {
      producers.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToLoadProducers'))
  } finally {
    loadingProducers.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadTopic()
  loading.value = false
  loadProducers()
})
</script>

<style scoped>
.topic-detail-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.detail-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: var(--gap-md);
}
</style>
