<template>
  <div v-if="visible" class="loading-overlay">
    <t-loading size="large" text="Loading..." />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
}>()
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
</style>
