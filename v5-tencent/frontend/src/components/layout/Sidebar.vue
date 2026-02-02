<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h1 class="sidebar-title">RocketMQ</h1>
    </div>
    <t-menu v-model="activeMenu" theme="light" :collapsed="false" @change="handleMenuChange">
      <t-menu-item value="dashboard" @click="navigateTo('/dashboard')">
        <template #icon>
          <t-icon name="dashboard" />
        </template>
        {{ t('sidebar.dashboard') }}
      </t-menu-item>
      <t-menu-item value="clusters" @click="navigateTo('/clusters')">
        <template #icon>
          <t-icon name="server" />
        </template>
        {{ t('sidebar.cluster') }}
      </t-menu-item>
      <t-menu-item value="topics" @click="navigateTo('/topics')">
        <template #icon>
          <t-icon name="layers" />
        </template>
        {{ t('sidebar.topic') }}
      </t-menu-item>
      <t-menu-item value="groups" @click="navigateTo('/groups')">
        <template #icon>
          <t-icon name="usergroup" />
        </template>
        {{ t('sidebar.consumer') }}
      </t-menu-item>
      <t-menu-item value="messages" @click="navigateTo('/messages')">
        <template #icon>
          <t-icon name="mail" />
        </template>
        {{ t('sidebar.message') }}
      </t-menu-item>
      <t-menu-item value="roles" @click="navigateTo('/roles')">
        <template #icon>
          <t-icon name="user" />
        </template>
        {{ t('sidebar.producer') }}
      </t-menu-item>
    </t-menu>
  </aside>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const activeMenu = ref('dashboard')

watch(
  () => route.path,
  path => {
    const menuName = path.split('/')[1] || 'dashboard'
    activeMenu.value = menuName
  },
  { immediate: true }
)

const handleMenuChange = (value: string) => {
  activeMenu.value = value
}

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
  border-right: 1px solid rgba(0, 82, 217, 0.08);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.03);
}

.sidebar-header {
  min-height: 68px;
  height: 68px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(0, 82, 217, 0.08);
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.02) 0%, rgba(0, 102, 255, 0.02) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 22px;
  font-weight: 700;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  background: linear-gradient(135deg, #0052d9 0%, #0066ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.01em;
  line-height: 1;
  padding-top: 2px;
}

:deep(.t-menu) {
  border: none;
  flex: 1;
  padding: 8px 0;
}

:deep(.t-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  padding: 12px 16px;
  font-weight: 500;
  font-family:
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    sans-serif;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

:deep(.t-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #0052d9 0%, #0066ff 100%);
  border-radius: 0 2px 2px 0;
  transition: height 0.2s ease;
}

:deep(.t-menu-item:hover) {
  background: rgba(0, 82, 217, 0.06);
  transform: translateX(2px);
}

:deep(.t-menu-item:hover::before) {
  height: 60%;
}

:deep(.t-menu-item.t-is-active) {
  background: linear-gradient(90deg, rgba(0, 82, 217, 0.08) 0%, rgba(0, 82, 217, 0.02) 100%);
  color: #0052d9;
  font-weight: 600;
}

:deep(.t-menu-item.t-is-active::before) {
  height: 80%;
}

:deep(.t-icon) {
  font-size: 18px;
  margin-right: 10px;
}

:deep(.t-menu-item .t-icon) {
  transition: all 0.2s ease;
}

:deep(.t-menu-item:hover .t-icon) {
  transform: scale(1.1);
}

:deep(.t-menu-item.t-is-active .t-icon) {
  color: #0052d9;
}

/* 响应式适配 - 与 Header 高度保持一致 */
@media (min-width: 1920px) {
  .sidebar-header {
    min-height: 72px;
    height: 72px;
  }
}

@media (min-width: 2560px) {
  .sidebar-header {
    min-height: 80px;
    height: 80px;
  }
}
</style>
