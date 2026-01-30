<template>
  <div class="config-page">
    <PageHeader 
      title="Configuration" 
      description="Configure Tencent Cloud credentials for RocketMQ access"
    />

    <t-card class="config-card">
      <t-form
        ref="formRef"
        :data="formData"
        :rules="formRules"
        label-width="120px"
        @submit="handleSubmit"
      >
        <t-form-item label="Secret ID" name="secretId">
          <t-input
            v-model="formData.secretId"
            placeholder="Enter your Tencent Cloud Secret ID"
            clearable
          />
        </t-form-item>

        <t-form-item label="Secret Key" name="secretKey">
          <t-input
            v-model="formData.secretKey"
            type="password"
            placeholder="Enter your Tencent Cloud Secret Key"
            clearable
          />
        </t-form-item>

        <t-form-item label="Region" name="region">
          <t-select
            v-model="formData.region"
            placeholder="Select a region"
            :loading="loadingRegions"
            clearable
          >
            <t-option
              v-for="region in regions"
              :key="region.region"
              :value="region.region"
              :label="region.regionName"
            />
          </t-select>
        </t-form-item>

        <t-form-item>
          <t-space>
            <t-button theme="primary" type="submit" :loading="saving">
              Save Configuration
            </t-button>
            <t-button theme="default" @click="handleTest" :loading="testing">
              Test Connection
            </t-button>
            <t-button theme="default" @click="handleClear">
              Clear
            </t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import type { FormInstanceFunctions, FormRule } from 'tdesign-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import { configApi } from '@/api/config'
import { useConfigStore } from '@/stores/config'
import type { RegionInfo, CredentialsRequest } from '@/api/types'

const configStore = useConfigStore()
const formRef = ref<FormInstanceFunctions>()

const formData = ref<CredentialsRequest>({
  secretId: '',
  secretKey: '',
  region: ''
})

const regions = ref<RegionInfo[]>([])
const loadingRegions = ref(false)
const saving = ref(false)
const testing = ref(false)

const formRules: Record<string, FormRule[]> = {
  secretId: [
    { required: true, message: 'Secret ID is required', type: 'error' }
  ],
  secretKey: [
    { required: true, message: 'Secret Key is required', type: 'error' }
  ],
  region: [
    { required: true, message: 'Region is required', type: 'error' }
  ]
}

const loadRegions = async () => {
  loadingRegions.value = true
  try {
    const response = await configApi.listRegions()
    if (response.success) {
      regions.value = response.data
    }
  } catch (error) {
    MessagePlugin.error('Failed to load regions')
  } finally {
    loadingRegions.value = false
  }
}

const loadCredentials = async () => {
  try {
    const response = await configApi.getCredentials()
    if (response.success && response.data) {
      formData.value = response.data
    }
  } catch (error) {
    // No credentials saved yet, ignore error
  }
}

const handleSubmit = async ({ validateResult }: { validateResult: boolean }) => {
  if (!validateResult) return

  saving.value = true
  try {
    const response = await configApi.saveCredentials(formData.value)
    if (response.success) {
      configStore.setCredentials(formData.value)
      // Encrypt and store in localStorage
      localStorage.setItem('rocketmq_credentials', btoa(JSON.stringify(formData.value)))
      MessagePlugin.success('Configuration saved successfully')
    }
  } catch (error) {
    MessagePlugin.error('Failed to save configuration')
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  testing.value = true
  try {
    const response = await configApi.testConnection(formData.value)
    if (response.success && response.data) {
      MessagePlugin.success('Connection test successful')
    } else {
      MessagePlugin.error('Connection test failed')
    }
  } catch (error) {
    MessagePlugin.error('Connection test failed')
  } finally {
    testing.value = false
  }
}

const handleClear = () => {
  formRef.value?.reset()
  formData.value = {
    secretId: '',
    secretKey: '',
    region: ''
  }
  configStore.clearCredentials()
  localStorage.removeItem('rocketmq_credentials')
  MessagePlugin.success('Configuration cleared')
}

onMounted(() => {
  loadRegions()
  loadCredentials()
  
  // Try to load from localStorage
  const stored = localStorage.getItem('rocketmq_credentials')
  if (stored) {
    try {
      const decoded = JSON.parse(atob(stored))
      formData.value = decoded
    } catch (e) {
      // Invalid data, ignore
    }
  }
})
</script>

<style scoped>
.config-page {
  max-width: 800px;
}

.config-card {
  margin-top: 24px;
}
</style>
