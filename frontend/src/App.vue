<template>
  <div id="app">
    <el-container class="layout-container">
      <el-aside width="240px" class="sidebar">
        <div class="logo">
          <h1>智宙</h1>
          <p>AI Agent Platform</p>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
          <el-menu-item index="/agents">
            <el-icon><Robot /></el-icon>
            <span>智能工程师</span>
          </el-menu-item>
          <el-menu-item index="/testing" disabled>
            <el-icon><Document /></el-icon>
            <span>智能测试</span>
          </el-menu-item>
          <el-menu-item index="/sales" disabled>
            <el-icon><ShoppingCart /></el-icon>
            <span>智能销售</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>智宙</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-switch
              v-model="isDarkMode"
              class="theme-switch"
              active-text="暗色"
              inactive-text="亮色"
            />
          </div>
        </el-header>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isDarkMode = ref(false)

const currentPage = computed(() => {
  const routeMap: Record<string, string> = {
    '/dashboard': '首页',
    '/settings': '设置',
    '/agents': '智能工程师',
    '/testing': '智能测试',
    '/sales': '智能销售'
  }
  return routeMap[route.path] || '未知页面'
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  
  .sidebar {
    background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
    border-right: 1px solid #2d2d4a;
    
    .logo {
      padding: 24px;
      text-align: center;
      color: white;
      border-bottom: 1px solid #2d2d4a;
      
      h1 {
        margin: 0;
        font-size: 24px;
        background: linear-gradient(45deg, #1677ff, #722ed1);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
      
      p {
        margin: 8px 0 0;
        font-size: 12px;
        opacity: 0.7;
      }
    }
    
    .sidebar-menu {
      border: none;
      background: transparent;
      
      :deep(.el-menu-item) {
        color: rgba(255, 255, 255, 0.7);
        
        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: white;
        }
        
        &.is-active {
          background: linear-gradient(90deg, #1677ff, #722ed1);
          color: white;
        }
      }
    }
  }
  
  .header {
    background: white;
    border-bottom: 1px solid #e0e0e0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
  
  .main-content {
    background: #f5f5f5;
    padding: 24px;
  }
}
</style>