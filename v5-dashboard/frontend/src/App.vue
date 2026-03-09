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
