<template>
  <div class="topics-page">
    <PageHeader :title="t('topic.title')" description="Manage RocketMQ topics">
      <template #actions>
        <t-space>
          <t-input
            v-model="searchKeyword"
            :placeholder="t('topic.searchTopicPlaceholder')"
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
            :placeholder="t('topic.selectCluster')"
            style="width: 200px"
            @change="loadTopics"
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
            {{ t('topic.createTopic') }}
          </t-button>
        </t-space>
      </template>
    </PageHeader>

    <LoadingOverlay :visible="loading" />

    <t-card v-if="!loading && topics.length > 0" class="table-card">
      <t-table :data="topics" :columns="columns" row-key="topicId" :loading="tableLoading">
        <template #createTime="{ row }">
          {{ formatTime(row.createTime) }}
        </template>

        <template #action="{ row }">
          <t-space :size="8">
            <t-button theme="default" variant="outline" size="small" @click="handleView(row)">
              <template #icon><t-icon name="view-module" /></template>
              {{ t('topic.view') }}
            </t-button>
            <t-button
              theme="default"
              variant="outline"
              size="small"
              @click="handleSendMessage(row)"
            >
              <template #icon><t-icon name="send" /></template>
              {{ t('topic.send') }}
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
              <template #icon><t-icon name="edit" /></template>
              {{ t('common.edit') }}
            </t-button>
            <t-popconfirm
              :content="t('topic.deleteConfirm')"
              @confirm="handleDelete(row.topicName)"
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
      v-else-if="!loading && (!selectedClusterId || topics.length === 0)"
      :message="!selectedClusterId ? t('topic.pleaseSelectCluster') : t('topic.noTopicsFound')"
      :action-text="selectedClusterId ? t('topic.createTopic') : undefined"
      @action="showCreateDialog = true"
    />

    <!-- Create Dialog -->
    <t-dialog
      v-model:visible="showCreateDialog"
      :header="t('topic.createTopicTitle')"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="150px">
        <t-form-item :label="t('topic.topicName')" name="topicName">
          <t-input v-model="createForm.topicName" :placeholder="t('topic.enterTopicName')" />
        </t-form-item>
        <t-form-item :label="t('topic.messageType')" name="messageType">
          <t-select v-model="createForm.messageType" :placeholder="t('topic.selectMessageType')">
            <t-option value="NORMAL" :label="t('topic.normal')" />
            <t-option value="FIFO" :label="t('topic.fifo')" />
            <t-option value="DELAY" :label="t('topic.delay')" />
            <t-option value="TRANSACTION" :label="t('topic.transaction')" />
          </t-select>
        </t-form-item>
        <t-form-item :label="t('topic.partitionNumber')" name="partitionNum">
          <t-input-number v-model="createForm.partitionNum" :min="1" :max="128" />
        </t-form-item>
        <t-form-item :label="t('topic.remark')" name="remark">
          <t-textarea
            v-model="createForm.remark"
            :placeholder="t('topic.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Edit Dialog -->
    <t-dialog
      v-model:visible="showEditDialog"
      :header="t('topic.editTopicTitle')"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="150px">
        <t-form-item :label="t('topic.partitionNumber')" name="partitionNum">
          <t-input-number v-model="editForm.partitionNum" :min="1" :max="128" />
        </t-form-item>
        <t-form-item :label="t('topic.remark')" name="remark">
          <t-textarea
            v-model="editForm.remark"
            :placeholder="t('topic.enterDescription')"
            :maxlength="200"
          />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Send Message Dialog -->
    <t-dialog
      v-model:visible="showSendDialog"
      :header="t('topic.sendMessageTitle')"
      :on-confirm="handleSend"
      :confirm-btn="{ loading: sending }"
      width="700px"
    >
      <t-form ref="sendFormRef" :data="sendForm" :rules="sendFormRules" label-width="120px">
        <t-form-item :label="t('topic.body')" name="body">
          <t-textarea
            v-model="sendForm.body"
            :placeholder="t('topic.enterMessageBody')"
            :rows="5"
          />
        </t-form-item>
        <t-form-item :label="t('topic.tags')" name="tags">
          <t-input v-model="sendForm.tags" :placeholder="t('topic.optionalTags')" />
        </t-form-item>
        <t-form-item :label="t('topic.keys')" name="keys">
          <t-input v-model="sendForm.keys" :placeholder="t('topic.optionalKeys')" />
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
import { topicApi } from '@/api/topic'
import { clusterApi } from '@/api/cluster'
import { messageApi } from '@/api/message'
import type {
  TopicInfo,
  CreateTopicRequest,
  UpdateTopicRequest,
  ClusterInfo,
  SendMessageRequest
} from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)
const sending = ref(false)

