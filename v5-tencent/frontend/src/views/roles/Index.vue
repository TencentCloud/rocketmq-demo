<template>
  <div class="roles-page">
    <PageHeader :title="t('role.pageTitle')" :description="t('role.pageDescription')">
      <template #actions>
        <t-space>
          <t-select
            v-model="selectedClusterId"
            :placeholder="t('role.selectCluster')"
            style="width: 200px"
            @change="loadRoles"
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
            {{ t('role.createRole') }}
          </t-button>
        </t-space>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && roles.length > 0" class="table-card">
      <t-table :data="roles" :columns="columns" row-key="roleName" :loading="tableLoading">
        <template #permissions="{ row }">
          <t-space :size="4">
            <t-tag v-for="perm in (row.permissions || [])" :key="perm" :theme="getPermissionTheme(perm)" variant="light" size="small">
              {{ perm }}
            </t-tag>
          </t-space>
        </template>
        <template #createTime="{ row }">{{ formatTime(row.createTime) }}</template>
        <template #action="{ row }">
          <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
            <template #icon><t-icon name="edit" /></template>
            {{ t('common.edit') }}
          </t-button>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && (!selectedClusterId || roles.length === 0)"
      :message="!selectedClusterId ? 'Please select a cluster' : 'No roles found'"
      :action-text="selectedClusterId ? 'Create Role' : undefined"
      @action="showCreateDialog = true"
    />

    <t-dialog
      v-model:visible="showCreateDialog"
      header="Create Role"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="150px">
        <t-form-item label="Role Name" name="roleName">
          <t-input v-model="createForm.roleName" placeholder="Enter role name" />
        </t-form-item>
        <t-form-item label="Permission Type" name="permissionType">
          <t-select v-model="createForm.permissionType" placeholder="Select permission type">
            <t-option value="PUB" label="Publish" />
            <t-option value="SUB" label="Subscribe" />
            <t-option value="PUB_SUB" label="Publish & Subscribe" />
            <t-option value="DENY" label="Deny" />
          </t-select>
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
      header="Edit Role"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="150px">
        <t-form-item label="Permission Type" name="permissionType">
          <t-select v-model="editForm.permissionType" placeholder="Select permission type">
            <t-option value="PUB" label="Publish" />
            <t-option value="SUB" label="Subscribe" />
            <t-option value="PUB_SUB" label="Publish & Subscribe" />
            <t-option value="DENY" label="Deny" />
          </t-select>
        </t-form-item>
        <t-form-item label="Remark" name="remark">
          <t-textarea v-model="editForm.remark" placeholder="Enter description" :maxlength="200" />
        </t-form-item>
      </t-form>
    </t-dialog>
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
import { roleApi } from '@/api/role'
import { clusterApi } from '@/api/cluster'
import type { RoleInfo, CreateRoleRequest, UpdateRoleRequest, ClusterInfo } from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)

const selectedClusterId = ref('')
const clusters = ref<ClusterInfo[]>([])
const roles = ref<RoleInfo[]>([])

const showCreateDialog = ref(false)
const showEditDialog = ref(false)

const createFormRef = ref<FormInstanceFunctions>()
const editFormRef = ref<FormInstanceFunctions>()

const createForm = ref<CreateRoleRequest>({
  clusterId: '',
  roleName: '',
  permissionType: 'PUB_SUB',
  remark: ''
})

const editForm = ref<UpdateRoleRequest>({
  permissionType: 'PUB_SUB',
  remark: ''
})

const currentEditName = ref('')

const formRules: Record<string, FormRule[]> = {
  roleName: [{ required: true, message: t('role.roleNameRequired'), type: 'error' }],
  permissionType: [{ required: true, message: t('role.permissionTypeRequired'), type: 'error' }]
}

const editFormRules: Record<string, FormRule[]> = {
  permissionType: [{ required: true, message: t('role.permissionTypeRequired'), type: 'error' }]
}

const columns: PrimaryTableCol[] = [
  { colKey: 'roleName', title: t('role.roleName'), width: 200 },
  { colKey: 'accessKey', title: 'Access Key', width: 180, ellipsis: true },
  { colKey: 'permissions', title: t('role.permissionType'), cell: 'permissions', width: 180 },
  { colKey: 'createTime', title: t('role.createTime'), cell: 'createTime', width: 180 },
  { colKey: 'description', title: t('role.remark'), ellipsis: true },
  { colKey: 'action', title: t('role.actions'), cell: 'action', width: 120, fixed: 'right' }
]

const getPermissionTheme = (perm: string) => {
  switch (perm) {
    case 'PUB':
      return 'success'
    case 'SUB':
      return 'primary'
    case 'DENY':
      return 'danger'
    default:
      return 'default'
  }
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
    MessagePlugin.error('Failed to load clusters')
  }
}

const loadRoles = async () => {
  if (!selectedClusterId.value) return
  tableLoading.value = true
  try {
    const response = await roleApi.listRoles(selectedClusterId.value)
    if (response.success) {
      roles.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load roles')
  } finally {
    tableLoading.value = false
  }
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return
  creating.value = true
  try {
    createForm.value.clusterId = selectedClusterId.value
    const response = await roleApi.createRole(createForm.value)
    if (response.success) {
      MessagePlugin.success('Role created successfully')
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadRoles()
    }
  } catch (error) {
    MessagePlugin.error('Failed to create role')
  } finally {
    creating.value = false
  }
}

const handleEdit = (role: RoleInfo) => {
  currentEditName.value = role.roleName
  editForm.value = {
    permissionType: role.permissions?.join(',') || '',
    remark: role.description || ''
  }
  showEditDialog.value = true
}

const handleUpdate = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return
  updating.value = true
  try {
    const response = await roleApi.updateRole(currentEditName.value, editForm.value)
    if (response.success) {
      MessagePlugin.success('Role updated successfully')
      showEditDialog.value = false
      loadRoles()
    }
  } catch (error) {
    MessagePlugin.error('Failed to update role')
  } finally {
    updating.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadClusters()
  if (selectedClusterId.value) {
    await loadRoles()
  }
  loading.value = false
})
</script>

<style scoped>
.roles-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}
</style>
