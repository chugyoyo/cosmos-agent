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
          <el-menu-item index="/orchestration">
            <el-icon><Connection /></el-icon>
            <span>Agent编排</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能聊天</span>
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
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isDarkMode = ref(false)

// Initialize theme from localStorage or system preference
const initializeTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    isDarkMode.value = savedTheme === 'dark'
  } else {
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  updateTheme()
}

// Update theme
const updateTheme = () => {
  if (isDarkMode.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
    localStorage.setItem('theme', 'light')
  }
}

// Watch for theme changes
watch(isDarkMode, updateTheme)

// Initialize theme on mount
initializeTheme()

const currentPage = computed(() => {
  const routeMap: Record<string, string> = {
    '/dashboard': '首页',
    '/settings': '设置',
    '/testing': '智能测试',
    '/orchestration': 'Agent编排',
    '/chat': '智能聊天'
  }
  return routeMap[route.path] || '未知页面'
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  
  .sidebar {
    background: var(--sidebar-bg);
    border-right: 1px solid var(--sidebar-border);
    transition: all var(--transition-normal);
    
    .logo {
      padding: var(--spacing-lg);
      text-align: center;
      color: white;
      border-bottom: 1px solid var(--sidebar-border);
      
      h1 {
        margin: 0;
        font-size: var(--font-size-xl);
        font-weight: var(--font-weight-bold);
        background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        transition: all var(--transition-normal);
      }
      
      p {
        margin: var(--spacing-sm) 0 0;
        font-size: var(--font-size-xs);
        opacity: 0.7;
        transition: opacity var(--transition-normal);
      }
      
      &:hover h1 {
        transform: scale(1.05);
      }
    }
    
    .sidebar-menu {
      border: none;
      background: transparent;
      
      :deep(.el-menu-item) {
        color: var(--sidebar-text);
        transition: all var(--transition-fast);
        border-radius: var(--radius-sm);
        margin: var(--spacing-xs) var(--spacing-sm);
        
        &:hover {
          background: var(--sidebar-hover);
          color: var(--sidebar-text-active);
          transform: translateX(4px);
        }
        
        &.is-active {
          background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
          color: var(--sidebar-text-active);
          box-shadow: var(--shadow-md);
          
          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 70%;
            background: var(--surface-color);
            border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
          }
        }
        
        .el-icon {
          transition: transform var(--transition-fast);
        }
        
        &:hover .el-icon {
          transform: scale(1.1);
        }
      }
    }
  }
  
  .header {
    background: var(--surface-color);
    border-bottom: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 var(--spacing-lg);
    transition: all var(--transition-normal);
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
    }
    
    .theme-switch {
      :deep(.el-switch__core) {
        transition: all var(--transition-fast);
        
        &:hover {
          transform: scale(1.05);
        }
      }
    }
  }
  
  .main-content {
    background: var(--background-color);
    padding: var(--spacing-lg);
    transition: all var(--transition-normal);
  }
}
</style>