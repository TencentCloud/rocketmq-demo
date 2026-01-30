<template>
  <div class="empty-state">
    <t-icon name="inbox" size="64px" class="empty-icon" />
    <p class="empty-text">{{ message }}</p>
    <t-button v-if="actionText" theme="primary" @click="handleAction">
      {{ actionText }}
    </t-button>
  </div>
</template>

<script setup lang="ts">
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}

.empty-icon {
  color: #ddd;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px 0;
}
</style>
