import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: {
      title: 'Dashboard',
      icon: 'dashboard'
    }
  },
  {
    path: '/clusters',
    name: 'Clusters',
    component: () => import('@/views/clusters/Index.vue'),
    meta: {
      title: 'Cluster Management',
      icon: 'server'
    }
  },
  {
    path: '/topics',
    name: 'Topics',
    component: () => import('@/views/topics/Index.vue'),
    meta: {
      title: 'Topic Management',
      icon: 'layers'
    }
  },
  {
    path: '/groups',
    name: 'Groups',
    component: () => import('@/views/groups/Index.vue'),
    meta: {
      title: 'Consumer Group Management',
      icon: 'usergroup'
    }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/messages/Index.vue'),
    meta: {
      title: 'Message Management',
      icon: 'mail'
    }
  },
  {
    path: '/roles',
    name: 'Roles',
    component: () => import('@/views/roles/Index.vue'),
    meta: {
      title: 'Role Management',
      icon: 'user'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - RocketMQ Dashboard`
  }

  next()
})

export default router
