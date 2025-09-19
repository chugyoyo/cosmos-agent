<template>
  <div class="page-container">
    <div class="page-header">
      <h2>首页</h2>
      <p class="description">欢迎使用智宙 AI Agent 平台</p>
    </div>
    
    <div class="dashboard-content">
      <!-- 统计卡片 -->
      <div class="stats-section">
        <div class="section-header">
          <h3>数据概览</h3>
        </div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in statsData" :key="stat.key">
            <StatsCard
              :icon="stat.icon"
              :value="stat.value"
              :label="stat.label"
              :color="stat.color"
              :trend="stat.trend"
            />
          </el-col>
        </el-row>
      </div>
      
      <!-- 快捷入口 -->
      <div class="quick-access">
        <div class="section-header">
          <h3>快捷入口</h3>
        </div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in quickAccessItems" :key="item.key">
            <QuickAccessCard
              :icon="item.icon"
              :title="item.title"
              :description="item.description"
              :color="item.color"
              :route="item.route"
              :disabled="item.disabled"
              @click="handleQuickAccess(item)"
            />
          </el-col>
        </el-row>
      </div>
      
      <!-- 系统状态 -->
      <div class="system-status">
        <div class="section-header">
          <h3>系统状态</h3>
        </div>
        <el-row :gutter="20">
          <el-col :span="8" v-for="status in systemStatus" :key="status.key">
            <el-card class="enhanced-card">
              <StatusIndicator
                :status="status.status"
                :text="status.title"
                :description="status.description"
              />
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 最新活动 -->
      <div class="recent-activity">
        <div class="section-header">
          <h3>最新活动</h3>
        </div>
        <el-card class="enhanced-card">
          <el-timeline>
            <el-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :timestamp="activity.timestamp"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatsCard from '@/components/StatsCard.vue'
import QuickAccessCard from '@/components/QuickAccessCard.vue'
import StatusIndicator from '@/components/StatusIndicator.vue'

const router = useRouter()

// 统计数据
const statsData = [
  {
    key: 'agents',
    icon: 'Robot',
    value: '12',
    label: '智能工程师',
    color: 'primary',
    trend: { type: 'up' as const, value: 12 }
  },
  {
    key: 'deployed',
    icon: 'Cpu',
    value: '8',
    label: '已部署',
    color: 'success',
    trend: { type: 'up' as const, value: 5 }
  },
  {
    key: 'models',
    icon: 'MagicStick',
    value: '3',
    label: 'AI 模型',
    color: 'warning',
    trend: { type: 'up' as const, value: 1 }
  },
  {
    key: 'calls',
    icon: 'Phone',
    value: '1,234',
    label: '调用次数',
    color: 'secondary',
    trend: { type: 'up' as const, value: 23 }
  }
]

// 快捷入口
const quickAccessItems = [
  {
    key: 'settings',
    title: '设置',
    description: '配置 AI 模型',
    icon: 'Setting',
    color: 'primary',
    route: '/settings'
  },
  {
    key: 'agents',
    title: '智能工程师',
    description: '管理 AI Agent',
    icon: 'Robot',
    color: 'success',
    route: '/agents'
  },
  {
    key: 'testing',
    title: '智能测试',
    description: '自动化测试',
    icon: 'Document',
    color: 'warning',
    route: '/testing',
    disabled: true
  },
  {
    key: 'sales',
    title: '智能销售',
    description: '销售助手',
    icon: 'ShoppingCart',
    color: 'error',
    route: '/sales',
    disabled: true
  }
]

// 系统状态
const systemStatus = [
  {
    key: 'backend',
    title: '后端服务',
    description: '运行正常',
    status: 'online' as const
  },
  {
    key: 'database',
    title: '数据库连接',
    description: '连接正常',
    status: 'online' as const
  },
  {
    key: 'ai-model',
    title: 'AI 模型',
    description: '需要配置',
    status: 'warning' as const
  }
]

// 最新活动
const recentActivities = [
  {
    id: 1,
    content: '系统启动成功',
    timestamp: '2024-01-20 10:00:00',
    type: 'success'
  },
  {
    id: 2,
    content: '数据库连接建立',
    timestamp: '2024-01-20 10:00:01',
    type: 'success'
  },
  {
    id: 3,
    content: '欢迎使用智宙平台',
    timestamp: '2024-01-20 10:00:02',
    type: 'info'
  }
]

const handleQuickAccess = (item: any) => {
  if (item.disabled) {
    ElMessage({
      message: `${item.title} 功能正在开发中`,
      type: 'warning'
    })
    return
  }
  
  if (item.route) {
    router.push(item.route)
  }
}
</script>

<style scoped lang="scss">
.dashboard-content {
  .stats-section {
    margin-bottom: var(--spacing-2xl);
  }
  
  .quick-access {
    margin-bottom: var(--spacing-2xl);
  }
  
  .system-status {
    margin-bottom: var(--spacing-2xl);
  }
  
  .recent-activity {
    :deep(.el-timeline-item__node) {
      transition: all var(--transition-fast);
      
      &:hover {
        transform: scale(1.2);
      }
    }
    
    :deep(.el-timeline-item__content) {
      transition: all var(--transition-fast);
      
      &:hover {
        transform: translateX(4px);
      }
    }
  }
}
</style>