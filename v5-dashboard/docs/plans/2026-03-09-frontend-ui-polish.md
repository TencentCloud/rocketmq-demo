# Frontend UI/UX Polish 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 对现有 TDesign Vue 3 前端进行增量式 UI/UX 精致化重构，解决间距不统一、样式冲突、交互细节不足、Sidebar 无折叠等问题。

**Architecture:** 保留现有技术栈（Vue 3 + TDesign + Vue Router + Pinia + vue-i18n），通过统一 CSS 变量、规范组件用法、优化交互细节来提升视觉一致性与专业度。

**Tech Stack:** Vue 3, TDesign-Vue-Next ^1.18.0, TypeScript, Vite, vue-i18n

---

## Task 1: 规范化全局 CSS 变量与 style.css

**问题：**
- `style.css` 包含 Vite 默认暗色模式样式（`background-color: #242424`），与 App.vue 的明色风格冲突
- CSS 变量分散在 `:root` 和多个组件内，无统一设计 token
- `@media (prefers-color-scheme)` 默认样式会在 light 模式下造成颜色跳变

**Files:**
- Modify: `frontend/src/style.css`
- Modify: `frontend/src/App.vue`

**Step 1: 清理 style.css**

将 `frontend/src/style.css` 替换为如下内容（移除暗色默认、规范基础重置）：

```css
/* =============================================
   全局基础样式 & 设计 Token
   =============================================
   这里定义所有设计变量，供整个应用使用。
   TDesign 组件的全局覆写统一在 App.vue 中。
*/

:root {
  /* ---- 颜色 token ---- */
  --color-primary: #0052d9;
  --color-primary-hover: #0047c0;
  --color-primary-light-1: rgba(0, 82, 217, 0.08);
  --color-primary-light-2: rgba(0, 82, 217, 0.04);
  --color-primary-border: rgba(0, 82, 217, 0.15);

  --color-bg-page: #f3f4f6;
  --color-bg-container: #ffffff;
  --color-bg-aside: #ffffff;

  --color-text-primary: #1a1a2e;
  --color-text-secondary: #4b5563;
  --color-text-placeholder: #9ca3af;
  --color-text-disabled: #d1d5db;

  --color-border-default: #e5e7eb;
  --color-border-light: #f3f4f6;

  --color-success: #00a870;
  --color-warning: #e37318;
  --color-danger: #d54941;

  /* ---- 间距 token ---- */
  --gap-xs: 4px;
  --gap-sm: 8px;
  --gap-md: 16px;
  --gap-lg: 24px;
  --gap-xl: 32px;

  /* ---- 圆角 token ---- */
  --radius-sm: 4px;
  --radius-md: 6px;
  --radius-lg: 8px;
  --radius-xl: 12px;

  /* ---- 字体 token ---- */
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;

  /* ---- 阴影 token ---- */
  --shadow-card: 0 1px 4px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-card-hover: 0 4px 12px rgba(0, 82, 217, 0.10);
  --shadow-header: 0 1px 0 var(--color-border-default);
  --shadow-sidebar: 1px 0 0 var(--color-border-default);

  /* ---- 动效 token ---- */
  --transition-default: all 0.2s ease;
  --transition-spring: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* ---- 基础重置 ---- */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: var(--font-sans);
  font-size: 14px;
  line-height: 1.5;
  color: var(--color-text-primary);
  background-color: var(--color-bg-page);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* ---- 原生元素规范化 ---- */
a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  color: var(--color-primary-hover);
}

/* Scrollbar 美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
```

**Step 2: 精简 App.vue 的 style 块**

在 `frontend/src/App.vue` 的 `<style>` 部分，将原有的散乱覆写样式整理为基于 CSS token 的版本。
移除 `*` 重置（已在 style.css 中完成），以及硬编码的颜色值，改用 CSS 变量：

