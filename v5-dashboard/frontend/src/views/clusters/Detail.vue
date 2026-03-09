<template>
  <div class="cluster-detail-page">
    <PageHeader :title="cluster?.clusterName || clusterId" :description="t('cluster.clusterDetailsTitle')">
      <template #actions>
        <t-button theme="default" variant="outline" @click="router.back()">
          <template #icon><t-icon name="arrow-left" /></template>
          {{ t('common.back') }}
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <div v-if="!loading && cluster">
      <t-card class="detail-card">
        <t-descriptions bordered :title="t('cluster.basicInformation')">
          <t-descriptions-item :label="t('cluster.clusterId')">
            {{ cluster.clusterId }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.clusterName')">
            {{ cluster.clusterName }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.region')">
            {{ cluster.region }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.status')">
            <t-tag
              :theme="cluster.status === 'RUNNING' ? 'success' : 'default'"
              variant="light"
            >
              {{ cluster.status }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.clusterType')">
            {{ cluster.clusterType || '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.maxTps')">
            {{ cluster.maxTps != null ? cluster.maxTps : '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.topicCount')">
            {{ cluster.topicCount ?? '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.groupCount')">
            {{ cluster.groupCount ?? '-' }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.createTime')">
            {{ formatTime(cluster.createTime) }}
          </t-descriptions-item>
          <t-descriptions-item :label="t('cluster.remark')" :span="2">
            {{ cluster.remark || '-' }}
          </t-descriptions-item>
        </t-descriptions>
      </t-card>
    </div>

    <EmptyState
      v-else-if="!loading && !cluster"
      :message="t('cluster.noClustersFound')"
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
import { clusterApi } from '@/api/cluster'
import type { ClusterInfo } from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const clusterId = computed(() => route.params.clusterId as string)

const loading = ref(true)
const cluster = ref<ClusterInfo | null>(null)

const loadCluster = async () => {
  try {
    const response = await clusterApi.getCluster(clusterId.value)
    if (response.success) {
      cluster.value = response.data
    }
  } catch (error: any) {
    MessagePlugin.error(error.message || t('cluster.failedToLoadCluster'))
  }
}

onMounted(async () => {
  loading.value = true
  await loadCluster()
  loading.value = false
})
</script>

<style scoped>
.cluster-detail-page {
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
