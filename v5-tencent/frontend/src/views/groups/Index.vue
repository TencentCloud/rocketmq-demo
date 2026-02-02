<template>
  <div class="groups-page">
    <PageHeader :title="t('consumer.title')" description="Manage RocketMQ consumer groups">
      <template #actions>
        <t-space>
          <t-input
            v-model="searchKeyword"
            :placeholder="t('consumer.searchGroupPlaceholder')"
            clearable
            style="width: 250px"
            @enter="handleSearch"
            @clear="handleClearSearch"
          >
            <template #suffix-icon>
              <t-icon name="search" @click="handleSearch" style="cursor: pointer" />
            </template>
          </t-input>
          <t-select
            v-model="selectedClusterId"
            :placeholder="t('consumer.selectCluster')"
            style="width: 200px"
            @change="loadGroups"
          >
            <t-option
              v-for="cluster in clusters"
              :key="cluster.clusterId"
              :value="cluster.clusterId"
              :label="`${cluster.clusterId}${cluster.clusterName ? ' (' + cluster.clusterName + ')' : ''}`"
            />
          </t-select>
          <t-button theme="primary" :disabled="!selectedClusterId" @click="showCreateDialog = true">
            <template #icon><t-icon name="add" /></template>
            {{ t('consumer.createGroup') }}
          </t-button>
        </t-space>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && groups.length > 0" class="table-card">
      <t-table :data="groups" :columns="columns" row-key="groupId" :loading="tableLoading">
        <template #consumeEnable="{ row }">
          <t-tag :theme="row.consumeEnable ? 'success' : 'default'" variant="light">
            {{ row.consumeEnable ? t('consumer.enabled') : t('consumer.disabled') }}
          </t-tag>
        </template>
        <template #createTime="{ row }">{{ formatTime(row.createTime) }}</template>
        <template #action="{ row }">
          <t-space :size="8">
            <t-button theme="default" variant="outline" size="small" @click="handleView(row)">
              <template #icon><t-icon name="view-module" /></template>
              {{ t('consumer.view') }}
            </t-button>
            <t-button
              theme="default"
              variant="outline"
              size="small"
              @click="handleResetOffset(row)"
            >
              <template #icon><t-icon name="refresh" /></template>
              {{ t('consumer.reset') }}
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
              <template #icon><t-icon name="edit" /></template>
              {{ t('common.edit') }}
            </t-button>
            <t-popconfirm
              :content="t('consumer.deleteConfirm')"
              @confirm="handleDelete(row.groupName)"
            >
              <t-button theme="danger" variant="outline" size="small">
                <template #icon><t-icon name="delete" /></template>
                {{ t('common.delete') }}
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && (!selectedClusterId || groups.length === 0)"
      :message="
        !selectedClusterId ? t('consumer.pleaseSelectCluster') : t('consumer.noGroupsFound')
      "
      :action-text="selectedClusterId ? t('consumer.createGroup') : undefined"
      @action="showCreateDialog = true"
    />

    <t-dialog
      v-model:visible="showCreateDialog"
      header="Create Consumer Group"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="150px">
        <t-form-item label="Group Name" name="groupName">
          <t-input v-model="createForm.groupName" placeholder="Enter group name" />
        </t-form-item>
        <t-form-item label="Consume Type" name="consumeType">
          <t-select v-model="createForm.consumeType" placeholder="Select type">
            <t-option value="PULL" label="Pull" />
            <t-option value="PUSH" label="Push" />
          </t-select>
        </t-form-item>
        <t-form-item label="Retry Max Times" name="retryMaxTimes">
          <t-input-number v-model="createForm.retryMaxTimes" :min="0" :max="16" />
        </t-form-item>
        <t-form-item label="Remark" name="remark">
          <t-textarea
            v-model="createForm.remark"
            placeholder="Enter description"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="showEditDialog"
      header="Edit Consumer Group"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="150px">
        <t-form-item label="Consume Enable" name="consumeEnable">
          <t-switch v-model="editForm.consumeEnable" />
        </t-form-item>
        <t-form-item label="Retry Max Times" name="retryMaxTimes">
          <t-input-number v-model="editForm.retryMaxTimes" :min="0" :max="16" />
        </t-form-item>
        <t-form-item label="Remark" name="remark">
          <t-textarea v-model="editForm.remark" placeholder="Enter description" :maxlength="200" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="showResetDialog"
      header="Reset Offset"
      :on-confirm="handleResetConfirm"
      :confirm-btn="{ loading: resetting }"
      width="600px"
    >
      <t-form ref="resetFormRef" :data="resetForm" :rules="resetFormRules" label-width="120px">
        <t-form-item label="Topic Name" name="topicName">
          <t-input v-model="resetForm.topicName" placeholder="Enter topic name" />
        </t-form-item>
        <t-form-item label="Timestamp" name="timestamp">
          <t-date-picker
            v-model="resetForm.timestamp"
            mode="date-time"
            placeholder="Select timestamp"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-drawer
      v-model:visible="showDetailDrawer"
      header="Consumer Group Details"
      size="large"
      :footer="false"
    >
      <div v-if="selectedGroup">
        <t-descriptions bordered title="Basic Information">
          <t-descriptions-item label="Group Name">{{
            selectedGroup.groupName
          }}</t-descriptions-item>
          <t-descriptions-item label="Consume Type">{{
            selectedGroup.consumeType
          }}</t-descriptions-item>
          <t-descriptions-item label="Consume Enable">
            <t-tag :theme="selectedGroup.consumeEnable ? 'success' : 'default'" variant="light">
              {{ selectedGroup.consumeEnable ? 'Enabled' : 'Disabled' }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="Retry Max Times">{{
            selectedGroup.retryMaxTimes
          }}</t-descriptions-item>
          <t-descriptions-item label="Create Time">{{
            formatTime(selectedGroup.createTime)
          }}</t-descriptions-item>
          <t-descriptions-item label="Remark" :span="2">{{
            selectedGroup.remark || '-'
          }}</t-descriptions-item>
        </t-descriptions>

        <t-divider />
        <h3>Consumer Clients</h3>
        <t-table
          :data="clients"
          :columns="clientColumns"
          :loading="loadingClients"
          row-key="clientId"
          :empty="'No clients connected'"
          size="small"
        />

        <t-divider />
        <h3>Consumption Lag</h3>
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
      </div>
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
import { groupApi } from '@/api/group'
import { clusterApi } from '@/api/cluster'
import type {
  GroupInfo,
  CreateGroupRequest,
  UpdateGroupRequest,
  ResetOffsetRequest,
  ClusterInfo,
  ConsumerClientInfo,
  ConsumerLagInfo
} from '@/api/types'
import { formatTime, formatNumber } from '@/utils/format'

const { t } = useI18n()

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)
const resetting = ref(false)
const loadingClients = ref(false)
const loadingLag = ref(false)