```vue
<style>
/* TDesign 全局主题覆写 */

/* ---- Card ---- */
:deep(.t-card) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border-light);
  transition: var(--transition-default);
}

:deep(.t-card:hover) {
  box-shadow: var(--shadow-card-hover);
  border-color: var(--color-primary-border);
}

:deep(.t-card__body) {
  padding: var(--gap-lg);
}

:deep(.t-card__header) {
  padding: var(--gap-md) var(--gap-lg);
  border-bottom: 1px solid var(--color-border-light);
}

/* ---- Button ---- */
:deep(.t-button) {
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: var(--transition-default);
}

:deep(.t-button--theme-primary) {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

:deep(.t-button--theme-primary:hover) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
  box-shadow: 0 2px 8px rgba(0, 82, 217, 0.25);
}

:deep(.t-button--theme-default) {
  border-color: var(--color-primary-border);
  color: var(--color-primary);
}

:deep(.t-button--theme-default:hover) {
  border-color: var(--color-primary);
  background-color: var(--color-primary-light-2);
}

/* ---- Table ---- */
:deep(.t-table) {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:deep(.t-table__th) {
  font-weight: 600;
  color: var(--color-text-secondary);
  font-size: 13px;
  letter-spacing: 0.02em;
  background-color: #f9fafb;
}

:deep(.t-table__body tr:hover td) {
  background-color: var(--color-primary-light-2);
}

/* ---- Tag ---- */
:deep(.t-tag) {
  border-radius: var(--radius-sm);
  font-weight: 500;
  font-size: 12px;
}

/* ---- Input ---- */
:deep(.t-input) {
  border-radius: var(--radius-md);
}

:deep(.t-input:not(.t-is-disabled):hover .t-input__inner) {
  border-color: var(--color-primary);
}

:deep(.t-input.t-is-focused .t-input__inner),
:deep(.t-input--focused .t-input__inner) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(0, 82, 217, 0.12);
}

/* ---- Select ---- */
:deep(.t-select) {
  border-radius: var(--radius-md);
}

/* ---- Form ---- */
:deep(.t-form-item__label) {
  font-weight: 500;
  color: var(--color-text-primary);
}

/* ---- Dialog ---- */
:deep(.t-dialog) {
  border-radius: var(--radius-xl);
}

/* ---- Drawer ---- */
:deep(.t-drawer__header) {
  border-bottom: 1px solid var(--color-border-default);
}
</style>
```

**Step 3: 提交**

```bash
git add frontend/src/style.css frontend/src/App.vue
git commit -m "style: unify global css variables and clean up default vite styles"
```

---

## Task 2: 重构 Sidebar 组件（折叠功能 + 标志优化）

**问题：**
- Sidebar 宽度固定 260px，无法折叠，占用小屏幕空间
- 标题 "Tencent RocketMQ" 是普通文本，缺少 logo 感
- `t-menu-item` 的 `::before` 伪元素动效在 TDesign 内部 DOM 结构中实际无法命中
- Header 高度与 Sidebar 头部通过媒体查询分别管理，应统一

**Files:**
- Modify: `frontend/src/components/layout/Sidebar.vue`
- Modify: `frontend/src/components/layout/Header.vue`
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/stores/app.ts`

**Step 1: 更新 Sidebar.vue**

将侧边栏改为支持折叠，折叠后只显示图标，展开时显示图标+文字：

```vue
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
```

**Step 2: 更新 Header.vue（与 Sidebar 高度对齐）**

```vue
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
```

**Step 3: 更新 MainLayout.vue（主内容区背景色）**

```vue
<template>
  <div class="main-layout">
    <Sidebar />
    <div class="layout-content">
      <Header />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: var(--color-bg-page);
}

.layout-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: var(--color-bg-page);
}
</style>
```

**Step 4: 提交**

```bash
git add frontend/src/components/layout/Sidebar.vue frontend/src/components/layout/Header.vue frontend/src/layouts/MainLayout.vue
git commit -m "feat: add sidebar collapse, align header/sidebar height, refine logo design"
```

---

## Task 3: 优化通用组件（PageHeader、EmptyState、LoadingOverlay）

**问题：**
- `PageHeader` 有 16px 底部 margin + 24px/32px padding，与卡片 margin-top: 16px 叠加后有 40px 间距，过于宽松
- `EmptyState` 无 Card 包装，直接悬浮在页面上；empty icon 颜色 `#ddd` 太浅
- `LoadingOverlay` 的 "Loading..." 文字未国际化

