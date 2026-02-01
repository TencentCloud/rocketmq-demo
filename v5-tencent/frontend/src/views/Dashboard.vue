<template>
  <div class="dashboard-page">
    <PageHeader title="RocketMQ Dashboard" description="Tencent Cloud RocketMQ 5.x Management Console" />

    <div class="dashboard-content">
      <!-- Welcome Card -->
      <t-card class="welcome-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="check-circle" class="header-icon" />
            <span class="header-title">Welcome to RocketMQ Dashboard</span>
          </div>
        </template>
        <div class="welcome-content">
          <p>This is a modern web-based management console for Tencent Cloud RocketMQ 5.x.</p>
          <p>You can manage clusters, topics, consumer groups, messages, and more through this dashboard.</p>
        </div>
      </t-card>

      <!-- Quick Start Guide -->
      <t-card class="guide-card" hover-shadow>
        <template #header>
          <div class="card-header">
            <t-icon name="book" class="header-icon" />
            <span class="header-title">Quick Start Guide</span>
          </div>
        </template>
        <div class="guide-content">
          <t-steps :current="0" layout="vertical">
            <t-step-item title="Configure Cluster" content="Go to 'Config' page to add your Tencent Cloud RocketMQ cluster information." />
            <t-step-item title="Manage Topics" content="Navigate to 'Topics' page to create, view, and manage topics." />
            <t-step-item title="Manage Consumer Groups" content="Go to 'Groups' page to manage consumer groups and monitor consumption lag." />
            <t-step-item title="Query Messages" content="Use 'Messages' page to query message details by message ID." />
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
                <div class="feature-title">Clusters</div>
                <div class="feature-desc">View and manage RocketMQ clusters</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/topics')">
            <div class="feature-content">
              <t-icon name="layers" class="feature-icon topic-icon" />
              <div class="feature-info">
                <div class="feature-title">Topics</div>
                <div class="feature-desc">Create and manage message topics</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/groups')">
            <div class="feature-content">
              <t-icon name="usergroup" class="feature-icon group-icon" />
              <div class="feature-info">
                <div class="feature-title">Consumer Groups</div>
                <div class="feature-desc">Manage consumer groups and monitor lag</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :span="6">
          <t-card class="feature-card" hover-shadow @click="navigateTo('/messages')">
            <div class="feature-content">
              <t-icon name="mail" class="feature-icon message-icon" />
              <div class="feature-info">
                <div class="feature-title">Messages</div>
                <div class="feature-desc">Query and trace message details</div>
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
            <span class="header-title">API Limitations</span>
          </div>
        </template>
        <div class="limitations-content">
          <t-alert theme="warning" message="Important Notes" style="margin-bottom: 16px;">
            <template #default>
              <ul class="limitation-list">
                <li><strong>Message Query:</strong> Currently only supports querying by message ID. Time-based queries require using Tencent Cloud Console.</li>
                <li><strong>Cluster Management:</strong> Cluster creation and deletion should be done through Tencent Cloud Console.</li>
                <li><strong>Message Sending:</strong> For production use, please use official RocketMQ client SDKs instead of dashboard.</li>
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
            <span class="header-title">Documentation & Resources</span>
          </div>
        </template>
        <div class="docs-content">
          <t-list :split="true">
            <t-list-item>
              <t-list-item-meta 
                title="Tencent Cloud RocketMQ Documentation" 
                description="Official documentation for Tencent Cloud RocketMQ 5.x"
              >
                <template #image>
                  <t-icon name="file-text" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link theme="primary" href="https://cloud.tencent.com/document/product/1493" target="_blank">
                  View Docs
                </t-link>
              </template>
            </t-list-item>
            <t-list-item>
              <t-list-item-meta 
                title="RocketMQ Official Website" 
                description="Apache RocketMQ official documentation and guides"
              >
                <template #image>
                  <t-icon name="internet" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link theme="primary" href="https://rocketmq.apache.org/" target="_blank">
                  Visit Site
                </t-link>
              </template>
            </t-list-item>
            <t-list-item>
              <t-list-item-meta 
                title="API Reference" 
                description="Tencent Cloud RocketMQ API documentation"
              >
                <template #image>
                  <t-icon name="code" size="24px" />
                </template>
              </t-list-item-meta>
              <template #action>
                <t-link theme="primary" href="https://cloud.tencent.com/document/api/1493" target="_blank">
                  API Docs
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
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  padding: 24px;
  width: 100%;
  overflow-y: auto;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 16px;
}

/* Card Headers */
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  font-size: 20px;
  color: #0052d9;
}

.header-title {
  color: #000;
}

/* Welcome Card */
.welcome-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
}

.welcome-content {
  padding: 8px 0;
}

.welcome-content p {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

/* Guide Card */
.guide-card {
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border-radius: 12px;
}

.guide-content {
  padding: 16px 0;
}

/* Feature Cards */
.feature-row {
  margin: 0;
}

.feature-card {
  height: 140px;
  cursor: pointer;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(0, 82, 217, 0.08);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 82, 217, 0.15);
  border-color: rgba(0, 82, 217, 0.2);
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  height: 100%;
  text-align: center;
}

.feature-icon {
  font-size: 48px;
  padding: 12px;
  border-radius: 12px;
}

.cluster-icon {
  color: #0052d9;
  background: linear-gradient(135deg, rgba(0, 82, 217, 0.1) 0%, rgba(0, 102, 255, 0.1) 100%);
}

.topic-icon {
  color: #29cc85;
  background: linear-gradient(135deg, rgba(41, 204, 133, 0.1) 0%, rgba(41, 204, 133, 0.08) 100%);
}

.group-icon {
  color: #e37318;
  background: linear-gradient(135deg, rgba(227, 115, 24, 0.1) 0%, rgba(227, 115, 24, 0.08) 100%);
}

.message-icon {
  color: #eb2f96;
  background: linear-gradient(135deg, rgba(235, 47, 150, 0.1) 0%, rgba(235, 47, 150, 0.08) 100%);
}

.feature-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #000;
}

.feature-desc {
  font-size: 13px;
  color: #666;
}

/* Limitations Card */
.limitations-card {
  background: linear-gradient(135deg, #fffbf0 0%, #fff9e6 100%);
  border-radius: 12px;
}

.limitations-content {
  padding: 8px 0;
}

.limitation-list {
  margin: 0;
  padding-left: 20px;
  list-style: disc;
}

.limitation-list li {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.limitation-list strong {
  color: #333;
}

/* Documentation Card */
.docs-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
}

.docs-content {
  padding: 8px 0;
}

/* Responsive */
@media (min-width: 1920px) {
  .dashboard-page {
    padding: 32px;
  }

  .feature-card {
    height: 160px;
  }

  .feature-icon {
    font-size: 56px;
  }

  .feature-title {
    font-size: 18px;
  }
}

@media (min-width: 2560px) {
  .dashboard-page {
    padding: 40px;
  }

  .feature-card {
    height: 180px;
  }

  .feature-icon {
    font-size: 64px;
  }

  .feature-title {
    font-size: 20px;
  }

  .feature-desc {
    font-size: 14px;
  }
}
</style>
