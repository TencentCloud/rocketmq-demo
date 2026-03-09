<template>
  <aside :class="['sidebar', { 'sidebar--collapsed': isCollapsed }]">
    <!-- Logo 区域 -->
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <div class="sidebar-logo-icon">
          <span class="logo-badge">MQ</span>
        </div>
        <transition name="fade">
          <div v-if="!isCollapsed" class="sidebar-logo-text">
            <span class="logo-title">RocketMQ</span>
            <span class="logo-subtitle">Dashboard</span>
          </div>
        </transition>
      </div>
      <button class="collapse-btn" @click="toggleCollapse" :title="isCollapsed ? '展开' : '收起'">
        <t-icon :name="isCollapsed ? 'chevron-right' : 'chevron-left'" size="16px" />
      </button>
    </div>

    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <t-menu
        v-model="activeMenu"
        theme="light"
        :collapsed="isCollapsed"
        :width="isCollapsed ? '64px' : '240px'"
        @change="handleMenuChange"
      >
        <t-menu-item value="dashboard" @click="navigateTo('/dashboard')">
          <template #icon><t-icon name="dashboard" /></template>
          {{ t('sidebar.dashboard') }}
        </t-menu-item>
        <t-menu-item value="clusters" @click="navigateTo('/clusters')">
          <template #icon><t-icon name="server" /></template>
          {{ t('sidebar.cluster') }}
        </t-menu-item>
        <t-menu-item value="topics" @click="navigateTo('/topics')">
          <template #icon><t-icon name="layers" /></template>
          {{ t('sidebar.topic') }}
        </t-menu-item>
        <t-menu-item value="groups" @click="navigateTo('/groups')">
          <template #icon><t-icon name="usergroup" /></template>
          {{ t('sidebar.consumer') }}
        </t-menu-item>
        <t-menu-item value="messages" @click="navigateTo('/messages')">
          <template #icon><t-icon name="mail" /></template>
          {{ t('sidebar.message') }}
        </t-menu-item>
        <t-menu-item value="roles" @click="navigateTo('/roles')">
          <template #icon><t-icon name="user" /></template>
          {{ t('sidebar.role') }}
        </t-menu-item>
      </t-menu>
    </nav>
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
const isCollapsed = ref(false)

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

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
/* ---- 侧边栏容器 ---- */
.sidebar {
  width: 240px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-aside);
  border-right: 1px solid var(--color-border-default);
  transition: width 0.25s ease;
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar--collapsed {
  width: 64px;
}

/* ---- Header 区域 ---- */
.sidebar-header {
  height: 64px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border-default);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  min-width: 0;
}

.sidebar-logo-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-badge {
  font-size: 10px;
  font-weight: 700;
  color: white;
  font-family: var(--font-mono);
  letter-spacing: 0.05em;
}

.sidebar-logo-text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
  white-space: nowrap;
  font-family: var(--font-mono);
  letter-spacing: -0.01em;
}

.logo-subtitle {
  font-size: 11px;
  color: var(--color-text-placeholder);
  white-space: nowrap;
  margin-top: 1px;
}

.collapse-btn {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-placeholder);
  padding: 0;
  transition: var(--transition-default);
}

.collapse-btn:hover {
  background-color: var(--color-primary-light-1);
  color: var(--color-primary);
}

/* ---- 导航区域 ---- */
.sidebar-nav {
  flex: 1;
  overflow: hidden;
  padding: 8px 0;
}

:deep(.t-default-menu),
:deep(.t-menu) {
  border: none !important;
  background: transparent !important;
  width: 100% !important;
  padding: 0 8px;
}

:deep(.t-menu-item) {
  border-radius: var(--radius-md);
  margin: 2px 0;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: var(--transition-default);
}

:deep(.t-menu-item:hover) {
  background-color: var(--color-primary-light-1);
  color: var(--color-primary);
}

:deep(.t-menu-item.t-is-active) {
  background-color: var(--color-primary-light-1);
  color: var(--color-primary);
  font-weight: 600;
}

:deep(.t-icon) {
  font-size: 17px;
}

/* ---- Fade 过渡 ---- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-4px);
}
</style>
