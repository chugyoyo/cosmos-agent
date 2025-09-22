<template>
  <div id="app">
    <el-container class="layout-container">
      <el-aside :width="isCollapsed ? '64px' : '240px'" class="sidebar" :class="{ 'collapsed': isCollapsed }">
        <div class="logo">
          <h1 :class="{ 'collapsed-title': isCollapsed }">智宙</h1>
          <p v-show="!isCollapsed">AI Agent Platform</p>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
          :collapse="isCollapsed"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>设置</template>
          </el-menu-item>
          <el-menu-item index="/agent">
            <el-icon><Connection /></el-icon>
            <template #title>Agent管理</template>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>智能聊天</template>
          </el-menu-item>
        </el-menu>
        <div class="collapse-toggle" @click="toggleSidebar">
          <el-icon :class="{ 'rotated': isCollapsed }">
            <ArrowLeft />
          </el-icon>
        </div>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { House, Setting, Connection, ChatDotRound, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const isDarkMode = ref(false)
const isCollapsed = ref(true) // 默认压缩状态

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

// Initialize theme and sidebar state on mount
onMounted(() => {
  initializeTheme()
  const savedSidebarState = localStorage.getItem('sidebarCollapsed')
  if (savedSidebarState !== null) {
    isCollapsed.value = savedSidebarState === 'true'
  } else {
    isCollapsed.value = true // 默认压缩
  }
})

// Toggle sidebar
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarCollapsed', isCollapsed.value.toString())
}

const currentPage = computed(() => {
  const routeMap: Record<string, string> = {
    '/': '首页',
    '/settings': '设置',
    '/testing': '智能测试',
    '/agent': 'Agent管理',
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
    position: relative;
    overflow: visible;
    
    &.collapsed {
      .logo {
        h1.collapsed-title {
          font-size: var(--font-size-md);
        }
      }
    }
    
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
    
    .collapse-toggle {
      position: absolute;
      right: -12px;
      bottom: 20px;
      width: 24px;
      height: 24px;
      background: var(--primary-color);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all var(--transition-fast);
      border: 2px solid var(--surface-color);
      box-shadow: var(--shadow-md);
      z-index: 10;
      
      &:hover {
        background: var(--secondary-color);
        transform: scale(1.1);
      }
      
      .el-icon {
        color: white;
        font-size: 12px;
        transition: transform var(--transition-fast);
        
        &.rotated {
          transform: rotate(180deg);
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