**Files:**
- Modify: `frontend/src/components/common/PageHeader.vue`
- Modify: `frontend/src/components/common/EmptyState.vue`
- Modify: `frontend/src/components/common/LoadingOverlay.vue`

**Step 1: 更新 PageHeader.vue**

```vue
<template>
  <div class="page-header">
    <div class="header-left">
      <h1 class="page-title">{{ title }}</h1>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--gap-md) var(--gap-lg);
  background-color: var(--color-bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  margin-bottom: var(--gap-md);
}

.header-left {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.4;
  font-family: var(--font-sans);
  letter-spacing: -0.01em;
}

.page-description {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 4px 0 0 0;
  line-height: 1.5;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--gap-sm);
  flex-shrink: 0;
  margin-left: var(--gap-md);
}
</style>
```

**Step 2: 更新 EmptyState.vue（带 Card 包装，图标优化）**

```vue
<template>
  <div class="empty-state">
    <t-card class="empty-card">
      <div class="empty-inner">
        <div class="empty-icon-wrapper">
          <t-icon name="inbox" size="40px" class="empty-icon" />
        </div>
        <p class="empty-title">{{ message || t('common.noData') }}</p>
        <t-button
          v-if="actionText"
          theme="primary"
          variant="outline"
          size="small"
          @click="handleAction"
        >
          <template #icon><t-icon name="add" /></template>
          {{ actionText }}
        </t-button>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  message?: string
  actionText?: string
}>()

const emit = defineEmits<{
  action: []
}>()

const handleAction = () => {
  emit('action')
}
</script>

<style scoped>
.empty-state {
  padding: var(--gap-md) 0;
}

.empty-card {
  border: 1px dashed var(--color-border-default) !important;
  box-shadow: none !important;
  background-color: transparent !important;
}

.empty-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--gap-xl) var(--gap-lg);
  gap: var(--gap-md);
  text-align: center;
}

.empty-icon-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
  border-radius: 50%;
}

.empty-icon {
  color: var(--color-text-disabled);
}

.empty-title {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
  max-width: 320px;
  line-height: 1.6;
}
</style>
```

**Step 3: 更新 LoadingOverlay.vue（国际化 + 精简）**

```vue
<template>
  <div v-if="visible" class="loading-overlay">
    <div class="loading-content">
      <t-loading size="large" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
}>()
</script>

<style scoped>
.loading-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border-radius: inherit;
}
</style>
```

注意：`LoadingOverlay` 改为 `position: absolute`，在各 View 内需将页面容器设置 `position: relative`，以确保 Overlay 只覆盖内容区域而非全屏遮罩 Sidebar。

**Step 4: 提交**

```bash
git add frontend/src/components/common/
git commit -m "refactor: improve PageHeader, EmptyState, LoadingOverlay components"
```

---

## Task 4: Dashboard 视觉专项提升

**问题：**
- `feature-card` 硬编码 `height: 140px`，内容居中方式生硬
- `welcome-card` 和 `guide-card` 使用重复的 gradient background，与全局 Card 风格不符
- `limitations-card` 使用黄色渐变背景，与整体蓝色主题割裂
- `t-alert` 嵌套 slot 的 limitation-list 使用硬编码 `split(':')` 解析国际化字符串，不健壮

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`

**Step 1: 重写 Dashboard.vue 中 `<style>` 块**

将原有 scoped 样式替换为基于 CSS token 的版本：

```css
<style scoped>
.dashboard-page {
  height: 100%;
  padding: var(--gap-lg);
  overflow-y: auto;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: var(--gap-md);
}

