<template>
  <div class="stats-card enhanced-card">
    <div class="stats-content">
      <div class="stats-icon" :style="{ background: iconGradient }">
        <el-icon size="28">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="stats-info">
        <div class="stats-value">{{ value }}</div>
        <div class="stats-label">{{ label }}</div>
      </div>
    </div>
    <div class="stats-trend" v-if="trend">
      <el-icon :class="trend.type">
        <component :is="trend.type === 'up' ? 'ArrowUp' : 'ArrowDown'" />
      </el-icon>
      <span :class="trend.type">{{ trend.value }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  icon: string
  value: string | number
  label: string
  color?: string
  trend?: {
    type: 'up' | 'down'
    value: number
  }
}

const props = withDefaults(defineProps<Props>(), {
  color: 'primary'
})

const iconGradient = computed(() => {
  const gradients = {
    primary: 'linear-gradient(45deg, var(--primary-color), var(--primary-light))',
    secondary: 'linear-gradient(45deg, var(--secondary-color), var(--secondary-light))',
    success: 'linear-gradient(45deg, var(--success-color), var(--success-light))',
    warning: 'linear-gradient(45deg, var(--warning-color), var(--warning-light))',
    error: 'linear-gradient(45deg, var(--error-color), var(--error-light))'
  }
  return gradients[props.color as keyof typeof gradients] || gradients.primary
})
</script>

<style scoped lang="scss">
.stats-card {
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 100px;
    height: 100px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
    transform: translate(30px, -30px);
  }
  
  .stats-content {
    display: flex;
    align-items: center;
    gap: var(--spacing-lg);
    position: relative;
    z-index: 1;
  }
  
  .stats-icon {
    width: 64px;
    height: 64px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    transition: transform var(--transition-normal);
    
    &:hover {
      transform: scale(1.1) rotate(5deg);
    }
  }
  
  .stats-info {
    flex: 1;
    
    .stats-value {
      font-size: var(--font-size-xl);
      font-weight: var(--font-weight-bold);
      color: var(--text-color);
      margin-bottom: var(--spacing-xs);
      line-height: var(--line-height-tight);
    }
    
    .stats-label {
      font-size: var(--font-size-sm);
      color: var(--text-secondary);
      font-weight: var(--font-weight-medium);
    }
  }
  
  .stats-trend {
    position: absolute;
    top: var(--spacing-lg);
    right: var(--spacing-lg);
    display: flex;
    align-items: center;
    gap: var(--spacing-xs);
    font-size: var(--font-size-xs);
    font-weight: var(--font-weight-semibold);
    
    .el-icon {
      font-size: var(--font-size-sm);
    }
    
    .up {
      color: var(--success-color);
    }
    
    .down {
      color: var(--error-color);
    }
  }
}
</style>