<template>
  <div class="clusters-page">
    <PageHeader :title="t('cluster.pageTitle')" :description="t('cluster.pageDescription')" />

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && clusters.length > 0" class="table-card">
      <t-table :data="clusters" :columns="columns" row-key="clusterId" :loading="tableLoading">
        <template #status="{ row }">
          <t-tag :theme="row.status === 'RUNNING' ? 'success' : 'default'" variant="light">
            {{ row.status }}
          </t-tag>
        </template>

        <template #action="{ row }">
          <t-button theme="primary" variant="text" size="small" @click="handleView(row)">
            {{ t('cluster.view') }}
          </t-button>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && clusters.length === 0"
      :message="t('cluster.noClustersFound')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol } from 'tdesign-vue-next'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { clusterApi } from '@/api/cluster'
import type { ClusterInfo } from '@/api/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const tableLoading = ref(false)
const clusters = ref<ClusterInfo[]>([])

const columns: PrimaryTableCol[] = [
  {
    colKey: 'clusterId',
    title: t('cluster.clusterId'),
    width: 200
  },
  {
    colKey: 'clusterName',
    title: t('cluster.clusterName'),
    width: 200
  },
  {
    colKey: 'region',
    title: t('cluster.region'),
    width: 150
  },
  {
    colKey: 'status',
    title: t('cluster.status'),
    cell: 'status',
    width: 120
  },
  {
    colKey: 'remark',
    title: t('cluster.remark'),
    ellipsis: true
  },
  {
    colKey: 'action',
    title: t('cluster.actions'),
    cell: 'action',
    width: 100,
    fixed: 'right'
  }
]

const loadClusters = async () => {
  tableLoading.value = true
  try {
    const response = await clusterApi.listClusters()
    if (response.success) {
      clusters.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('cluster.failedToLoadClusters'))
  } finally {
    tableLoading.value = false
  }
}

const handleView = (cluster: ClusterInfo) => {
  router.push(`/clusters/${cluster.clusterId}`)
}

onMounted(async () => {
  loading.value = true
  await loadClusters()
  loading.value = false
})
</script>

<style scoped>
.clusters-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}
</style>
