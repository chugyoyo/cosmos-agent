<template>
  <div :class="cardClasses" @click="handleClick">
    <div v-if="$slots.header || title" class="card-header">
      <slot name="header">
        <h3 v-if="title" class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </slot>
    </div>
    
    <div v-if="$slots.default" class="card-body">
      <slot />
    </div>
    
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Card title
  title: {
    type: String,
    default: ''
  },
  // Card subtitle
  subtitle: {
    type: String,
    default: ''
  },
  // Card variants
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'elevated', 'outlined', 'filled'].includes(value)
  },
  // Card sizes
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // Interactive card
  interactive: {
    type: Boolean,
    default: false
  },
  // Hoverable card
  hoverable: {
    type: Boolean,
    default: false
  },
  // Loading state
  loading: {
    type: Boolean,
    default: false
  },
  // Custom classes
  customClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const cardClasses = computed(() => {
  const classes = [
    'base-card',
    `base-card--${props.variant}`,
    `base-card--${props.size}`,
    {
      'base-card--interactive': props.interactive,
      'base-card--hoverable': props.hoverable,
      'base-card--loading': props.loading
    }
  ]
  
  if (props.customClass) {
    classes.push(props.customClass)
  }
  
  return classes
})

const handleClick = (event) => {
  if (props.interactive && !props.loading) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
.base-card {
  @include card-base;
  position: relative;
  overflow: hidden;
  
  // Size variants
  &--sm {
    padding: var(--spacing-4);
    
    .card-header {
      margin-bottom: var(--spacing-3);
    }
    
    .card-body {
      margin-bottom: var(--spacing-3);
    }
    
    .card-footer {
      margin-top: var(--spacing-3);
    }
  }
  
  &--md {
    padding: var(--spacing-6);
    
    .card-header {
      margin-bottom: var(--spacing-4);
    }
    
    .card-body {
      margin-bottom: var(--spacing-4);
    }
    
    .card-footer {
      margin-top: var(--spacing-4);
    }
  }
  
  &--lg {
    padding: var(--spacing-8);
    
    .card-header {
      margin-bottom: var(--spacing-6);
    }
    
    .card-body {
      margin-bottom: var(--spacing-6);
    }
    
    .card-footer {
      margin-top: var(--spacing-6);
    }
  }
  
  // Variant styles
  &--default {
    background: var(--surface-color);
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
  }
  
  &--elevated {
    background: var(--surface-color);
    border: none;
    box-shadow: var(--shadow-lg);
  }
  
  &--outlined {
    background: transparent;
    border: 2px solid var(--border-color);
    box-shadow: none;
  }
  
  &--filled {
    background: var(--background-secondary);
    border: none;
    box-shadow: none;
  }
  
  // State modifiers
  &--interactive {
    cursor: pointer;
    user-select: none;
    
    &:hover {
      @include card-hover;
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: var(--shadow-sm);
    }
  }
  
  &--hoverable {
    &:hover {
      @include card-hover;
    }
  }
  
  &--loading {
    position: relative;
    pointer-events: none;
    
    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1;
    }
  }
  
  // Card sections
  .card-header {
    .card-title {
      margin: 0;
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
      line-height: var(--line-height-tight);
    }
    
    .card-subtitle {
      margin: var(--spacing-1) 0 0 0;
      font-size: var(--font-size-sm);
      color: var(--text-secondary);
      line-height: var(--line-height-normal);
    }
  }
  
  .card-body {
    color: var(--text-primary);
    line-height: var(--line-height-normal);
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .card-footer {
    border-top: 1px solid var(--border-light);
    padding-top: var(--spacing-4);
    color: var(--text-secondary);
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  // Focus styles for interactive cards
  &--interactive:focus {
    @include focus-ring;
    outline: none;
  }
}

// Dark mode adjustments
[data-theme="dark"] {
  .base-card {
    &--outlined {
      border-color: var(--border-color);
    }
    
    &--filled {
      background: var(--surface-secondary);
    }
    
    &--loading::after {
      background: rgba(0, 0, 0, 0.8);
    }
  }
}
</style>
