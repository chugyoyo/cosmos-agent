<template>
  <div class="quick-access-card enhanced-card" @click="handleClick">
    <div class="card-content">
      <div class="card-icon" :style="{ background: iconGradient }">
        <el-icon size="32">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="card-info">
        <div class="card-title">{{ title }}</div>
        <div class="card-desc">{{ description }}</div>
      </div>
      <div class="card-arrow">
        <el-icon>
          <ArrowRight />
        </el-icon>
      </div>
    </div>
    <div class="card-footer" v-if="footer">
      {{ footer }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  icon: string
  title: string
  description: string
  color?: string
  route?: string
  footer?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  color: 'primary',
  disabled: false
})

const router = useRouter()
const emit = defineEmits(['click'])

const iconGradient = computed(() => {
  const gradients = {
    primary: 'linear-gradient(45deg, var(--primary-color), var(--secondary-color))',
    secondary: 'linear-gradient(45deg, var(--secondary-color), var(--secondary-light))',
    success: 'linear-gradient(45deg, var(--success-color), var(--success-light))',
    warning: 'linear-gradient(45deg, var(--warning-color), var(--warning-light))',
    error: 'linear-gradient(45deg, var(--error-color), var(--error-light))'
  }
  return gradients[props.color as keyof typeof gradients] || gradients.primary
})

const handleClick = () => {
  if (props.disabled) return
  
  if (props.route) {
    router.push(props.route)
  }
  
  emit('click')
}
</script>

<style scoped lang="scss">
.quick-access-card {
  cursor: pointer;
  user-select: none;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: var(--primary-color);
    transform: scaleX(0);
    transition: transform var(--transition-normal);
    transform-origin: left;
  }
  
  &:hover {
    &::before {
      transform: scaleX(1);
    }
    
    .card-arrow {
      transform: translateX(4px);
    }
    
    .card-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
  
  &:active {
    transform: translateY(0);
  }
  
  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;
    
    &:hover {
      transform: none;
      
      .card-arrow {
        transform: none;
      }
      
      .card-icon {
        transform: none;
      }
    }
  }
  
  .card-content {
    display: flex;
    align-items: center;
    gap: var(--spacing-lg);
    position: relative;
    z-index: 1;
  }
  
  .card-icon {
    width: 64px;
    height: 64px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    transition: transform var(--transition-normal);
    flex-shrink: 0;
  }
  
  .card-info {
    flex: 1;
    min-width: 0;
    
    .card-title {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-bold);
      color: var(--text-color);
      margin-bottom: var(--spacing-xs);
      line-height: var(--line-height-tight);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .card-desc {
      font-size: var(--font-size-sm);
      color: var(--text-secondary);
      line-height: var(--line-height-normal);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .card-arrow {
    color: var(--text-tertiary);
    transition: transform var(--transition-normal);
    flex-shrink: 0;
  }
  
  .card-footer {
    margin-top: var(--spacing-md);
    padding-top: var(--spacing-md);
    border-top: 1px solid var(--border-light);
    font-size: var(--font-size-xs);
    color: var(--text-tertiary);
    text-align: center;
  }
}
</style>