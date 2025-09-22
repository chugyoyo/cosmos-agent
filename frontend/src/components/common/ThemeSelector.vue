<template>
  <el-dropdown 
    trigger="click" 
    placement="bottom-end"
    @command="handleThemeChange"
  >
    <button class="theme-selector-btn">
      <el-icon class="theme-icon">
        <component :is="getThemeIcon()" />
      </el-icon>
      <span class="theme-label">{{ themeLabel }}</span>
      <el-icon class="dropdown-icon">
        <ArrowDown />
      </el-icon>
    </button>
    
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item 
          v-for="option in themeOptions" 
          :key="option.value"
          :command="option.value"
          :class="{ 'is-active': currentTheme === option.value }"
        >
          <div class="theme-option">
            <el-icon class="option-icon">
              <component :is="option.icon" />
            </el-icon>
            <span class="option-label">{{ option.label }}</span>
            <el-icon v-if="currentTheme === option.value" class="check-icon">
              <Check />
            </el-icon>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { 
  Sunny, 
  Moon, 
  Monitor, 
  ArrowDown, 
  Check 
} from '@element-plus/icons-vue'

const { 
  currentTheme, 
  themeIcon, 
  themeLabel, 
  setTheme, 
  THEME_OPTIONS 
} = useTheme()

const themeOptions = [
  {
    value: THEME_OPTIONS.LIGHT,
    label: '浅色模式',
    icon: Sunny
  },
  {
    value: THEME_OPTIONS.DARK,
    label: '深色模式',
    icon: Moon
  },
  {
    value: THEME_OPTIONS.AUTO,
    label: '跟随系统',
    icon: Monitor
  }
]

const getThemeIcon = () => {
  switch (themeIcon.value) {
    case 'sunny':
      return Sunny
    case 'moon':
      return Moon
    case 'monitor':
    default:
      return Monitor
  }
}

const handleThemeChange = (theme) => {
  setTheme(theme)
}
</script>

<style lang="scss" scoped>
.theme-selector-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--surface-color);
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: var(--transition-all);
  
  &:hover {
    background: var(--background-secondary);
    border-color: var(--border-dark);
  }
  
  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px var(--primary-200);
  }
  
  .theme-icon {
    font-size: 16px;
    color: var(--text-primary);
  }
  
  .theme-label {
    font-weight: var(--font-weight-medium);
    white-space: nowrap;
  }
  
  .dropdown-icon {
    font-size: 12px;
    color: var(--text-tertiary);
    transition: var(--transition-fast);
  }
}

.theme-option {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  width: 100%;
  padding: var(--spacing-1) 0;
  
  .option-icon {
    font-size: 16px;
    color: var(--text-primary);
  }
  
  .option-label {
    flex: 1;
    font-size: var(--font-size-sm);
    color: var(--text-primary);
  }
  
  .check-icon {
    font-size: 14px;
    color: var(--primary-color);
  }
}

:deep(.el-dropdown-menu__item) {
  padding: var(--spacing-2) var(--spacing-3);
  
  &.is-active {
    background: var(--primary-50);
    color: var(--primary-color);
  }
  
  &:hover {
    background: var(--background-secondary);
  }
}

// Dark mode adjustments
[data-theme="dark"] {
  .theme-selector-btn {
    background: var(--surface-color);
    border-color: var(--border-color);
    
    &:hover {
      background: var(--surface-secondary);
      border-color: var(--border-dark);
    }
  }
}
</style>
