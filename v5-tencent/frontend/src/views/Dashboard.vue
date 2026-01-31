<template>
  <div class="dashboard-page">
    <PageHeader title="Dashboard" description="RocketMQ Cluster Overview">
      <template #actions>
        <t-button 
          theme="default" 
          :loading="refreshing"
          @click="refreshData"
        >
          <template #icon><t-icon name="refresh" /></template>
          Refresh
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading && !refreshing" />

    <div v-if="!loading" class="dashboard-content">
      <!-- Statistics Cards -->
      <t-row :gutter="16" class="stats-row">
        <t-col :span="6">
          <t-card class="stat-card" hover-shadow>
            <div class="stat-content">
              <t-icon name="server" class="stat-icon cluster-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalClusters }}</div>
                <div class="stat-label">Total Clusters</div>
              </div>
            </div>
          </t-card>
        </t-col>
        
        <t-col :span="6">
          <t-card class="stat-card" hover-shadow>
            <div class="stat-content">
              <t-icon name="layers" class="stat-icon topic-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalTopics }}</div>
                <div class="stat-label">Total Topics</div>
              </div>
            </div>
          </t-card>
        </t-col>
        
        <t-col :span="6">
          <t-card class="stat-card" hover-shadow>
            <div class="stat-content">
              <t-icon name="usergroup" class="stat-icon group-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalGroups }}</div>
                <div class="stat-label">Consumer Groups</div>
              </div>
            </div>
          </t-card>
        </t-col>
        
        <t-col :span="6">
          <t-card class="stat-card" hover-shadow>
            <div class="stat-content">
              <t-icon name="mail" class="stat-icon message-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(overview.totalMessages) }}</div>
                <div class="stat-label">Total Messages</div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <!-- Top 10 Lag Groups Table -->
      <t-card class="lag-table-card" title="Top 10 Consumer Groups by Lag" hover-shadow>
        <t-table
          :data="topLagGroups"
          :columns="columns"
          :loading="loadingLag"
          row-key="groupName"
          :empty="'No consumer groups with lag'"
        >
          <template #lag="{ row }">
            <t-tag :theme="getLagTheme(row.lag)" variant="light">
              {{ formatNumber(row.lag) }}
            </t-tag>
          </template>
        </t-table>
      </t-card>
    </div>

    <EmptyState 
      v-else-if="!loading && !overview.totalClusters"
      message="No clusters configured yet"
      action-text="Configure Now"
      @action="$router.push('/config')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol } from 'tdesign-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { dashboardApi } from '@/api/dashboard'
import type { DashboardOverview, TopLagGroup } from '@/api/types'
import { formatNumber } from '@/utils/format'

const loading = ref(true)
const refreshing = ref(false)
const loadingLag = ref(false)

const overview = ref<DashboardOverview>({
  totalClusters: 0,
  totalTopics: 0,
  totalGroups: 0,
  totalMessages: 0
})

const topLagGroups = ref<TopLagGroup[]>([])

let refreshTimer: number | null = null

const columns: PrimaryTableCol[] = [
  {
    colKey: 'groupName',
    title: 'Consumer Group',
    width: 200
  },
  {
    colKey: 'topicName',
    title: 'Topic',
    width: 200
  },
  {
    colKey: 'lag',
    title: 'Message Lag',
    cell: 'lag',
    width: 150
  }
]

const getLagTheme = (lag: number): string => {
  if (lag > 10000) return 'danger'
  if (lag > 1000) return 'warning'
  return 'success'
}

const loadOverview = async () => {
  try {
    const response = await dashboardApi.getOverview()
    if (response.success) {
      overview.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load dashboard overview')
  }
}

const loadTopLagGroups = async () => {
  loadingLag.value = true
  try {
    const response = await dashboardApi.getTopLagGroups(10)
    if (response.success) {
      topLagGroups.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load top lag groups')
  } finally {
    loadingLag.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadTopLagGroups()
    ])
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadTopLagGroups()
    ])
    MessagePlugin.success('Dashboard data refreshed')
  } finally {
    refreshing.value = false
  }
}

const startAutoRefresh = () => {
  refreshTimer = window.setInterval(() => {
    refreshData()
  }, 30000) // 30 seconds
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  padding: 24px;
  width: 100%;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  height: 120px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0, 82, 217, 0.08);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 82, 217, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(0, 82, 217, 0.06);
}

.cluster-icon {
  color: #0052d9;
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.1) 0%, rgba(0, 102, 255, 0.1) 100%);
}

.topic-icon {
  color: #29cc85;
  background: linear-gradient(135deg, rgba(41, 204, 133, 0.1) 0%, rgba(41, 204, 133, 0.08) 100%);
}

.group-icon {
  color: #e37318;
  background: linear-gradient(135deg, rgba(227, 115, 24, 0.1) 0%, rgba(227, 115, 24, 0.08) 100%);
}

.message-icon {
  color: #eb2f96;
  background: linear-gradient(135deg, rgba(235, 47, 150, 0.1) 0%, rgba(235, 47, 150, 0.08) 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  background: linear-gradient(135deg, #000000 0%, #333333 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.lag-table-card {
  flex: 1;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border-radius: 12px;
  overflow: hidden;
}

/* 表格增强 */
:deep(.t-table__th) {
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.04) 0%, rgba(0, 102, 255, 0.04) 100%);
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-weight: 600;
  letter-spacing: 0.01em;
}

:deep(.t-table__body tr) {
  transition: all 0.15s ease;
}

:deep(.t-table__body tr:hover) {
  background: linear-gradient(90deg, rgba(0, 82, 217, 0.03) 0%, rgba(0, 102, 255, 0.02) 100%);
  transform: translateX(4px);
}

/* 响应式适配 */
@media (min-width: 1920px) {
  .dashboard-page {
    padding: 32px;
  }

  .stat-card {
    height: 140px;
  }

  .stat-icon {
    font-size: 56px;
    padding: 14px;
  }

  .stat-value {
    font-size: 36px;
  }
}

@media (min-width: 2560px) {
  .dashboard-page {
    padding: 40px;
  }

  .stat-card {
    height: 160px;
  }

  .stat-icon {
    font-size: 64px;
    padding: 16px;
  }

  .stat-value {
    font-size: 42px;
  }

  .stat-label {
    font-size: 14px;
  }
}
</style>