/* ---- Card Headers ---- */
.card-header {
  display: flex;
  align-items: center;
  gap: var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.header-icon {
  font-size: 18px;
  color: var(--color-primary);
}

/* ---- Welcome ---- */
.welcome-content p {
  margin: 6px 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

/* ---- Guide ---- */
.guide-content {
  padding: var(--gap-sm) 0;
}

/* ---- Feature Cards ---- */
.feature-row {
  margin: 0;
}

.feature-card {
  cursor: pointer;
  transition: var(--transition-spring);
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 82, 217, 0.12);
  border-color: var(--color-primary-border) !important;
}

.feature-content {
  display: flex;
  align-items: center;
  gap: var(--gap-md);
  padding: var(--gap-sm) 0;
}

.feature-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.cluster-icon {
  color: var(--color-primary);
  background-color: rgba(0, 82, 217, 0.08);
}

.topic-icon {
  color: var(--color-success);
  background-color: rgba(0, 168, 112, 0.08);
}

.group-icon {
  color: var(--color-warning);
  background-color: rgba(227, 115, 24, 0.08);
}

.message-icon {
  color: var(--color-danger);
  background-color: rgba(213, 73, 65, 0.08);
}

.feature-info {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* ---- Limitations ---- */
.limitations-content {
  padding: var(--gap-xs) 0;
}

.limitation-list {
  margin: var(--gap-sm) 0 0 0;
  padding-left: var(--gap-lg);
  list-style: disc;
}

.limitation-list li {
  margin: var(--gap-xs) 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

/* ---- Docs ---- */
.docs-content :deep(.t-list-item__meta-avatar) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--color-primary-light-1);
  border-radius: var(--radius-md);
}

.docs-content :deep(.t-list-item__meta-avatar .t-icon) {
  color: var(--color-primary);
}
</style>
```

**Step 2: 更新 `feature-card` template 内 layout（改为横向）**

将 Dashboard.vue 中 Feature Cards 区域的 `feature-content` 由纵向居中改为横向布局，并移除硬编码高度。原来是：
```html
<div class="feature-content">
  <t-icon ... />
  <div class="feature-info">...</div>
</div>
```

与新的 CSS 样式配合（`display: flex; align-items: center; gap: 16px;`），无需修改 template，CSS 调整就够了。但需要移除 `feature-card` 的硬编码高度 `height: 140px`，确保 template 中无此内联样式。

**Step 3: 提交**

```bash
git add frontend/src/views/Dashboard.vue
git commit -m "style: modernize dashboard layout with css tokens, improve feature card design"
```

---

## Task 5: 批量调整数据页面间距与表格样式

**问题：**
- 所有 Index.vue 页面都有 `padding: 24px`，但 `PageHeader` 内部也有 `padding: 16px 24px`，造成双重内边距
- `table-card` 的 `margin-top: 16px` 在 PageHeader 已有 `margin-bottom: 16px` 后叠加
- `roles/Index.vue` 的操作列使用 `t-link` 而非 `t-button`，与其他页面风格不一致
- `messages/Index.vue` 的 result-header 文字内嵌英文硬编码（"Query Results"）

**Files:**
- Modify: `frontend/src/views/clusters/Index.vue`
- Modify: `frontend/src/views/topics/Index.vue`
- Modify: `frontend/src/views/groups/Index.vue`
- Modify: `frontend/src/views/messages/Index.vue`
- Modify: `frontend/src/views/roles/Index.vue`

**Step 1: 批量修正 scoped `<style>` 块**

以下改动应用于所有五个 Index.vue 文件，原则是：
- 页面容器内边距统一用 CSS token：`padding: var(--gap-lg)`
- `position: relative` 使 LoadingOverlay 相对于内容区定位
- `table-card` 不需要 `margin-top`（PageHeader 已有 margin-bottom）

对每个文件的 `<style scoped>` 块做以下替换：

**clusters/Index.vue:**
```css
<style scoped>
.clusters-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}
</style>
```

**topics/Index.vue:**
```css
<style scoped>
.topics-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

h3 {
  margin: var(--gap-md) 0 var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}
</style>
```

**groups/Index.vue:**
```css
<style scoped>
.groups-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

h3 {
  margin: var(--gap-md) 0 var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}
