<template>
  <div class="dashboard">
    <t-layout>
      <t-header>
        <t-head-menu theme="dark" :value="activeMenu">
          <template #logo>
            <span class="logo">RocketMQ Dashboard</span>
          </template>
          <t-menu-item value="overview">Overview</t-menu-item>
          <t-menu-item value="clusters">Clusters</t-menu-item>
          <t-menu-item value="topics">Topics</t-menu-item>
          <t-menu-item value="consumers">Consumers</t-menu-item>
        </t-head-menu>
      </t-header>
      <t-content>
        <div class="content-wrapper">
          <t-card title="Dashboard" hover-shadow>
            <p>Welcome to RocketMQ Dashboard</p>
            <p>Start monitoring your RocketMQ clusters here.</p>
          </t-card>
        </div>
      </t-content>
    </t-layout>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeMenu = ref('overview')
</script>

<style scoped>
.dashboard {
  height: 100vh;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: white;
  padding: 0 16px;
}

.content-wrapper {
  padding: 24px;
}
</style>