const selectedClusterId = ref('')
const clusters = ref<ClusterInfo[]>([])
const topics = ref<TopicInfo[]>([])
const searchKeyword = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showSendDialog = ref(false)

const createFormRef = ref<FormInstanceFunctions>()
const editFormRef = ref<FormInstanceFunctions>()
const sendFormRef = ref<FormInstanceFunctions>()

const createForm = ref<CreateTopicRequest>({
  clusterId: '',
  topicName: '',
  messageType: 'NORMAL',
  partitionNum: 1,
  remark: ''
})

const editForm = ref<UpdateTopicRequest>({
  partitionNum: 1,
  remark: ''
})

const sendForm = ref<SendMessageRequest>({
  clusterId: '',
  topicName: '',
  body: '',
  tags: '',
  keys: ''
})

const currentEditName = ref('')

const formRules: Record<string, FormRule[]> = {
  topicName: [{ required: true, message: 'Topic name is required', type: 'error' }],
  messageType: [{ required: true, message: 'Message type is required', type: 'error' }],
  partitionNum: [{ required: true, message: 'Partition number is required', type: 'error' }]
}

const editFormRules: Record<string, FormRule[]> = {
  partitionNum: [{ required: true, message: 'Partition number is required', type: 'error' }]
}

const sendFormRules: Record<string, FormRule[]> = {
  body: [{ required: true, message: 'Message body is required', type: 'error' }]
}

const columns: PrimaryTableCol[] = [
  { colKey: 'topicName', title: t('topic.topicName'), width: 200 },
  { colKey: 'topicType', title: t('topic.type'), width: 120 },
  { colKey: 'queueNum', title: t('topic.partitions'), width: 100 },
  { colKey: 'retentionHours', title: t('topic.retentionHours'), width: 120 },
  { colKey: 'createTime', title: t('common.createTime'), cell: 'createTime', width: 180 },
  { colKey: 'remark', title: t('topic.remark'), ellipsis: true },
  { colKey: 'action', title: t('topic.actions'), cell: 'action', width: 250, fixed: 'right' }
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

const loadTopics = async () => {
  if (!selectedClusterId.value) return

  tableLoading.value = true
  try {
    const response = await topicApi.listTopics(
      selectedClusterId.value,
      searchKeyword.value || undefined
    )
    if (response.success) {
      topics.value = response.data
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToLoadTopics'))
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  loadTopics()
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  loadTopics()
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return

  creating.value = true
  try {
    createForm.value.clusterId = selectedClusterId.value
    const response = await topicApi.createTopic(createForm.value)
    if (response.success) {
      MessagePlugin.success(t('topic.topicCreatedSuccess'))
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToCreateTopic'))
  } finally {
    creating.value = false
  }
}

const handleEdit = (topic: TopicInfo) => {
  currentEditName.value = topic.topicName
  editForm.value = {
    partitionNum: topic.partitionNum,
    remark: topic.remark
  }
  showEditDialog.value = true
}

const handleUpdate = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return

  updating.value = true
  try {
    const response = await topicApi.updateTopic(currentEditName.value, editForm.value)
    if (response.success) {
      MessagePlugin.success(t('topic.topicUpdatedSuccess'))
      showEditDialog.value = false
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToUpdateTopic'))
  } finally {
    updating.value = false
  }
}

const handleDelete = async (topicName: string) => {
  try {
    const response = await topicApi.deleteTopic(topicName, selectedClusterId.value)
    if (response.success) {
      MessagePlugin.success(t('topic.topicDeletedSuccess'))
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToDeleteTopic'))
  }
}

const handleView = (topic: TopicInfo) => {
  router.push({ name: 'TopicDetail', params: { topicName: topic.topicName }, query: { clusterId: selectedClusterId.value } })
}

const handleSendMessage = (topic: TopicInfo) => {
  sendForm.value = {
    clusterId: selectedClusterId.value,
    topicName: topic.topicName,
    body: '',
    tags: '',
    keys: ''
  }
  showSendDialog.value = true
}

const handleSend = async () => {
  const valid = await sendFormRef.value?.validate()
  if (!valid) return

  sending.value = true
  try {
    const response = await messageApi.sendMessage(sendForm.value)
    if (response.success) {
      MessagePlugin.success(t('topic.messageSentSuccess'))
      showSendDialog.value = false
      sendFormRef.value?.reset()
    }
  } catch (error) {
    MessagePlugin.error(t('topic.failedToSendMessage'))
  } finally {
    sending.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await loadClusters()
  if (selectedClusterId.value) {
    await loadTopics()
  }
  loading.value = false
})
</script>

<style scoped>
.topics-page {
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