const selectedClusterId = ref('')
const clusters = ref<ClusterInfo[]>([])
const groups = ref<GroupInfo[]>([])
const selectedGroup = ref<GroupInfo | null>(null)
const clients = ref<ConsumerClientInfo[]>([])
const lagInfo = ref<ConsumerLagInfo[]>([])
const searchKeyword = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showResetDialog = ref(false)
const showDetailDrawer = ref(false)

const createFormRef = ref<FormInstanceFunctions>()
const editFormRef = ref<FormInstanceFunctions>()
const resetFormRef = ref<FormInstanceFunctions>()

const createForm = ref<CreateGroupRequest>({
  clusterId: '',
  groupName: '',
  consumeType: 'PUSH',
  retryMaxTimes: 3,
  remark: ''
})

const editForm = ref<UpdateGroupRequest>({
  consumeEnable: true,
  retryMaxTimes: 3,
  remark: ''
})

const resetForm = ref<ResetOffsetRequest>({
  clusterId: '',
  groupName: '',
  topicName: '',
  timestamp: ''
})

const currentEditName = ref('')

const formRules: Record<string, FormRule[]> = {
  groupName: [{ required: true, message: 'Group name is required', type: 'error' }],
  consumeType: [{ required: true, message: 'Consume type is required', type: 'error' }]
}

const editFormRules: Record<string, FormRule[]> = {}
const resetFormRules: Record<string, FormRule[]> = {
  topicName: [{ required: true, message: 'Topic name is required', type: 'error' }],
  timestamp: [{ required: true, message: 'Timestamp is required', type: 'error' }]
}

