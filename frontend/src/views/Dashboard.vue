<template>
  <div class="page-container">
    <div class="page-header">
      <h2>首页</h2>
      <p class="description">欢迎使用智宙 AI Agent 平台</p>
    </div>
    
    <div class="dashboard-content">
      <!-- 快捷入口 -->
      <div class="quick-access">
        <h3>快捷入口</h3>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in quickAccessItems" :key="item.key">
            <el-card class="quick-access-card" @click="navigateTo(item.route)">
              <div class="card-content">
                <div class="card-icon" :style="{ background: item.color }">
                  <el-icon size="32"><component :is="item.icon" /></el-icon>
                </div>
                <div class="card-info">
                  <div class="card-title">{{ item.title }}</div>
                  <div class="card-desc">{{ item.description }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 系统状态 -->
      <div class="system-status">
        <h3>系统状态</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card>
              <div class="status-item">
                <div class="status-indicator online"></div>
                <div class="status-info">
                  <div class="status-title">后端服务</div>
                  <div class="status-desc">运行正常</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div class="status-item">
                <div class="status-indicator online"></div>
                <div class="status-info">
                  <div class="status-title">数据库连接</div>
                  <div class="status-desc">连接正常</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div class="status-item">
                <div class="status-indicator warning"></div>
                <div class="status-info">
                  <div class="status-title">AI 模型</div>
                  <div class="status-desc">需要配置</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 最新活动 -->
      <div class="recent-activity">
        <h3>最新活动</h3>
        <el-card>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const quickAccessItems = [
  {
    key: 'settings',
    title: '设置',
    description: '配置 AI 模型',
    icon: 'Setting',
    color: 'linear-gradient(45deg, #1677ff, #722ed1)',
    route: '/settings'
  },
  {
    key: 'agents',
    title: '智能工程师',
    description: '管理 AI Agent',
    icon: 'Robot',
    color: 'linear-gradient(45deg, #52c41a, #73d13d)',
    route: '/agents'
  },
  {
    key: 'testing',
    title: '智能测试',
    description: '自动化测试',
    icon: 'Document',
    color: 'linear-gradient(45deg, #fa8c16, #ffa940)',
    route: '/testing'
  },
  {
    key: 'sales',
    title: '智能销售',
    description: '销售助手',
    icon: 'ShoppingCart',
    color: 'linear-gradient(45deg, #eb2f96, #f759ab)',
    route: '/sales'
  }
]

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

const navigateTo = (route: string) => {
  router.push(route)
}
</script>

<style scoped lang="scss">
.dashboard-content {
  .quick-access {
    margin-bottom: 32px;
    
    h3 {
      margin-bottom: 20px;
      color: #333;
    }
    
    .quick-access-card {
      cursor: pointer;
      transition: all 0.3s ease;
      border: none;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      }
      
      .card-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .card-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        }
        
        .card-info {
          flex: 1;
          
          .card-title {
            font-size: 18px;
            font-weight: bold;
            color: #333;
            margin-bottom: 4px;
          }
          
          .card-desc {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
  }
  
  .system-status {
    margin-bottom: 32px;
    
    h3 {
      margin-bottom: 20px;
      color: #333;
    }
    
    .status-item {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .status-indicator {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        
        &.online {
          background: #52c41a;
          box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
        }
        
        &.warning {
          background: #fa8c16;
          box-shadow: 0 0 0 3px rgba(250, 140, 22, 0.2);
        }
        
        &.error {
          background: #ff4d4f;
          box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.2);
        }
      }
      
      .status-info {
        .status-title {
          font-size: 16px;
          font-weight: bold;
          color: #333;
          margin-bottom: 2px;
        }
        
        .status-desc {
          font-size: 14px;
          color: #666;
        }
      }
    }
  }
  
  .recent-activity {
    h3 {
      margin-bottom: 20px;
      color: #333;
    }
  }
}
</style>