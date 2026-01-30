<template>
  <div class="clusters-page">
    <PageHeader title="Cluster Management" description="Manage RocketMQ clusters" />
    <EmptyState message="No clusters configured yet" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style scoped>
.clusters-page {
  height: 100%;
}
</style>
