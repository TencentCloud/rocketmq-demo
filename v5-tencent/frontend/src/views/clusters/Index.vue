<template>
  <div class="clusters-page">
    <PageHeader title="Cluster Management" description="Manage RocketMQ clusters">
      <template #actions>
        <t-button theme="primary" @click="showCreateDialog = true">
          <template #icon><t-icon name="add" /></template>
          Create Cluster
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && clusters.length > 0" class="table-card">
      <t-table
        :data="clusters"
        :columns="columns"
        row-key="clusterId"
        :loading="tableLoading"
      >
        <template #status="{ row }">
          <t-tag :theme="row.status === 'RUNNING' ? 'success' : 'default'" variant="light">
            {{ row.status }}
          </t-tag>
        </template>

        <template #createTime="{ row }">
          {{ formatTime(row.createTime) }}
        </template>

        <template #action="{ row }">
          <t-space :size="8">
            <t-button theme="default" variant="outline" size="small" @click="handleView(row)">
              <template #icon><t-icon name="view-module" /></template>
              View
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
              <template #icon><t-icon name="edit" /></template>
              Edit
            </t-button>
            <t-popconfirm
              content="Are you sure you want to delete this cluster?"
              @confirm="handleDelete(row.clusterId)"
            >
              <t-button theme="danger" variant="outline" size="small">
                <template #icon><t-icon name="delete" /></template>
                Delete
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && clusters.length === 0"
      message="No clusters found"
      action-text="Create Cluster"
      @action="showCreateDialog = true"
    />

    <!-- Create Dialog -->
    <t-dialog
      v-model:visible="showCreateDialog"
      header="Create Cluster"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form
        ref="createFormRef"
        :data="createForm"
        :rules="formRules"
        label-width="120px"
      >
        <t-form-item label="Cluster Name" name="clusterName">
          <t-input v-model="createForm.clusterName" placeholder="Enter cluster name" />
        </t-form-item>

        <t-form-item label="Region" name="region">
          <t-select v-model="createForm.region" placeholder="Select region">
            <t-option
              v-for="region in regions"
              :key="region.region"
              :value="region.region"
              :label="region.regionName"
            />
          </t-select>
        </t-form-item>

        <t-form-item label="Remark" name="remark">
          <t-textarea
            v-model="createForm.remark"
            placeholder="Enter cluster description"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Edit Dialog -->
    <t-dialog
      v-model:visible="showEditDialog"
      header="Edit Cluster"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form
        ref="editFormRef"
        :data="editForm"
        :rules="editFormRules"
        label-width="120px"
      >
        <t-form-item label="Cluster Name" name="clusterName">
          <t-input v-model="editForm.clusterName" placeholder="Enter cluster name" />
        </t-form-item>

        <t-form-item label="Remark" name="remark">
          <t-textarea
            v-model="editForm.remark"
            placeholder="Enter cluster description"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Detail Drawer -->
    <t-drawer
      v-model:visible="showDetailDrawer"
      header="Cluster Details"
      size="large"
      :footer="false"
    >
      <t-descriptions v-if="selectedCluster" bordered>
        <t-descriptions-item label="Cluster ID">
          {{ selectedCluster.clusterId }}
        </t-descriptions-item>
        <t-descriptions-item label="Cluster Name">
          {{ selectedCluster.clusterName }}
        </t-descriptions-item>
        <t-descriptions-item label="Region">
          {{ selectedCluster.region }}
        </t-descriptions-item>
        <t-descriptions-item label="Status">
          <t-tag :theme="selectedCluster.status === 'RUNNING' ? 'success' : 'default'" variant="light">
            {{ selectedCluster.status }}
          </t-tag>
        </t-descriptions-item>
        <t-descriptions-item label="Create Time">
          {{ formatTime(selectedCluster.createTime) }}
        </t-descriptions-item>
        <t-descriptions-item label="Remark" :span="2">
          {{ selectedCluster.remark || '-' }}
        </t-descriptions-item>
      </t-descriptions>
    </t-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import type { FormInstanceFunctions, FormRule, PrimaryTableCol } from 'tdesign-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { clusterApi } from '@/api/cluster'
