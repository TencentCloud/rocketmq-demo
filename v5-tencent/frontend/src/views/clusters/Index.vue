<template>
  <div class="clusters-page">
    <PageHeader :title="t('cluster.pageTitle')" :description="t('cluster.pageDescription')">
      <template #actions>
        <t-button theme="primary" @click="showCreateDialog = true">
          <template #icon><t-icon name="add" /></template>
          {{ t('cluster.createCluster') }}
        </t-button>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && clusters.length > 0" class="table-card">
      <t-table :data="clusters" :columns="columns" row-key="clusterId" :loading="tableLoading">
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
              {{ t('cluster.view') }}
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
              <template #icon><t-icon name="edit" /></template>
              {{ t('cluster.edit') }}
            </t-button>
            <t-popconfirm
              :content="t('cluster.deleteConfirm')"
              @confirm="handleDelete(row.clusterId)"
            >
              <t-button theme="danger" variant="outline" size="small">
                <template #icon><t-icon name="delete" /></template>
                {{ t('cluster.delete') }}
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <EmptyState
      v-else-if="!loading && clusters.length === 0"
      :message="t('cluster.noClustersFound')"
      :action-text="t('cluster.createCluster')"
      @action="showCreateDialog = true"
    />

    <!-- Create Dialog -->
    <t-dialog
      v-model:visible="showCreateDialog"
      :header="t('cluster.createClusterTitle')"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="120px">
        <t-form-item :label="t('cluster.clusterName')" name="clusterName">
          <t-input v-model="createForm.clusterName" :placeholder="t('cluster.enterClusterName')" />
        </t-form-item>

        <t-form-item :label="t('cluster.region')" name="region">
          <t-select v-model="createForm.region" :placeholder="t('cluster.selectRegion')">
            <t-option
              v-for="region in regions"
              :key="region.region"
              :value="region.region"
              :label="region.regionName"
            />
          </t-select>
        </t-form-item>

        <t-form-item :label="t('cluster.remark')" name="remark">
          <t-textarea
            v-model="createForm.remark"
            :placeholder="t('cluster.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Edit Dialog -->
    <t-dialog
      v-model:visible="showEditDialog"
      :header="t('cluster.editClusterTitle')"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="120px">
        <t-form-item :label="t('cluster.clusterName')" name="clusterName">
          <t-input v-model="editForm.clusterName" :placeholder="t('cluster.enterClusterName')" />
        </t-form-item>

        <t-form-item :label="t('cluster.remark')" name="remark">
          <t-textarea
            v-model="editForm.remark"
            :placeholder="t('cluster.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Detail Drawer -->
    <t-drawer
      v-model:visible="showDetailDrawer"
      :header="t('cluster.clusterDetailsTitle')"
      size="large"
      :footer="false"
    >
      <t-descriptions v-if="selectedCluster" bordered>
        <t-descriptions-item :label="t('cluster.clusterId')">
          {{ selectedCluster.clusterId }}
        </t-descriptions-item>
        <t-descriptions-item :label="t('cluster.clusterName')">
          {{ selectedCluster.clusterName }}
        </t-descriptions-item>
        <t-descriptions-item :label="t('cluster.region')">
          {{ selectedCluster.region }}
        </t-descriptions-item>
        <t-descriptions-item :label="t('cluster.status')">
          <t-tag
            :theme="selectedCluster.status === 'RUNNING' ? 'success' : 'default'"
            variant="light"
          >
            {{ selectedCluster.status }}
          </t-tag>
        </t-descriptions-item>
        <t-descriptions-item :label="t('cluster.createTime')">
          {{ formatTime(selectedCluster.createTime) }}
        </t-descriptions-item>
        <t-descriptions-item :label="t('cluster.remark')" :span="2">
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
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import { clusterApi } from '@/api/cluster'
import { configApi } from '@/api/config'
import type {
  ClusterInfo,
  CreateClusterRequest,
  UpdateClusterRequest,
  RegionInfo
} from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()

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
  clusterName: [{ required: true, message: t('cluster.enterClusterName'), type: 'error' }],
  region: [{ required: true, message: t('cluster.selectRegion'), type: 'error' }]
}

const editFormRules: Record<string, FormRule[]> = {
  clusterName: [{ required: true, message: t('cluster.enterClusterName'), type: 'error' }]
}

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
    colKey: 'createTime',
    title: t('cluster.createTime'),
    cell: 'createTime',
    width: 180
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
    MessagePlugin.error(t('cluster.failedToLoadClusters'))
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
    MessagePlugin.error(t('cluster.failedToLoadRegions'))
  }
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return

  creating.value = true
  try {
    const response = await clusterApi.createCluster(createForm.value)
    if (response.success) {
      MessagePlugin.success(t('cluster.clusterCreatedSuccess'))
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error(t('cluster.failedToCreateCluster'))
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
      MessagePlugin.success(t('cluster.clusterUpdatedSuccess'))
      showEditDialog.value = false
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error(t('cluster.failedToUpdateCluster'))
  } finally {
    updating.value = false
  }
}

const handleDelete = async (clusterId: string) => {
  try {
    const response = await clusterApi.deleteCluster(clusterId)
    if (response.success) {
      MessagePlugin.success(t('cluster.clusterDeletedSuccess'))
      loadClusters()
    }
  } catch (error) {
    MessagePlugin.error(t('cluster.failedToDeleteCluster'))
  }
}

const handleView = (cluster: ClusterInfo) => {
  selectedCluster.value = cluster
  showDetailDrawer.value = true
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadClusters(), loadRegions()])
  loading.value = false
})
</script>

<style scoped>
.clusters-page {
  height: 100%;
  padding: 24px;
  width: 100%;
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
  }
}

@media (min-width: 2560px) {
  .clusters-page {
    padding: 40px;
  }
}
</style>
