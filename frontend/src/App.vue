<template>
  <div id="app" :class="{ 'dark-mode': isDarkMode }">
    <div class="app-layout">
      <!-- 移动端遮罩层 -->
      <div 
        v-if="isMobile && !isCollapsed" 
        class="mobile-overlay" 
        :class="{ active: !isCollapsed }"
        @click="toggleSidebar"
      ></div>
      
      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ 
        'collapsed': isCollapsed, 
        'mobile-open': isMobile && !isCollapsed 
      }">
        <div class="sidebar-header" v-if="!isCollapsed">
          <div class="logo">
            <div class="logo-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="logo-text">
              <h1>智宙</h1>
              <p>AI Agent Platform</p>
            </div>
          </div>
          <button class="collapse-btn" @click="toggleSidebar">
            <el-icon>
              <ArrowLeft />
            </el-icon>
          </button>
        </div>
        
        <nav class="sidebar-nav">
          <div class="nav-section">
            <div class="nav-title" v-show="!isCollapsed">主要功能</div>
            <div class="nav-items">
              <router-link to="/" class="nav-item" :class="{ 'active': $route.path === '/' }">
                <el-icon><House /></el-icon>
                <span v-show="!isCollapsed">首页</span>
              </router-link>
              <router-link to="/agent" class="nav-item" :class="{ 'active': $route.path.startsWith('/agent') }">
                <el-icon><Connection /></el-icon>
                <span v-show="!isCollapsed">Agent管理</span>
              </router-link>
              <router-link to="/chat" class="nav-item" :class="{ 'active': $route.path.startsWith('/chat') }">
                <el-icon><ChatDotRound /></el-icon>
                <span v-show="!isCollapsed">智能聊天</span>
              </router-link>
            </div>
          </div>
          
          <div class="nav-section">
            <div class="nav-title" v-show="!isCollapsed">系统设置</div>
            <div class="nav-items">
              <router-link to="/settings" class="nav-item" :class="{ 'active': $route.path.startsWith('/settings') }">
                <el-icon><Setting /></el-icon>
                <span v-show="!isCollapsed">设置</span>
              </router-link>
            </div>
          </div>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info" v-show="!isCollapsed">
            <div class="user-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div class="user-details">
              <div class="user-name">管理员</div>
              <div class="user-role">系统管理员</div>
            </div>
          </div>
        </div>
        
        <!-- 折叠状态下的展开按钮 - 显示在最下方 -->
        <div class="sidebar-expand-btn" v-if="isCollapsed" @click="toggleSidebar">
          <el-icon>
            <ArrowLeft />
          </el-icon>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="main-layout" :class="{ 'collapsed': isCollapsed }">
        <!-- 顶部导航栏 -->
        <header class="top-header">
          <div class="header-left">
            <!-- 移动端菜单按钮 -->
            <button 
              v-if="isMobile" 
              class="mobile-menu-btn"
              @click="toggleSidebar"
            >
              <el-icon><Menu /></el-icon>
            </button>
            
            <div class="breadcrumb">
              <span class="breadcrumb-item">{{ currentPage }}</span>
            </div>
          </div>
          <div class="header-right">
            <div class="header-actions">
              <ThemeSelector />
              <el-tooltip content="通知" placement="bottom">
                <button class="action-btn">
                  <el-icon><Bell /></el-icon>
                  <span class="notification-badge">3</span>
                </button>
              </el-tooltip>
              <el-dropdown trigger="click" class="user-dropdown">
                <button class="user-menu-btn">
                  <div class="user-avatar-small">
                    <el-icon><User /></el-icon>
                  </div>
                  <span class="user-name-small">管理员</span>
                  <el-icon><ArrowDown /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人设置</el-dropdown-item>
                    <el-dropdown-item>系统设置</el-dropdown-item>
                    <el-dropdown-item divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </header>

        <!-- 页面内容 -->
        <div class="page-content">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import ThemeSelector from '@/components/common/ThemeSelector.vue'
import { 
  House, 
  Setting, 
  Connection, 
  ChatDotRound, 
  ArrowLeft, 
  User, 
  Bell, 
  ArrowDown,
  Menu
} from '@element-plus/icons-vue'

const route = useRoute()
const { isDarkMode } = useTheme()
const isCollapsed = ref(false) // 默认展开状态
const isMobile = ref(false)

// Mobile detection
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 767
  // Auto-collapse sidebar on mobile
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

