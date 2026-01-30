<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h1 class="sidebar-title">RocketMQ</h1>
    </div>
    <t-menu v-model="activeMenu" theme="light" :collapsed="false" @change="handleMenuChange">
      <t-menu-item value="dashboard" @click="navigateTo('/dashboard')">
        <template #icon>
          <t-icon name="dashboard" />
        </template>
        Dashboard
      </t-menu-item>
      <t-menu-item value="clusters" @click="navigateTo('/clusters')">
        <template #icon>
          <t-icon name="server" />
        </template>
        Clusters
      </t-menu-item>
      <t-menu-item value="topics" @click="navigateTo('/topics')">
        <template #icon>
          <t-icon name="layers" />
        </template>
        Topics
      </t-menu-item>
      <t-menu-item value="groups" @click="navigateTo('/groups')">
        <template #icon>
          <t-icon name="usergroup" />
        </template>
        Groups
      </t-menu-item>
      <t-menu-item value="messages" @click="navigateTo('/messages')">
        <template #icon>
          <t-icon name="mail" />
        </template>
        Messages
      </t-menu-item>
      <t-menu-item value="roles" @click="navigateTo('/roles')">
        <template #icon>
          <t-icon name="user" />
        </template>
        Roles
      </t-menu-item>
    </t-menu>
  </aside>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('dashboard')

watch(
  () => route.path,
  path => {
    const menuName = path.split('/')[1] || 'dashboard'
    activeMenu.value = menuName
  },
  { immediate: true }
)

const handleMenuChange = (value: string) => {
  activeMenu.value = value
}

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  background-color: #fff;
  border-right: 1px solid #e7e7e7;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px 16px;
  border-bottom: 1px solid #e7e7e7;
}

.sidebar-title {
  font-size: 20px;
  font-weight: 600;
  color: #000;
  margin: 0;
}

:deep(.t-menu) {
  border: none;
  flex: 1;
}

:deep(.t-menu-item) {
  margin: 4px 8px;
  border-radius: 4px;
}

:deep(.t-menu-item.t-is-active) {
  background-color: var(--td-brand-color-1);
  color: var(--td-brand-color);
}
</style>
