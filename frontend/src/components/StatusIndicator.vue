<template>
  <div class="status-indicator" :class="status">
    <div class="indicator-dot" :class="status"></div>
    <div class="indicator-text">{{ text }}</div>
    <div class="indicator-description" v-if="description">{{ description }}</div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  status: 'online' | 'offline' | 'warning' | 'error'
  text: string
  description?: string
}

defineProps<Props>()
</script>

<style scoped lang="scss">
.status-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  background: var(--surface-color);
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: var(--shadow-sm);
  }
  
  .indicator-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    position: relative;
    animation: pulse 2s infinite;
    
    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 20px;
      height: 20px;
      border-radius: 50%;
      animation: ripple 2s infinite;
    }
    
    &.online {
      background: var(--success-color);
      
      &::before {
        background: rgba(82, 196, 26, 0.2);
      }
    }
    
    &.offline {
      background: var(--text-tertiary);
      
      &::before {
        background: rgba(153, 153, 153, 0.2);
      }
    }
    
    &.warning {
      background: var(--warning-color);
      
      &::before {
        background: rgba(250, 140, 22, 0.2);
      }
    }
    
    &.error {
      background: var(--error-color);
      
      &::before {
        background: rgba(255, 77, 79, 0.2);
      }
    }
  }
  
  .indicator-text {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    color: var(--text-color);
  }
  
  .indicator-description {
    font-size: var(--font-size-xs);
    color: var(--text-secondary);
    margin-left: var(--spacing-xs);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.6;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}
</style>