// Initialize sidebar state on mount
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
  const savedSidebarState = localStorage.getItem('sidebarCollapsed')
  if (savedSidebarState !== null) {
    isCollapsed.value = savedSidebarState === 'true'
  } else {
    isCollapsed.value = false // 默认展开
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
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
// 全局样式重置
#app {
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  background: var(--background-color);
  color: var(--text-color);
  transition: all var(--transition-normal);
}

// 主布局容器
.app-layout {
  display: flex;
  height: 100vh;
  background: var(--background-color);
}

// 侧边栏样式
.sidebar {
  width: 280px;
  background: var(--surface-color);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: all var(--transition-normal);
  position: relative;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  
  &.collapsed {
    width: 64px;
    
    .logo-text {
      display: none;
    }
    
    .nav-title {
      display: none;
    }
    
    .user-info {
      display: none;
    }
  }
}

// 侧边栏头部
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .logo-icon {
      width: 40px;
      height: 40px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
    }
    
    .logo-text {
      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
        line-height: 1.2;
      }
      
      p {
        margin: 0;
        font-size: 12px;
        opacity: 0.8;
        font-weight: 500;
      }
    }
  }
  
  .collapse-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    border-radius: 8px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all var(--transition-fast);
    color: white;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.05);
    }
    
    .el-icon {
      font-size: 16px;
      transition: transform var(--transition-fast);
      
      &.rotated {
        transform: rotate(180deg);
      }
    }
  }
}

// 侧边栏导航
.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  
  .nav-section {
    margin-bottom: 24px;
    
    .nav-title {
      padding: 0 20px 12px;
      font-size: 12px;
      font-weight: 600;
      color: var(--text-tertiary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .nav-items {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
  }
}

// 导航项样式
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  position: relative;
  font-weight: 500;
  font-size: 14px;
  
  &:hover {
    background: var(--sidebar-hover);
    color: var(--text-color);
    transform: translateX(4px);
  }
  
  &.active {
    background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
    color: white;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 60%;
      background: white;
      border-radius: 0 4px 4px 0;
    }
  }
  
  .el-icon {
    font-size: 18px;
    transition: transform var(--transition-fast);
  }
  
  &:hover .el-icon {
    transform: scale(1.1);
  }
}

// 侧边栏底部
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid var(--border-light);
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-avatar {
      width: 40px;
      height: 40px;
      background: var(--primary-color);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
    }
    
    .user-details {
      .user-name {
        font-weight: 600;
        font-size: 14px;
        color: var(--text-color);
        margin-bottom: 2px;
      }
      
      .user-role {
        font-size: 12px;
        color: var(--text-tertiary);
      }
    }
  }
}

// 折叠状态下的展开按钮
.sidebar-expand-btn {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
  
  &:hover {
    background: var(--primary-dark);
    transform: translateX(-50%) scale(1.1);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.4);
  }
  
  .el-icon {
    font-size: 16px;
    transform: rotate(180deg);
  }
}

// 主布局区域
.main-layout {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--background-color);
  margin-left: 0;
  width: calc(100% - 280px);
  transition: width var(--transition-normal);
  
  &.collapsed {
    width: calc(100% - 64px);
  }
}

// 顶部导航栏
.top-header {
  height: 64px;
  background: var(--surface-color);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 50;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: var(--spacing-3);
    
    .mobile-menu-btn {
      display: none;
      padding: var(--spacing-2);
      border: none;
      background: transparent;
      color: var(--text-color);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: var(--transition-fast);
      
      &:hover {
        background: var(--background-secondary);
      }
      
      .el-icon {
        font-size: 18px;
      }
    }
    
    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .breadcrumb-item {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-color);
      }
    }
  }
  
  .header-right {
    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
}

// 操作按钮
.action-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: var(--background-color);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
  
  &:hover {
    background: var(--primary-color);
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }
  
  .el-icon {
    font-size: 18px;
  }
  
  .notification-badge {
    position: absolute;
    top: -2px;
    right: -2px;
    background: var(--error-color);
    color: white;
    font-size: 10px;
    font-weight: 600;
    padding: 2px 6px;
    border-radius: 10px;
    min-width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

// 用户下拉菜单
.user-dropdown {
  .user-menu-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--background-color);
    border: 1px solid var(--border-color);
    border-radius: 8px;
    cursor: pointer;
    transition: all var(--transition-fast);
    
    &:hover {
      background: var(--primary-color);
      color: white;
      border-color: var(--primary-color);
    }
    
    .user-avatar-small {
      width: 32px;
      height: 32px;
      background: var(--primary-color);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 16px;
    }
    
    .user-name-small {
      font-weight: 500;
      font-size: 14px;
    }
    
    .el-icon {
      font-size: 12px;
      transition: transform var(--transition-fast);
    }
  }
}

