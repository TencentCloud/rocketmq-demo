import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const loading = ref(false)
  const title = ref('RocketMQ Dashboard')
  const selectedClusterId = ref<string>('')

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const setTitle = (value: string) => {
    title.value = value
  }

  const setSelectedClusterId = (clusterId: string) => {
    selectedClusterId.value = clusterId
  }

  return {
    loading,
    title,
    selectedClusterId,
    setLoading,
    setTitle,
    setSelectedClusterId
  }
})
