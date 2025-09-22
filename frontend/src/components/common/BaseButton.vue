<template>
  <component
    :is="tag"
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="handleClick"
  >
    <div v-if="loading" class="button-loading">
      <div class="loading-spinner"></div>
    </div>
    <el-icon v-else-if="icon" class="button-icon">
      <component :is="icon" />
    </el-icon>
    <span v-if="$slots.default" class="button-content">
      <slot />
    </span>
  </component>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Button variants
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'ghost', 'danger'].includes(value)
  },
  // Button sizes
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl'].includes(value)
  },
  // Button type for form submission
  type: {
    type: String,
    default: 'button',
    validator: (value) => ['button', 'submit', 'reset'].includes(value)
  },
  // HTML tag to render
  tag: {
    type: String,
    default: 'button'
  },
  // Disabled state
  disabled: {
    type: Boolean,
    default: false
  },
  // Loading state
  loading: {
    type: Boolean,
    default: false
  },
  // Icon component
  icon: {
    type: [String, Object],
    default: null
  },
  // Full width
  block: {
    type: Boolean,
    default: false
  },
  // Rounded corners
  rounded: {
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

const buttonClasses = computed(() => {
  const classes = [
    'base-button',
    `base-button--${props.variant}`,
    `base-button--${props.size}`,
    {
      'base-button--disabled': props.disabled,
      'base-button--loading': props.loading,
      'base-button--block': props.block,
      'base-button--rounded': props.rounded,
      'base-button--icon-only': !$slots.default && props.icon
    }
  ]
  
  if (props.customClass) {
    classes.push(props.customClass)
  }
  
  return classes
})

const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
.base-button {
  @include button-base;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  text-decoration: none;
  white-space: nowrap;
  vertical-align: middle;
  user-select: none;
  border: 1px solid transparent;
  transition: var(--transition-all);
  
  // Size variants
  &--xs {
    padding: var(--spacing-1) var(--spacing-2);
    font-size: var(--font-size-xs);
    line-height: var(--line-height-tight);
    min-height: 24px;
  }
  
  &--sm {
    padding: var(--spacing-2) var(--spacing-3);
    font-size: var(--font-size-sm);
    line-height: var(--line-height-tight);
    min-height: 32px;
  }
  
  &--md {
    padding: var(--spacing-3) var(--spacing-4);
    font-size: var(--font-size-sm);
    line-height: var(--line-height-normal);
    min-height: 40px;
  }
  
  &--lg {
    padding: var(--spacing-4) var(--spacing-6);
    font-size: var(--font-size-base);
    line-height: var(--line-height-normal);
    min-height: 48px;
  }
  
  &--xl {
    padding: var(--spacing-5) var(--spacing-8);
    font-size: var(--font-size-lg);
    line-height: var(--line-height-normal);
    min-height: 56px;
  }
  
  // Variant styles
  &--primary {
    @include button-primary;
  }
  
  &--secondary {
    @include button-secondary;
  }
  
  &--ghost {
    @include button-ghost;
  }
  
  &--danger {
    @include button-danger;
  }
  
  // State modifiers
  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
  
  &--loading {
    cursor: wait;
    pointer-events: none;
  }
  
  &--block {
    width: 100%;
  }
  
  &--rounded {
    border-radius: var(--radius-full);
  }
  
  &--icon-only {
    padding: var(--spacing-2);
    min-width: 40px;
    
    &.base-button--xs {
      padding: var(--spacing-1);
      min-width: 24px;
    }
    
    &.base-button--sm {
      padding: var(--spacing-1-5);
      min-width: 32px;
    }
    
    &.base-button--lg {
      padding: var(--spacing-3);
      min-width: 48px;
    }
    
    &.base-button--xl {
      padding: var(--spacing-4);
      min-width: 56px;
    }
  }
  
  // Icon styles
  .button-icon {
    font-size: 1em;
    line-height: 1;
  }
  
  .button-content {
    line-height: 1;
  }
  
  // Loading spinner
  .button-loading {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .loading-spinner {
    @include loading-spinner;
    width: 16px;
    height: 16px;
    border-width: 2px;
  }
  
  // Focus styles
  &:focus {
    @include focus-ring;
  }
  
  // Hover effects for interactive states
  &:not(.base-button--disabled):not(.base-button--loading):hover {
    transform: translateY(-1px);
  }
  
  &:not(.base-button--disabled):not(.base-button--loading):active {
    transform: translateY(0);
  }
}

// Dark mode adjustments
[data-theme="dark"] {
  .base-button {
    &--secondary {
      background: var(--surface-color);
      border-color: var(--border-color);
      color: var(--text-primary);
      
      &:hover:not(.base-button--disabled) {
        background: var(--background-secondary);
        border-color: var(--border-dark);
      }
    }
    
    &--ghost {
      color: var(--text-primary);
      
      &:hover:not(.base-button--disabled) {
        background: var(--background-secondary);
      }
    }
  }
}
</style>
