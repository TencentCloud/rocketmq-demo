<script setup lang="ts">
import { useAppStore } from './stores/app'
import MainLayout from './layouts/MainLayout.vue'
import LoadingOverlay from './components/common/LoadingOverlay.vue'

const appStore = useAppStore()
</script>

<template>
  <MainLayout>
    <router-view />
  </MainLayout>
  <LoadingOverlay :visible="appStore.loading" />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --primary-gradient: linear-gradient(135deg, #0052d9 0%, #0066ff 100%);
  --card-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  --card-shadow-hover: 0 4px 16px rgba(0, 82, 217, 0.12);
}

#app {
  font-family: var(--font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 标题使用等宽字体增强技术感 */
h1, h2, h3, h4, h5, h6 {
  font-family: var(--font-mono);
  font-weight: 600;
  letter-spacing: -0.02em;
}

/* 卡片标题、按钮文字等使用等宽字体 */
.card-title,
.t-card__header-title,
.t-button,
.t-table__th {
  font-family: var(--font-mono);
  font-weight: 500;
}

/* TDesign 卡片增强 */
:deep(.t-card) {
  border-radius: 8px;
  box-shadow: var(--card-shadow);
  border: 1px solid rgba(0, 82, 217, 0.06);
  transition: all 0.2s ease;
}

:deep(.t-card:hover) {
  box-shadow: var(--card-shadow-hover);
  border-color: rgba(0, 82, 217, 0.12);
}

:deep(.t-card__body) {
  padding: 20px;
}

:deep(.t-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

/* TDesign 按钮增强 */
:deep(.t-button) {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.t-button--theme-primary) {
  background: var(--primary-gradient);
  border: none;
}

:deep(.t-button--theme-primary:hover) {
  background: linear-gradient(135deg, #0047c0 0%, #0052d9 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 82, 217, 0.3);
}

:deep(.t-button--theme-default) {
  border-color: rgba(0, 82, 217, 0.2);
  color: #0052d9;
}

:deep(.t-button--theme-default:hover) {
  border-color: rgba(0, 82, 217, 0.4);
  background-color: rgba(0, 82, 217, 0.04);
}

/* TDesign 表格增强 */
:deep(.t-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.t-table__thead tr) {
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.03) 0%, rgba(0, 102, 255, 0.03) 100%);
}

:deep(.t-table__th) {
  background: transparent;
  font-weight: 600;
  color: #0052d9;
  letter-spacing: 0.02em;
  border-bottom: 2px solid rgba(0, 82, 217, 0.08);
}

:deep(.t-table__body tr:hover) {
  background-color: rgba(0, 82, 217, 0.03);
}

:deep(.t-table__body tr:nth-child(even)) {
  background-color: rgba(0, 0, 0, 0.015);
}

:deep(.t-table__body tr:nth-child(even):hover) {
  background-color: rgba(0, 82, 217, 0.03);
}

/* TDesign 标签增强 */
:deep(.t-tag) {
  border-radius: 4px;
  font-weight: 500;
  padding: 2px 8px;
  font-size: 12px;
}

:deep(.t-tag--theme-success) {
  background: rgba(41, 204, 133, 0.1);
  color: #29cc85;
  border: 1px solid rgba(41, 204, 133, 0.2);
}

:deep(.t-tag--theme-warning) {
  background: rgba(227, 115, 24, 0.1);
  color: #e37318;
  border: 1px solid rgba(227, 115, 24, 0.2);
}

:deep(.t-tag--theme-danger) {
  background: rgba(235, 47, 150, 0.1);
  color: #eb2f96;
  border: 1px solid rgba(235, 47, 150, 0.2);
}

/* TDesign 输入框增强 */
:deep(.t-input__inner) {
  border-radius: 6px;
  border-color: rgba(0, 82, 217, 0.2);
  transition: all 0.2s ease;
}

:deep(.t-input__inner:hover) {
  border-color: rgba(0, 82, 217, 0.4);
}

:deep(.t-input__inner:focus) {
  border-color: #0052d9;
  box-shadow: 0 0 0 2px rgba(0, 82, 217, 0.1);
}
</style>
