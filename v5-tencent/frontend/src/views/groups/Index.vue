<template>
  <div class="groups-page">
    <PageHeader :title="t('consumer.title')" :description="t('consumer.pageDescription')">
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
      :header="t('consumer.createGroupTitle')"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="150px">
        <t-form-item :label="t('consumer.groupName')" name="groupName">
          <t-input v-model="createForm.groupName" :placeholder="t('consumer.enterGroupName')" />
        </t-form-item>
        <t-form-item :label="t('consumer.consumeType')" name="consumeType">
          <t-select v-model="createForm.consumeType" :placeholder="t('consumer.selectType')">
            <t-option value="PULL" :label="t('consumer.pull')" />
            <t-option value="PUSH" :label="t('consumer.push')" />
          </t-select>
        </t-form-item>
        <t-form-item :label="t('consumer.maxRetryTimes')" name="maxRetryTimes">
          <t-input-number v-model="createForm.maxRetryTimes" :min="0" :max="16" />
        </t-form-item>
        <t-form-item :label="t('consumer.description')" name="description">
          <t-textarea
            v-model="createForm.description"
            :placeholder="t('consumer.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="showEditDialog"
      :header="t('consumer.editGroupTitle')"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="150px">
        <t-form-item :label="t('consumer.consumeEnable')" name="consumeEnable">
          <t-switch v-model="editForm.consumeEnable" />
        </t-form-item>
        <t-form-item :label="t('consumer.maxRetryTimes')" name="maxRetryTimes">
          <t-input-number v-model="editForm.maxRetryTimes" :min="0" :max="16" />
        </t-form-item>
        <t-form-item :label="t('consumer.description')" name="description">
          <t-textarea
            v-model="editForm.description"
            :placeholder="t('consumer.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="showResetDialog"
      :header="t('consumer.resetOffsetTitle')"
      :on-confirm="handleResetConfirm"
      :confirm-btn="{ loading: resetting }"
      width="600px"
    >
      <t-form ref="resetFormRef" :data="resetForm" :rules="resetFormRules" label-width="120px">
        <t-form-item :label="t('consumer.topicName')" name="topicName">
          <t-input v-model="resetForm.topicName" :placeholder="t('consumer.enterTopicName')" />
        </t-form-item>
        <t-form-item :label="t('consumer.timestamp')" name="timestamp">
          <t-date-picker
            v-model="resetForm.timestamp"
            mode="date-time"
            :placeholder="t('consumer.selectTimestamp')"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
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
  ClusterInfo
} from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)
const resetting = ref(false)

const selectedClusterId = ref('')
const clusters = ref<ClusterInfo[]>([])
const groups = ref<GroupInfo[]>([])
const searchKeyword = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showResetDialog = ref(false)

const createFormRef = ref<FormInstanceFunctions>()
const editFormRef = ref<FormInstanceFunctions>()
const resetFormRef = ref<FormInstanceFunctions>()

const createForm = ref<CreateGroupRequest>({
  clusterId: '',
  groupName: '',
  consumeType: 'PUSH',
  maxRetryTimes: 3,
  description: ''
})

const editForm = ref<UpdateGroupRequest>({
  consumeEnable: true,
  maxRetryTimes: 3,
  description: ''
})

const resetForm = ref<ResetOffsetRequest>({
  clusterId: '',
  groupName: '',
  topicName: '',
  timestamp: ''
})

const currentEditName = ref('')

const formRules: Record<string, FormRule[]> = {
  groupName: [{ required: true, message: t('consumer.groupNameRequired'), type: 'error' }],
  consumeType: [{ required: true, message: t('consumer.consumeTypeRequired'), type: 'error' }]
}

const editFormRules: Record<string, FormRule[]> = {}
const resetFormRules: Record<string, FormRule[]> = {
  topicName: [{ required: true, message: t('consumer.topicNameRequired'), type: 'error' }],
  timestamp: [{ required: true, message: t('consumer.timestampRequired'), type: 'error' }]
}

const columns: PrimaryTableCol[] = [
  { colKey: 'groupName', title: t('consumer.groupName'), width: 200 },
  { colKey: 'consumeType', title: t('consumer.type'), width: 100 },
  { colKey: 'consumeEnable', title: t('consumer.status'), cell: 'consumeEnable', width: 120 },
  { colKey: 'maxRetryTimes', title: t('consumer.retryMax'), width: 120 },
  { colKey: 'description', title: t('consumer.description'), width: 200, ellipsis: true },
  { colKey: 'createTime', title: t('common.createTime'), cell: 'createTime', width: 180 },
  { colKey: 'action', title: t('consumer.actions'), cell: 'action', width: 250, fixed: 'right' }
]

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
    maxRetryTimes: group.maxRetryTimes,
    description: group.description
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

const handleView = (group: GroupInfo) => {
  router.push({ name: 'GroupDetail', params: { groupName: group.groupName }, query: { clusterId: selectedClusterId.value } })
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
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

h3 {
  margin: var(--gap-md) 0 var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}
</style>
