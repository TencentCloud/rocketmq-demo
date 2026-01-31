<script setup lang="ts">
import { useAppStore } from './stores/app'
import MainLayout from './layouts/MainLayout.vue'
import LoadingOverlay from './components/common/LoadingOverlay.vue'

const appStore = useAppStore()
</script>

<template>
  <MainLayout>
    <router-view />
  </MainLayout>
  <LoadingOverlay :visible="appStore.loading" />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
</style>
