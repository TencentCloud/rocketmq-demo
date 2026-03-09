<template>
  <div class="group-detail-page">
    <PageHeader :title="groupName" :description="t('consumer.groupDetailsTitle')">
      <template #actions>
        <t-button theme="default" variant="outline" @click="router.back()">
          <template #icon><t-icon name="arrow-left" /></template>
          {{ t('common.back') }}
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <div v-if="!loading && group">
      <t-card class="detail-card">
        <t-descriptions bordered :title="t('consumer.basicInformation')">
          <t-descriptions-item :label="t('consumer.groupName')">{{ group.groupName }}</t-descriptions-item>
          <t-descriptions-item :label="t('consumer.consumeType')">{{ group.consumeType }}</t-descriptions-item>
          <t-descriptions-item :label="t('consumer.consumeEnable')">
            <t-tag :theme="group.consumeEnable ? 'success' : 'default'" variant="light">
              {{ group.consumeEnable ? t('consumer.enabled') : t('consumer.disabled') }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item :label="t('consumer.maxRetryTimes')">{{ group.maxRetryTimes }}</t-descriptions-item>
          <t-descriptions-item :label="t('common.createTime')">{{ formatTime(group.createTime) }}</t-descriptions-item>
          <t-descriptions-item :label="t('consumer.description')" :span="2">{{ group.description || '-' }}</t-descriptions-item>
        </t-descriptions>
      </t-card>

      <t-card class="detail-card">
        <template #title>{{ t('consumer.consumerClients') }}</template>
        <t-table
          :data="clients"
          :columns="clientColumns"
          :loading="loadingClients"
          row-key="clientId"
          :empty="t('consumer.noClientsConnected')"
          size="small"
        />
      </t-card>

      <t-card class="detail-card">
        <template #title>{{ t('consumer.consumptionLag') }}</template>
        <t-table
          :data="lagInfo"
          :columns="lagColumns"
          :loading="loadingLag"
          row-key="topicName"
          size="small"
        >
          <template #lag="{ row }">
            <t-progress :percentage="getLagPercentage(row.lag)" :theme="getLagTheme(row.lag)" />
            <span style="margin-left: 8px">{{ formatNumber(row.lag) }}</span>
          </template>
        </t-table>
      </t-card>
    </div>

    <EmptyState
      v-else-if="!loading && !group"
      :message="t('consumer.noGroupsFound')"
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
import { groupApi } from '@/api/group'
import { clusterApi } from '@/api/cluster'
import type { GroupInfo, ConsumerClientInfo, ConsumerLagInfo } from '@/api/types'
import { formatTime, formatNumber } from '@/utils/format'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const groupName = computed(() => route.params.groupName as string)
const clusterId = computed(() => route.query.clusterId as string)

const loading = ref(true)
const loadingClients = ref(false)
const loadingLag = ref(false)
const group = ref<GroupInfo | null>(null)
const clients = ref<ConsumerClientInfo[]>([])
const lagInfo = ref<ConsumerLagInfo[]>([])

const clientColumns: PrimaryTableCol[] = [
  { colKey: 'clientId', title: t('consumer.clientId') },
  { colKey: 'clientAddress', title: t('consumer.clientAddress') },
  { colKey: 'language', title: t('consumer.language') },
  { colKey: 'version', title: t('consumer.version') },
  { colKey: 'connectionTime', title: t('consumer.connectionTime') }
]

const lagColumns: PrimaryTableCol[] = [
  { colKey: 'topicName', title: t('consumer.topic') },
  { colKey: 'partitionId', title: t('consumer.partition') },
  { colKey: 'lag', title: t('consumer.lag'), cell: 'lag' }
]

const getLagPercentage = (lag: number) => Math.min(100, (lag / 10000) * 100)
const getLagTheme = (lag: number) => {
  if (lag > 10000) return 'danger'
  if (lag > 1000) return 'warning'
  return 'success'
}

const loadGroup = async () => {
  try {
    // Use clusterId from query, fallback to first available cluster
    let cid = clusterId.value
    if (!cid) {
      const clusterRes = await clusterApi.listClusters()
      if (clusterRes.success && clusterRes.data.length > 0) {
        cid = clusterRes.data[0].clusterId
      }
    }
    if (!cid) return
    const response = await groupApi.listGroups(cid, groupName.value)
    if (response.success && response.data.length > 0) {
      group.value = response.data[0]
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadGroups'))
  }
}

const loadClients = async () => {
  loadingClients.value = true
  try {
    const response = await groupApi.listClients(groupName.value, clusterId.value)
    if (response.success) {
      clients.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadClients'))
  } finally {
    loadingClients.value = false
  }
}

const loadLag = async () => {
  loadingLag.value = true
  try {
    const response = await groupApi.getLag(groupName.value, clusterId.value)
    if (response.success) {
      lagInfo.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadLag'))
  } finally {
    loadingLag.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadGroup()
  loading.value = false
  loadClients()
  loadLag()
})
</script>

<style scoped>
.group-detail-page {
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
