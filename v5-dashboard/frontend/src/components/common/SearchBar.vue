<template>
  <div class="search-bar">
    <t-input
      v-model="searchValue"
      :placeholder="placeholder"
      clearable
      @change="handleSearch"
      @clear="handleClear"
    >
      <template #prefix-icon>
        <t-icon name="search" />
      </template>
    </t-input>
    <div v-if="$slots.filters" class="search-filters">
      <slot name="filters" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
}>()

const searchValue = ref(props.modelValue || '')

watch(
  () => props.modelValue,
  val => {
    searchValue.value = val || ''
  }
)

const handleSearch = (value: string) => {
  emit('update:modelValue', value)
  emit('search', value)
}

const handleClear = () => {
  searchValue.value = ''
  emit('update:modelValue', '')
  emit('search', '')
}
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.search-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

:deep(.t-input) {
  width: 320px;
}
</style>
