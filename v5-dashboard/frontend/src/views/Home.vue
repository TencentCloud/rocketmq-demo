<template>
  <div class="home">
    <t-space direction="vertical" size="large">
      <t-card title="Welcome to RocketMQ Dashboard" hover-shadow>
        <p>This is a modern dashboard for monitoring and managing RocketMQ.</p>
        <template #actions>
          <t-button theme="primary" @click="goToDashboard">Go to Dashboard</t-button>
        </template>
      </t-card>
    </t-space>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.home {
  padding: 24px;
}
</style>
