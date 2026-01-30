<template>
  <div class="roles-page">
    <PageHeader title="Role Management" description="Manage access roles" />
    <EmptyState message="No roles configured" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style scoped>
.roles-page {
  height: 100%;
}
</style>
