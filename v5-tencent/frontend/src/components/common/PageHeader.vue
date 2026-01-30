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
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 16px 24px;
  background-color: #fff;
  border-radius: 4px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #000;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
