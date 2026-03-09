<template>
  <div class="dashboard-page">
    <PageHeader :title="t('dashboard.pageTitle')" :description="t('dashboard.pageDescription')" />

    <div class="dashboard-content">
      <!-- Welcome Card -->
      <t-card class="welcome-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="check-circle" class="header-icon" />
            <span class="header-title">{{ t('dashboard.welcomeTitle') }}</span>
          </div>
        </template>
        <div class="welcome-content">
          <p>{{ t('dashboard.welcomeContent1') }}</p>
          <p>{{ t('dashboard.welcomeContent2') }}</p>
        </div>
      </t-card>

      <!-- Quick Start Guide -->
      <t-card class="guide-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="book" class="header-icon" />
            <span class="header-title">{{ t('dashboard.quickStartTitle') }}</span>
          </div>
        </template>
        <div class="guide-content">
          <t-steps :current="0" layout="vertical">
            <t-step-item
              :title="t('dashboard.step1Title')"
              :content="t('dashboard.step1Content')"
            />
            <t-step-item
              :title="t('dashboard.step2Title')"
              :content="t('dashboard.step2Content')"
            />
            <t-step-item
              :title="t('dashboard.step3Title')"
              :content="t('dashboard.step3Content')"
            />
            <t-step-item
              :title="t('dashboard.step4Title')"
              :content="t('dashboard.step4Content')"
            />
          </t-steps>
        </div>
      </t-card>

      <!-- Feature Cards -->
      <t-row :gutter="16" class="feature-row">
        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/clusters')">
            <div class="feature-content">
              <t-icon name="server" class="feature-icon cluster-icon" />
              <div class="feature-info">
                <div class="feature-title">{{ t('dashboard.featureClusters') }}</div>
                <div class="feature-desc">{{ t('dashboard.featureClustersDesc') }}</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/topics')">
            <div class="feature-content">
              <t-icon name="layers" class="feature-icon topic-icon" />
              <div class="feature-info">
                <div class="feature-title">{{ t('dashboard.featureTopics') }}</div>
                <div class="feature-desc">{{ t('dashboard.featureTopicsDesc') }}</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/groups')">
            <div class="feature-content">
              <t-icon name="usergroup" class="feature-icon group-icon" />
              <div class="feature-info">
                <div class="feature-title">{{ t('dashboard.featureGroups') }}</div>
                <div class="feature-desc">{{ t('dashboard.featureGroupsDesc') }}</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/messages')">
            <div class="feature-content">
              <t-icon name="mail" class="feature-icon message-icon" />
              <div class="feature-info">
                <div class="feature-title">{{ t('dashboard.featureMessages') }}</div>
                <div class="feature-desc">{{ t('dashboard.featureMessagesDesc') }}</div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <!-- API Limitations -->
      <t-card class="limitations-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="info-circle" class="header-icon" />
            <span class="header-title">{{ t('dashboard.limitationsTitle') }}</span>
          </div>
        </template>
        <div class="limitations-content">
          <t-alert
            theme="warning"
            :message="t('dashboard.limitationsAlert')"
            style="margin-bottom: 16px"
          >
            <template #default>
              <ul class="limitation-list">
                <li>
                  <strong>{{ t('dashboard.limitation1').split(':')[0] }}:</strong>
                  {{ t('dashboard.limitation1').split(':')[1] }}
                </li>
                <li>
                  <strong>{{ t('dashboard.limitation2').split(':')[0] }}:</strong>
                  {{ t('dashboard.limitation2').split(':')[1] }}
                </li>
                <li>
                  <strong>{{ t('dashboard.limitation3').split(':')[0] }}:</strong>
                  {{ t('dashboard.limitation3').split(':')[1] }}
                </li>
              </ul>
            </template>
          </t-alert>
        </div>
      </t-card>

      <!-- Documentation Links -->
      <t-card class="docs-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="link" class="header-icon" />
            <span class="header-title">{{ t('dashboard.docsTitle') }}</span>
          </div>
        </template>
        <div class="docs-content">
          <t-list :split="true">
            <t-list-item>
              <t-list-item-meta
                :title="t('dashboard.docsItem1Title')"
                :description="t('dashboard.docsItem1Desc')"
              >
                <template #image>
                  <t-icon name="file-text" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link
                  theme="primary"
                  href="https://cloud.tencent.com/document/product/1493"
                  target="_blank"
                >
                  {{ t('dashboard.viewDocs') }}
                </t-link>
              </template>
            </t-list-item>
            <t-list-item>
              <t-list-item-meta
                :title="t('dashboard.docsItem2Title')"
                :description="t('dashboard.docsItem2Desc')"
              >
                <template #image>
                  <t-icon name="internet" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link theme="primary" href="https://rocketmq.apache.org/" target="_blank">
                  {{ t('dashboard.visitSite') }}
                </t-link>
              </template>
            </t-list-item>
            <t-list-item>
              <t-list-item-meta
                :title="t('dashboard.docsItem3Title')"
                :description="t('dashboard.docsItem3Desc')"
              >
                <template #image>
                  <t-icon name="code" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link
                  theme="primary"
                  href="https://cloud.tencent.com/document/api/1493"
                  target="_blank"
                >
                  {{ t('dashboard.apiDocs') }}
                </t-link>
              </template>
            </t-list-item>
          </t-list>
        </div>
      </t-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const { t } = useI18n()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  padding: var(--gap-lg);
  overflow-y: auto;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: var(--gap-md);
}

/* ---- Card Headers ---- */
.card-header {
  display: flex;
  align-items: center;
  gap: var(--gap-sm);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.header-icon {
  font-size: 18px;
  color: var(--color-primary);
}

/* ---- Welcome ---- */
.welcome-content p {
  margin: 6px 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

/* ---- Guide ---- */
.guide-content {
  padding: var(--gap-sm) 0;
}

/* ---- Feature Cards ---- */
.feature-row {
  margin: 0;
}

.feature-card {
  cursor: pointer;
  transition: var(--transition-spring);
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 82, 217, 0.12);
  border-color: var(--color-primary-border) !important;
}

.feature-content {
  display: flex;
  align-items: center;
  gap: var(--gap-md);
  padding: var(--gap-sm) 0;
}

.feature-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.cluster-icon {
  color: var(--color-primary);
  background-color: rgba(0, 82, 217, 0.08);
}

.topic-icon {
  color: var(--color-success);
  background-color: rgba(0, 168, 112, 0.08);
}

.group-icon {
  color: var(--color-warning);
  background-color: rgba(227, 115, 24, 0.08);
}

.message-icon {
  color: var(--color-danger);
  background-color: rgba(213, 73, 65, 0.08);
}

.feature-info {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* ---- Limitations ---- */
.limitations-content {
  padding: var(--gap-xs) 0;
}

.limitation-list {
  margin: var(--gap-sm) 0 0 0;
  padding-left: var(--gap-lg);
  list-style: disc;
}

.limitation-list li {
  margin: var(--gap-xs) 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

/* ---- Docs ---- */
.docs-content :deep(.t-list-item__meta-avatar) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--color-primary-light-1);
  border-radius: var(--radius-md);
}

.docs-content :deep(.t-list-item__meta-avatar .t-icon) {
  color: var(--color-primary);
}
</style>
