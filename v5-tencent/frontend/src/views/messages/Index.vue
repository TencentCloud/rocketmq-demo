<template>
  <div class="messages-page">
    <PageHeader title="Message Management" description="Query and send messages" />
    <EmptyState message="No messages to display" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>

<style scoped>
.messages-page {
  height: 100%;
}
</style>
