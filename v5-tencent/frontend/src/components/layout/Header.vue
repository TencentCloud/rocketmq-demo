<template>
  <header class="app-header">
    <div class="header-left">
      <h2 class="header-title">{{ title }}</h2>
    </div>
    <div class="header-right">
      <t-button theme="default" variant="text" @click="handleSettings">
        <template #icon>
          <t-icon name="setting" />
        </template>
        Configuration
      </t-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => {
  return (route.meta.title as string) || 'Dashboard'
})

const handleSettings = () => {
  console.log('Open settings')
}
</script>

<style scoped>
.app-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid #e7e7e7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #000;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
