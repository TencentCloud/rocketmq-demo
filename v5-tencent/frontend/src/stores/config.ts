import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CredentialsRequest } from '@/api/types'

export const useConfigStore = defineStore('config', () => {
  const credentials = ref<CredentialsRequest>({
    secretId: '',
    secretKey: '',
    region: ''
  })

  const isConfigured = ref(false)

  const setCredentials = (data: CredentialsRequest) => {
    credentials.value = data
    isConfigured.value = !!(data.secretId && data.secretKey && data.region)
  }

  const clearCredentials = () => {
    credentials.value = {
      secretId: '',
      secretKey: '',
      region: ''
    }
    isConfigured.value = false
  }

  return {
    credentials,
    isConfigured,
    setCredentials,
    clearCredentials
  }
})
