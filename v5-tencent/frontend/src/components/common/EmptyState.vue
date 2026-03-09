<template>
  <div class="empty-state">
    <t-card class="empty-card">
      <div class="empty-inner">
        <div class="empty-icon-wrapper">
          <t-icon name="inbox" size="40px" class="empty-icon" />
        </div>
        <p class="empty-title">{{ message || t('common.noData') }}</p>
        <t-button
          v-if="actionText"
          theme="primary"
          variant="outline"
          size="small"
          @click="handleAction"
        >
          <template #icon><t-icon name="add" /></template>
          {{ actionText }}
        </t-button>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
  padding: var(--gap-md) 0;
}

.empty-card {
  border: 1px dashed var(--color-border-default) !important;
  box-shadow: none !important;
  background-color: transparent !important;
}

.empty-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--gap-xl) var(--gap-lg);
  gap: var(--gap-md);
  text-align: center;
}

.empty-icon-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
  border-radius: 50%;
}

.empty-icon {
  color: var(--color-text-disabled);
}

.empty-title {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
  max-width: 320px;
  line-height: 1.6;
}
</style>