const columns: PrimaryTableCol[] = [
  { colKey: 'groupName', title: t('consumer.groupName'), width: 200 },
  { colKey: 'consumeType', title: t('consumer.type'), width: 100 },
  { colKey: 'consumeEnable', title: t('consumer.status'), cell: 'consumeEnable', width: 120 },
  { colKey: 'retryMaxTimes', title: t('consumer.retryMax'), width: 100 },
  { colKey: 'createTime', title: t('common.createTime'), cell: 'createTime', width: 180 },
  { colKey: 'remark', title: t('consumer.remark'), ellipsis: true },
  { colKey: 'action', title: t('consumer.actions'), cell: 'action', width: 250, fixed: 'right' }
]

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

const loadClusters = async () => {
  try {
    const response = await clusterApi.listClusters()
    if (response.success) {
      clusters.value = response.data
      if (clusters.value.length > 0 && !selectedClusterId.value) {
        selectedClusterId.value = clusters.value[0]?.clusterId || ''
      }
    }
  } catch (error) {
    MessagePlugin.error(t('cluster.failedToLoadClusters'))
  }
}

const loadGroups = async () => {
  if (!selectedClusterId.value) return
  tableLoading.value = true
  try {
    const response = await groupApi.listGroups(
      selectedClusterId.value,
      searchKeyword.value || undefined
    )
    if (response.success) {
      groups.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadGroups'))
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  loadGroups()
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  loadGroups()
}

const loadClients = async (groupName: string) => {
  loadingClients.value = true
  try {
    const response = await groupApi.listClients(groupName, selectedClusterId.value)
    if (response.success) {
      clients.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadClients'))
  } finally {
    loadingClients.value = false
  }
}

const loadLag = async (groupName: string) => {
  loadingLag.value = true
  try {
    const response = await groupApi.getLag(groupName, selectedClusterId.value)
    if (response.success) {
      lagInfo.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToLoadLag'))
  } finally {
    loadingLag.value = false
  }
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return
  creating.value = true
  try {
    createForm.value.clusterId = selectedClusterId.value
    const response = await groupApi.createGroup(createForm.value)
    if (response.success) {
      MessagePlugin.success(t('consumer.groupCreatedSuccess'))
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadGroups()
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToCreateGroup'))
  } finally {
    creating.value = false
  }
}

const handleEdit = (group: GroupInfo) => {
  currentEditName.value = group.groupName
  editForm.value = {
    consumeEnable: group.consumeEnable,
    retryMaxTimes: group.retryMaxTimes,
    remark: group.remark
  }
  showEditDialog.value = true
}

const handleUpdate = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return
  updating.value = true
  try {
    const response = await groupApi.updateGroup(currentEditName.value, editForm.value)
    if (response.success) {
      MessagePlugin.success(t('consumer.groupUpdatedSuccess'))
      showEditDialog.value = false
      loadGroups()
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToUpdateGroup'))
  } finally {
    updating.value = false
  }
}

const handleDelete = async (groupName: string) => {
  try {
    const response = await groupApi.deleteGroup(groupName, selectedClusterId.value)
    if (response.success) {
      MessagePlugin.success(t('consumer.groupDeletedSuccess'))
      loadGroups()
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToDeleteGroup'))
  }
}

const handleView = async (group: GroupInfo) => {
  selectedGroup.value = group
  showDetailDrawer.value = true
  loadClients(group.groupName)
  loadLag(group.groupName)
}

const handleResetOffset = (group: GroupInfo) => {
  resetForm.value = {
    clusterId: selectedClusterId.value,
    groupName: group.groupName,
    topicName: '',
    timestamp: ''
  }
  showResetDialog.value = true
}

const handleResetConfirm = async () => {
  const valid = await resetFormRef.value?.validate()
  if (!valid) return
  resetting.value = true
  try {
    const response = await groupApi.resetOffset(resetForm.value.groupName, resetForm.value)
    if (response.success) {
      MessagePlugin.success(t('consumer.offsetResetSuccess'))
      showResetDialog.value = false
      resetFormRef.value?.reset()
    }
  } catch (error) {
    MessagePlugin.error(t('consumer.failedToResetOffset'))
  } finally {
    resetting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadClusters()
  if (selectedClusterId.value) {
    await loadGroups()
  }
  loading.value = false
})
</script>

<style scoped>
.groups-page {
  height: 100%;
  padding: 24px;
  width: 100%;
}

.table-card {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
}

h3 {
  margin: 16px 0;
  font-size: 16px;
  font-weight: 600;
}

/* 响应式适配 */
@media (min-width: 1920px) {
  .groups-page {
    padding: 32px;
  }
}

@media (min-width: 2560px) {
  .groups-page {
    padding: 40px;
  }
}
</style>
