<template>
  <div class="groups-page">
    <PageHeader title="Consumer Group Management" description="Manage consumer groups" />
    <EmptyState message="No consumer groups available" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style scoped>
.groups-page {
  height: 100%;
}
</style>
