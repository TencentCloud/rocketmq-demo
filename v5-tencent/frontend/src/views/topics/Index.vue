<template>
  <div class="topics-page">
    <PageHeader title="Topic Management" description="Manage RocketMQ topics" />
    <EmptyState message="No topics available" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style scoped>
.topics-page {
  height: 100%;
}
</style>
