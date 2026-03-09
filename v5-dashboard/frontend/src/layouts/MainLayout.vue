<template>
  <div class="main-layout">
    <Sidebar />
    <div class="layout-content">
      <Header />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: var(--color-bg-page);
}

.layout-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: var(--color-bg-page);
}
</style>
