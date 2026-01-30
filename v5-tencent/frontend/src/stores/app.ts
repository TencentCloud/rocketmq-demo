import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const loading = ref(false)
  const title = ref('RocketMQ Dashboard')

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const setTitle = (value: string) => {
    title.value = value
  }

  return {
    loading,
    title,
    setLoading,
    setTitle
  }
})
