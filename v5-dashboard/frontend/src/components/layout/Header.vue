<template>
  <header class="app-header">
    <div class="header-left">
      <h2 class="header-title">{{ title }}</h2>
    </div>
    <div class="header-right">
      <LanguageSwitcher />
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import LanguageSwitcher from '../common/LanguageSwitcher.vue'

const route = useRoute()
const { t } = useI18n()

const title = computed(() => {
  return (route.meta.title as string) || t('header.dashboard')
})
</script>

<style scoped>
.app-header {
  height: 64px;
  background: var(--color-bg-container);
  border-bottom: 1px solid var(--color-border-default);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--gap-lg);
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
  letter-spacing: -0.01em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--gap-sm);
}
</style>
