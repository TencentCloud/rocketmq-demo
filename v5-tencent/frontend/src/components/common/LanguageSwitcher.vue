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
  localStorage.setItem('locale', data.value)
}
</script>

<style scoped>
:deep(.t-button--variant-text) {
  border-radius: var(--radius-md);
  padding: 4px 10px;
  font-size: 13px;
  font-weight: 500;
  transition: var(--transition-default);
  color: var(--color-text-secondary);
  height: 32px;
}

:deep(.t-button--variant-text:hover) {
  background-color: var(--color-primary-light-1);
  color: var(--color-primary);
}

:deep(.t-button--variant-text .t-icon) {
  font-size: 16px;
}
</style>
