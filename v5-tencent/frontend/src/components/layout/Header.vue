<template>
  <header class="app-header">
    <div class="header-left">
      <h2 class="header-title">{{ title }}</h2>
    </div>
    <div class="header-right">
      <LanguageSwitcher />
      <t-button theme="default" variant="text" @click="handleSettings">
        <template #icon>
          <t-icon name="setting" />
        </template>
        {{ t('header.configuration') }}
      </t-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import LanguageSwitcher from '../common/LanguageSwitcher.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const title = computed(() => {
  return (route.meta.title as string) || t('header.dashboard')
})

const handleSettings = () => {
  router.push('/config')
}
</script>

<style scoped>
.app-header {
  height: 68px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(0, 82, 217, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 10;
}

.app-header::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 82, 217, 0.2) 50%, transparent 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  background: linear-gradient(135deg, #0052d9 0%, #0066ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.01em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 按钮增强 */
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

/* 响应式适配 */
@media (min-width: 1920px) {
  .app-header {
    height: 72px;
    padding: 0 40px;
  }

  .header-title {
    font-size: 22px;
  }
}

@media (min-width: 2560px) {
  .app-header {
    height: 80px;
    padding: 0 48px;
  }

  .header-title {
    font-size: 24px;
  }
}
</style>
