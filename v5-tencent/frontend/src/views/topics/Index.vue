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
            placeholder="Select cluster"
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
              View
            </t-button>
            <t-button
              theme="default"
              variant="outline"
              size="small"
              @click="handleSendMessage(row)"
            >
              <template #icon><t-icon name="send" /></template>
              Send
            </t-button>
            <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
              <template #icon><t-icon name="edit" /></template>
              Edit
            </t-button>
            <t-popconfirm
              content="Are you sure you want to delete this topic?"
              @confirm="handleDelete(row.topicName)"
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
      v-else-if="!loading && (!selectedClusterId || topics.length === 0)"
      :message="!selectedClusterId ? 'Please select a cluster' : 'No topics found'"
      :action-text="selectedClusterId ? 'Create Topic' : undefined"
      @action="showCreateDialog = true"
    />

    <!-- Create Dialog -->
    <t-dialog
      v-model:visible="showCreateDialog"
      header="Create Topic"
      :on-confirm="handleCreate"
      :confirm-btn="{ loading: creating }"
      width="600px"
    >
      <t-form ref="createFormRef" :data="createForm" :rules="formRules" label-width="150px">
        <t-form-item label="Topic Name" name="topicName">
          <t-input v-model="createForm.topicName" placeholder="Enter topic name" />
        </t-form-item>
        <t-form-item label="Message Type" name="messageType">
          <t-select v-model="createForm.messageType" placeholder="Select message type">
            <t-option value="NORMAL" label="Normal" />
            <t-option value="FIFO" label="FIFO" />
            <t-option value="DELAY" label="Delay" />
            <t-option value="TRANSACTION" label="Transaction" />
          </t-select>
        </t-form-item>
        <t-form-item label="Partition Number" name="partitionNum">
          <t-input-number v-model="createForm.partitionNum" :min="1" :max="128" />
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

    <!-- Edit Dialog -->
    <t-dialog
      v-model:visible="showEditDialog"
      header="Edit Topic"
      :on-confirm="handleUpdate"
      :confirm-btn="{ loading: updating }"
      width="600px"
    >
      <t-form ref="editFormRef" :data="editForm" :rules="editFormRules" label-width="150px">
        <t-form-item label="Partition Number" name="partitionNum">
          <t-input-number v-model="editForm.partitionNum" :min="1" :max="128" />
        </t-form-item>
        <t-form-item label="Remark" name="remark">
          <t-textarea v-model="editForm.remark" placeholder="Enter description" :maxlength="200" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Send Message Dialog -->
    <t-dialog
      v-model:visible="showSendDialog"
      header="Send Message"
      :on-confirm="handleSend"
      :confirm-btn="{ loading: sending }"
      width="700px"
    >
      <t-form ref="sendFormRef" :data="sendForm" :rules="sendFormRules" label-width="120px">
        <t-form-item label="Body" name="body">
          <t-textarea v-model="sendForm.body" placeholder="Enter message body" :rows="5" />
        </t-form-item>
        <t-form-item label="Tags" name="tags">
          <t-input v-model="sendForm.tags" placeholder="Optional tags" />
        </t-form-item>
        <t-form-item label="Keys" name="keys">
          <t-input v-model="sendForm.keys" placeholder="Optional keys" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Detail Drawer -->
    <t-drawer
      v-model:visible="showDetailDrawer"
      header="Topic Details"
      size="large"
      :footer="false"
    >
      <div v-if="selectedTopic">
        <t-descriptions bordered title="Basic Information">
          <t-descriptions-item label="Topic Name">{{
            selectedTopic.topicName
          }}</t-descriptions-item>
          <t-descriptions-item label="Message Type">{{
            selectedTopic.messageType
          }}</t-descriptions-item>
          <t-descriptions-item label="Partition Num">{{
            selectedTopic.partitionNum
          }}</t-descriptions-item>
          <t-descriptions-item label="Create Time">{{
            formatTime(selectedTopic.createTime)
          }}</t-descriptions-item>
          <t-descriptions-item label="Remark" :span="2">{{
            selectedTopic.remark || '-'
          }}</t-descriptions-item>
        </t-descriptions>

        <t-divider />

        <h3>Producers</h3>
        <t-table
          :data="producers"
          :columns="producerColumns"
          :loading="loadingProducers"
          row-key="clientId"
          :empty="'No producers connected'"
          size="small"
        />
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
import { topicApi } from '@/api/topic'
import { clusterApi } from '@/api/cluster'
import { messageApi } from '@/api/message'
import type {
  TopicInfo,
  CreateTopicRequest,
  UpdateTopicRequest,
  ClusterInfo,
  ProducerInfo,
  SendMessageRequest
} from '@/api/types'
import { formatTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(true)
const tableLoading = ref(false)
const creating = ref(false)
const updating = ref(false)
const sending = ref(false)
const loadingProducers = ref(false)

const selectedClusterId = ref('')
const clusters = ref<ClusterInfo[]>([])
const topics = ref<TopicInfo[]>([])
const selectedTopic = ref<TopicInfo | null>(null)
const producers = ref<ProducerInfo[]>([])
const searchKeyword = ref('')

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showSendDialog = ref(false)
const showDetailDrawer = ref(false)

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
  { colKey: 'topicName', title: 'Topic Name', width: 200 },
  { colKey: 'messageType', title: 'Type', width: 120 },
  { colKey: 'partitionNum', title: 'Partitions', width: 100 },
  { colKey: 'createTime', title: 'Create Time', cell: 'createTime', width: 180 },
  { colKey: 'remark', title: 'Remark', ellipsis: true },
  { colKey: 'action', title: 'Actions', cell: 'action', width: 250, fixed: 'right' }
]

const producerColumns: PrimaryTableCol[] = [
  { colKey: 'clientId', title: 'Client ID' },
  { colKey: 'clientAddress', title: 'Address' },
  { colKey: 'language', title: 'Language' },
  { colKey: 'version', title: 'Version' }
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
    MessagePlugin.error('Failed to load clusters')
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
    MessagePlugin.error('Failed to load topics')
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

const loadProducers = async (topicName: string) => {
  loadingProducers.value = true
  try {
    const response = await topicApi.listProducers(topicName, selectedClusterId.value)
    if (response.success) {
      producers.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load producers')
  } finally {
    loadingProducers.value = false
  }
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return

  creating.value = true
  try {
    createForm.value.clusterId = selectedClusterId.value
    const response = await topicApi.createTopic(createForm.value)
    if (response.success) {
      MessagePlugin.success('Topic created successfully')
      showCreateDialog.value = false
      createFormRef.value?.reset()
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error('Failed to create topic')
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
      MessagePlugin.success('Topic updated successfully')
      showEditDialog.value = false
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error('Failed to update topic')
  } finally {
    updating.value = false
  }
}

const handleDelete = async (topicName: string) => {
  try {
    const response = await topicApi.deleteTopic(topicName, selectedClusterId.value)
    if (response.success) {
      MessagePlugin.success('Topic deleted successfully')
      loadTopics()
    }
  } catch (error) {
    MessagePlugin.error('Failed to delete topic')
  }
}

const handleView = async (topic: TopicInfo) => {
  selectedTopic.value = topic
  showDetailDrawer.value = true
  loadProducers(topic.topicName)
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
      MessagePlugin.success('Message sent successfully')
      showSendDialog.value = false
      sendFormRef.value?.reset()
    }
  } catch (error) {
    MessagePlugin.error('Failed to send message')
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
  .topics-page {
    padding: 32px;
  }
}

@media (min-width: 2560px) {
  .topics-page {
    padding: 40px;
  }
}
</style>