import { configApi } from '@/api/config'
import type { ClusterInfo, CreateClusterRequest, UpdateClusterRequest, RegionInfo } from '@/api/types'
import { formatTime } from '@/utils/format'

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)

const clusters = ref<ClusterInfo[]>([])
const regions = ref<RegionInfo[]>([])
const selectedCluster = ref<ClusterInfo | null>(null)

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDrawer = ref(false)

const createFormRef = ref<FormInstanceFunctions>()
const editFormRef = ref<FormInstanceFunctions>()

const createForm = ref<CreateClusterRequest>({
  clusterName: '',
  region: '',
  remark: ''
})

const editForm = ref<UpdateClusterRequest>({
  clusterName: '',
  remark: ''
})

const currentEditId = ref('')

const formRules: Record<string, FormRule[]> = {
  clusterName: [
    { required: true, message: 'Cluster name is required', type: 'error' }
  ],
  region: [
    { required: true, message: 'Region is required', type: 'error' }
  ]
}

const editFormRules: Record<string, FormRule[]> = {
  clusterName: [
    { required: true, message: 'Cluster name is required', type: 'error' }
  ]
}

const columns: PrimaryTableCol[] = [
  {
    colKey: 'clusterId',
    title: 'Cluster ID',
    width: 200
  },
  {
    colKey: 'clusterName',
    title: 'Cluster Name',
    width: 200
  },
  {
    colKey: 'region',
    title: 'Region',
    width: 150
  },
  {
    colKey: 'status',
    title: 'Status',
    cell: 'status',
    width: 120
  },
  {
    colKey: 'createTime',
    title: 'Create Time',
    cell: 'createTime',
    width: 180
  },
  {
    colKey: 'remark',
    title: 'Remark',
    ellipsis: true
  },
  {
    colKey: 'action',
    title: 'Actions',
    cell: 'action',
    width: 200,
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
    MessagePlugin.error('Failed to load clusters')
  } finally {
    tableLoading.value = false
  }
}

const loadRegions = async () => {
  try {
    const response = await configApi.listRegions()
    if (response.success) {
      regions.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load regions')
  }
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return

  creating.value = true
  try {
    const response = await clusterApi.createCluster(createForm.value)
    if (response.success) {
      MessagePlugin.success('Cluster created successfully')
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error('Failed to create cluster')
  } finally {
    creating.value = false
  }
}

const handleEdit = (cluster: ClusterInfo) => {
  currentEditId.value = cluster.clusterId
  editForm.value = {
    clusterName: cluster.clusterName,
    remark: cluster.remark
  }
  showEditDialog.value = true
}

const handleUpdate = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return

  updating.value = true
  try {
    const response = await clusterApi.updateCluster(currentEditId.value, editForm.value)
    if (response.success) {
      MessagePlugin.success('Cluster updated successfully')
      showEditDialog.value = false
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error('Failed to update cluster')
  } finally {
    updating.value = false
  }
}

const handleDelete = async (clusterId: string) => {
  try {
    const response = await clusterApi.deleteCluster(clusterId)
    if (response.success) {
      MessagePlugin.success('Cluster deleted successfully')
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error('Failed to delete cluster')
  }
}

const handleView = (cluster: ClusterInfo) => {
  selectedCluster.value = cluster
  showDetailDrawer.value = true
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    loadClusters(),
    loadRegions()
  ])
  loading.value = false
})
</script>

<style scoped>
.clusters-page {
  height: 100%;
  padding: 24px;
  width: 92%;
  max-width: 2400px;
  margin: 0 auto;
}

.table-card {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
}

/* 响应式适配 */
@media (min-width: 1920px) {
  .clusters-page {
    padding: 32px;
    width: 90%;
    max-width: 2400px;
  }
}

@media (min-width: 2560px) {
  .clusters-page {
    padding: 40px;
    width: 88%;
    max-width: 2800px;
  }
}
</style>
