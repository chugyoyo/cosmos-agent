<template>
  <teleport to="body">
    <transition name="modal" appear>
      <div
        v-if="modelValue"
        class="base-modal-overlay"
        @click="handleOverlayClick"
      >
        <div
          ref="modalRef"
          :class="modalClasses"
          role="dialog"
          :aria-modal="true"
          :aria-labelledby="titleId"
          :aria-describedby="contentId"
          @click.stop
        >
          <!-- Header -->
          <div v-if="showHeader" class="modal-header">
            <div class="modal-title-section">
              <h2 v-if="title" :id="titleId" class="modal-title">
                {{ title }}
              </h2>
              <p v-if="subtitle" class="modal-subtitle">
                {{ subtitle }}
              </p>
            </div>
            
            <div v-if="showClose" class="modal-actions">
              <el-button
                :icon="Close"
                circle
                size="small"
                variant="ghost"
                @click="handleClose"
              />
            </div>
          </div>
          
          <!-- Content -->
          <div :id="contentId" class="modal-content">
            <slot />
          </div>
          
          <!-- Footer -->
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  // Modal visibility
  modelValue: {
    type: Boolean,
    default: false
  },
  // Modal title
  title: {
    type: String,
    default: ''
  },
  // Modal subtitle
  subtitle: {
    type: String,
    default: ''
  },
  // Modal sizes
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl', 'full'].includes(value)
  },
  // Modal variants
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'centered', 'top', 'bottom'].includes(value)
  },
  // Show close button
  showClose: {
    type: Boolean,
    default: true
  },
  // Show header
  showHeader: {
    type: Boolean,
    default: true
  },
  // Close on overlay click
  closeOnOverlay: {
    type: Boolean,
    default: true
  },
  // Close on escape key
  closeOnEscape: {
    type: Boolean,
    default: true
  },
  // Prevent body scroll
  preventScroll: {
    type: Boolean,
    default: true
  },
  // Custom classes
  customClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'close', 'opened', 'closed'])

const modalRef = ref(null)
const titleId = computed(() => `modal-title-${Math.random().toString(36).substr(2, 9)}`)
const contentId = computed(() => `modal-content-${Math.random().toString(36).substr(2, 9)}`)

const modalClasses = computed(() => {
  const classes = [
    'base-modal',
    `base-modal--${props.size}`,
    `base-modal--${props.variant}`,
    {
      'base-modal--no-header': !props.showHeader
    }
  ]
  
  if (props.customClass) {
    classes.push(props.customClass)
  }
  
  return classes
})

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose()
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

const handleEscape = (event) => {
  if (event.key === 'Escape' && props.closeOnEscape) {
    handleClose()
  }
}

const lockBodyScroll = () => {
  if (props.preventScroll) {
    document.body.style.overflow = 'hidden'
  }
}

const unlockBodyScroll = () => {
  if (props.preventScroll) {
    document.body.style.overflow = ''
  }
}

// Watch for modal open/close
watch(() => props.modelValue, async (isOpen) => {
  if (isOpen) {
    lockBodyScroll()
    await nextTick()
    modalRef.value?.focus()
    emit('opened')
  } else {
    unlockBodyScroll()
    emit('closed')
  }
})

// Handle escape key
onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
  unlockBodyScroll()
})

// Focus management
const focusableElements = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'

const trapFocus = (event) => {
  if (!props.modelValue) return
  
  const modal = modalRef.value
  if (!modal) return
  
  const focusableContent = modal.querySelectorAll(focusableElements)
  const firstFocusableElement = focusableContent[0]
  const lastFocusableElement = focusableContent[focusableContent.length - 1]
  
  if (event.key === 'Tab') {
    if (event.shiftKey) {
      if (document.activeElement === firstFocusableElement) {
        lastFocusableElement?.focus()
        event.preventDefault()
      }
    } else {
      if (document.activeElement === lastFocusableElement) {
        firstFocusableElement?.focus()
        event.preventDefault()
      }
    }
  }
}

onMounted(() => {
  document.addEventListener('keydown', trapFocus)
})

onUnmounted(() => {
  document.removeEventListener('keydown', trapFocus)
})
</script>

<style lang="scss" scoped>
.base-modal-overlay {
  @include modal-overlay;
  z-index: var(--z-modal);
}

.base-modal {
  @include modal-content;
  position: relative;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  outline: none;
  
  // Size variants
  &--xs {
    width: 320px;
    max-width: 90vw;
  }
  
  &--sm {
    width: 480px;
    max-width: 90vw;
  }
  
  &--md {
    width: 640px;
    max-width: 90vw;
  }
  
  &--lg {
    width: 800px;
    max-width: 90vw;
  }
  
  &--xl {
    width: 1024px;
    max-width: 90vw;
  }
  
  &--full {
    width: 95vw;
    height: 95vh;
    max-width: none;
    max-height: none;
  }
  
  // Variant styles
  &--default {
    // Default centered modal
  }
  
  &--centered {
    // Explicitly centered (same as default)
  }
  
  &--top {
    margin-top: 5vh;
    margin-bottom: auto;
  }
  
  &--bottom {
    margin-top: auto;
    margin-bottom: 5vh;
  }
  
  // State modifiers
  &--no-header {
    .modal-content {
      border-top-left-radius: var(--radius-xl);
      border-top-right-radius: var(--radius-xl);
    }
  }
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--spacing-6) var(--spacing-6) var(--spacing-4);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.modal-title-section {
  flex: 1;
  min-width: 0;
}

.modal-title {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  line-height: var(--line-height-tight);
}

.modal-subtitle {
  margin: var(--spacing-1) 0 0 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-normal);
}

.modal-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-left: var(--spacing-4);
  flex-shrink: 0;
}

.modal-content {
  flex: 1;
  padding: var(--spacing-6);
  overflow-y: auto;
  @include custom-scrollbar;
  
  // Remove padding when no header
  .base-modal--no-header & {
    padding-top: var(--spacing-6);
  }
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--spacing-3);
  padding: var(--spacing-4) var(--spacing-6) var(--spacing-6);
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

// Modal transitions
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s var(--ease-out);
}

.modal-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

.modal-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

.modal-enter-active .base-modal,
.modal-leave-active .base-modal {
  transition: all 0.3s var(--ease-out);
}

.modal-enter-from .base-modal,
.modal-leave-to .base-modal {
  transform: scale(0.95) translateY(20px);
}

// Responsive adjustments
@media (max-width: 768px) {
  .base-modal {
    &--xs,
    &--sm,
    &--md,
    &--lg,
    &--xl {
      width: 95vw;
      max-width: none;
      margin: var(--spacing-4);
    }
    
    &--full {
      width: 100vw;
      height: 100vh;
      margin: 0;
      border-radius: 0;
    }
  }
  
  .modal-header {
    padding: var(--spacing-4) var(--spacing-4) var(--spacing-3);
  }
  
  .modal-content {
    padding: var(--spacing-4);
  }
  
  .modal-footer {
    padding: var(--spacing-3) var(--spacing-4) var(--spacing-4);
    flex-direction: column-reverse;
    
    .el-button {
      width: 100%;
    }
  }
}

// Dark mode adjustments
[data-theme="dark"] {
  .base-modal {
    background: var(--surface-color);
    border: 1px solid var(--border-color);
  }
  
  .modal-header,
  .modal-footer {
    border-color: var(--border-color);
  }
}
</style>