</style>
```

**messages/Index.vue:**
```css
<style scoped>
.messages-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.filter-card,
.result-card {
  /* PageHeader 已有 margin-bottom，无需再加 */
}

.result-header {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.message-body {
  background: #f9fafb;
  padding: var(--gap-md);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-default);
  overflow: auto;
  max-height: 400px;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.trace-item {
  display: flex;
  flex-direction: column;
  gap: var(--gap-xs);
}

.trace-action {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.trace-time,
.trace-host {
  font-size: 12px;
  color: var(--color-text-secondary);
}

h3 {
  margin: var(--gap-md) 0 var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}
</style>
```

**roles/Index.vue:**
```css
<style scoped>
.roles-page {
  height: 100%;
  padding: var(--gap-lg);
  position: relative;
}

.table-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}
</style>
```

**Step 2: 修复 roles/Index.vue 操作列风格（t-link → t-button）**

在 `roles/Index.vue` 的 template 中，找到操作列：
```html
<template #action="{ row }">
  <t-space>
    <t-link theme="primary" @click="handleEdit(row)">Edit</t-link>
    <t-popconfirm
      content="Are you sure you want to delete this role?"
      @confirm="handleDelete(row.roleName)"
    >
      <t-link theme="danger">Delete</t-link>
    </t-popconfirm>
  </t-space>
</template>
```

替换为与其他页面一致的 `t-button` 风格：
```html
<template #action="{ row }">
  <t-space :size="8">
    <t-button theme="default" variant="outline" size="small" @click="handleEdit(row)">
      <template #icon><t-icon name="edit" /></template>
      {{ t('common.edit') }}
    </t-button>
    <t-popconfirm
      :content="t('role.deleteConfirm')"
      @confirm="handleDelete(row.roleName)"
    >
      <t-button theme="danger" variant="outline" size="small">
        <template #icon><t-icon name="delete" /></template>
        {{ t('common.delete') }}
      </t-button>
    </t-popconfirm>
  </t-space>
</template>
```

**Step 3: 修复 messages/Index.vue 硬编码文字**

找到：
```html
<span>Query Results ({{ messages.length }} message{{ messages.length > 1 ? 's' : '' }})</span>
```

替换为使用 i18n（已有 key `message.queryResults` 和 `message.messages`）：
```html
<span>{{ t('message.queryResults') }}（{{ messages.length }} {{ t('message.messages') }}）</span>
```

**Step 4: 提交**

```bash
git add frontend/src/views/
git commit -m "style: unify data page padding/spacing, fix roles action buttons, fix hardcoded strings"
```

---

## Task 6: LanguageSwitcher 和 components/common 收尾

**问题：**
- `LanguageSwitcher.vue` 内部有 `transform: translateY(-1px)` hover 动效，在 Header 内使用时会导致轻微抖动
- `SearchBar.vue` 存在但并未在任何 Index.vue 中实际使用（各页面都内联了搜索框）

**Files:**
- Modify: `frontend/src/components/common/LanguageSwitcher.vue`
- Modify: `frontend/src/components/layout/Header.vue` (已在 Task 2 更新，此处只检查)

**Step 1: 修正 LanguageSwitcher 的 hover 动效**

```vue
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
```

**Step 2: 提交**

```bash
git add frontend/src/components/common/LanguageSwitcher.vue
git commit -m "style: clean up language switcher hover animation, use css tokens"
```

---

## 执行顺序

1. Task 1 (CSS 变量/style.css) → 这是基础，其他 Task 依赖这里定义的变量
2. Task 2 (Sidebar/Header/Layout)
3. Task 3 (通用组件)
4. Task 4 (Dashboard)
5. Task 5 (数据页面)
6. Task 6 (收尾)

## 注意事项

- 每个 Task 完成后需要在浏览器中验证样式无破坏
- TDesign 的 `:deep()` 覆写有时需要加更高优先级选择器
- `position: absolute` 的 LoadingOverlay 需要父容器有 `position: relative`
- CSS 变量需要在 style.css 的 `:root` 中定义后，才能在 App.vue 中引用
