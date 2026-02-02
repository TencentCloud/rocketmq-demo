<template>
  <t-dropdown :options="languageOptions" @click="handleLanguageChange">
    <t-button theme="default" variant="text">
      <template #icon>
        <t-icon name="translate" />
      </template>
      {{ currentLanguageLabel }}
    </t-button>
  </t-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const languageOptions = [
  {
    content: '简体中文',
    value: 'zh'
  },
  {
    content: 'English',
    value: 'en'
  }
]

const currentLanguageLabel = computed(() => {
  return locale.value === 'zh' ? '简体中文' : 'English'
})

const handleLanguageChange = (data: { value: string }) => {
  locale.value = data.value
  // 保存到 localStorage
  localStorage.setItem('locale', data.value)
}
</script>

<style scoped>
:deep(.t-button--variant-text) {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s ease;
  color: #0052d9;
}

:deep(.t-button--variant-text:hover) {
  background: rgba(0, 82, 217, 0.08);
  transform: translateY(-1px);
}

:deep(.t-button--variant-text .t-icon) {
  font-size: 18px;
  margin-right: 6px;
}
</style>