// 页面内容区域
.page-content {
  flex: 1;
  overflow: auto;
  background: var(--background-color);
  padding: 24px;
}

// 暗色模式适配
.dark-mode {
  .sidebar {
    background: #1a1a1a;
    border-right-color: #333;
    
    .sidebar-header {
      background: linear-gradient(135deg, #2d2d2d, #1a1a1a);
    }
  }
  
  .top-header {
    background: #1a1a1a;
    border-bottom-color: #333;
  }
  
  .action-btn {
    background: #2d2d2d;
    color: #ccc;
    
    &:hover {
      background: var(--primary-color);
      color: white;
    }
  }
  
  .user-menu-btn {
    background: #2d2d2d;
    border-color: #333;
    color: #ccc;
    
    &:hover {
      background: var(--primary-color);
      color: white;
    }
  }
}

// 响应式设计
// 大屏幕 (桌面)
@media (min-width: 1200px) {
  .sidebar {
    width: 280px;
  }
  
  .main-layout {
    width: calc(100% - 280px);
  }
  
  .page-content {
    padding: var(--spacing-8);
  }
}

// 中等屏幕 (平板)
@media (min-width: 768px) and (max-width: 1199px) {
  .sidebar {
    width: 240px;
  }
  
  .main-layout {
    width: calc(100% - 240px);
  }
  
  .page-content {
    padding: var(--spacing-6);
  }
  
  .sidebar.collapsed {
    width: 64px;
  }
  
  .main-layout.collapsed {
    width: calc(100% - 64px);
  }
}

// 小屏幕 (手机)
@media (max-width: 767px) {
  .app-layout {
    flex-direction: column;
  }
  
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    width: 280px;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform var(--transition-normal);
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
    
    &.mobile-open {
      transform: translateX(0);
    }
    
    &.collapsed {
      width: 280px;
      transform: translateX(-100%);
    }
  }
  
  .main-layout {
    width: 100%;
  }
  
  .top-header {
    padding: var(--spacing-3) var(--spacing-4);
    
    .header-left {
      .mobile-menu-btn {
        display: flex;
      }
      
      .breadcrumb {
        display: none;
      }
    }
    
    .header-right {
      .header-actions {
        gap: var(--spacing-2);
      }
      
      .user-dropdown {
        .user-info {
          display: none;
        }
      }
    }
  }
  
  .page-content {
    padding: var(--spacing-4);
  }
  
  // 移动端遮罩层
  .mobile-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
    opacity: 0;
    visibility: hidden;
    transition: all var(--transition-normal);
    
    &.active {
      opacity: 1;
      visibility: visible;
    }
  }
}

// 超小屏幕 (小手机)
@media (max-width: 480px) {
  .top-header {
    padding: var(--spacing-2) var(--spacing-3);
    
    .header-left {
      .mobile-menu-btn {
        margin-right: var(--spacing-2);
      }
    }
    
    .header-right {
      .header-actions {
        gap: var(--spacing-1);
        
        .action-btn {
          padding: var(--spacing-1);
          min-width: 32px;
          height: 32px;
        }
      }
    }
  }
  
  .page-content {
    padding: var(--spacing-3);
  }
  
  .sidebar {
    width: 100%;
    max-width: 320px;
  }
}

// 横屏模式 (手机横屏)
@media (max-width: 767px) and (orientation: landscape) {
  .sidebar {
    width: 240px;
  }
  
  .top-header {
    padding: var(--spacing-2) var(--spacing-3);
  }
  
  .page-content {
    padding: var(--spacing-3);
  }
}

// 高分辨率屏幕
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .sidebar {
    border-right-width: 0.5px;
  }
  
  .top-header {
    border-bottom-width: 0.5px;
  }
}

// 打印样式
@media print {
  .sidebar,
  .top-header,
  .mobile-overlay {
    display: none !important;
  }
  
  .main-layout {
    width: 100% !important;
  }
  
  .page-content {
    padding: 0 !important;
  }
}

// 滚动条样式
.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}
</style>