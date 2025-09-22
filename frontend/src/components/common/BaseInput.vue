<template>
  <div class="base-input-wrapper" :class="wrapperClasses">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="input-required">*</span>
    </label>
    
    <div class="input-container" :class="containerClasses">
      <div v-if="$slots.prefix || prefixIcon" class="input-prefix">
        <slot name="prefix">
          <el-icon v-if="prefixIcon" class="input-icon">
            <component :is="prefixIcon" />
          </el-icon>
        </slot>
      </div>
      
      <input
        :id="inputId"
        ref="inputRef"
        v-model="inputValue"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :autocomplete="autocomplete"
        :maxlength="maxlength"
        :minlength="minlength"
        :min="min"
        :max="max"
        :step="step"
        :pattern="pattern"
        :class="inputClasses"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
        @keyup="handleKeyup"
      />
      
      <div v-if="$slots.suffix || suffixIcon || clearable" class="input-suffix">
        <el-icon
          v-if="clearable && inputValue && !disabled && !readonly"
          class="input-clear"
          @click="handleClear"
        >
          <Close />
        </el-icon>
        <slot name="suffix">
          <el-icon v-if="suffixIcon" class="input-icon">
            <component :is="suffixIcon" />
          </el-icon>
        </slot>
      </div>
    </div>
    
    <div v-if="hint || error || $slots.hint" class="input-message">
      <slot name="hint">
        <span v-if="hint && !error" class="input-hint">{{ hint }}</span>
        <span v-if="error" class="input-error">{{ error }}</span>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  // Input value
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // Input type
  type: {
    type: String,
    default: 'text'
  },
  // Input label
  label: {
    type: String,
    default: ''
  },
  // Input placeholder
  placeholder: {
    type: String,
    default: ''
  },
  // Input hint text
  hint: {
    type: String,
    default: ''
  },
  // Error message
  error: {
    type: String,
    default: ''
  },
  // Input sizes
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // Input variants
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'filled', 'outlined'].includes(value)
  },
  // Disabled state
  disabled: {
    type: Boolean,
    default: false
  },
  // Readonly state
  readonly: {
    type: Boolean,
    default: false
  },
  // Required field
  required: {
    type: Boolean,
    default: false
  },
  // Clearable input
  clearable: {
    type: Boolean,
    default: false
  },
  // Prefix icon
  prefixIcon: {
    type: [String, Object],
    default: null
  },
  // Suffix icon
  suffixIcon: {
    type: [String, Object],
    default: null
  },
  // HTML attributes
  autocomplete: {
    type: String,
    default: 'off'
  },
  maxlength: {
    type: Number,
    default: null
  },
  minlength: {
    type: Number,
    default: null
  },
  min: {
    type: Number,
    default: null
  },
  max: {
    type: Number,
    default: null
  },
  step: {
    type: Number,
    default: null
  },
  pattern: {
    type: String,
    default: null
  },
  // Custom classes
  customClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:modelValue',
  'input',
  'change',
  'focus',
  'blur',
  'keydown',
  'keyup',
  'clear'
])

const inputRef = ref(null)
const inputId = computed(() => `input-${Math.random().toString(36).substr(2, 9)}`)

const inputValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const wrapperClasses = computed(() => {
  const classes = [
    'base-input-wrapper',
    `base-input-wrapper--${props.size}`,
    {
      'base-input-wrapper--disabled': props.disabled,
      'base-input-wrapper--readonly': props.readonly,
      'base-input-wrapper--error': props.error,
      'base-input-wrapper--required': props.required
    }
  ]
  
  if (props.customClass) {
    classes.push(props.customClass)
  }
  
  return classes
})

const containerClasses = computed(() => {
  return [
    'input-container',
    `input-container--${props.variant}`,
    {
      'input-container--focused': isFocused.value,
      'input-container--disabled': props.disabled,
      'input-container--readonly': props.readonly,
      'input-container--error': props.error,
      'input-container--with-prefix': props.prefixIcon || $slots.prefix,
      'input-container--with-suffix': props.suffixIcon || props.clearable || $slots.suffix
    }
  ]
})

const inputClasses = computed(() => {
  return [
    'base-input',
    `base-input--${props.size}`,
    {
      'base-input--error': props.error
    }
  ]
})

const isFocused = ref(false)

const handleInput = (event) => {
  emit('input', event)
}

const handleChange = (event) => {
  emit('change', event)
}

const handleFocus = (event) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event) => {
  isFocused.value = false
  emit('blur', event)
}

const handleKeydown = (event) => {
  emit('keydown', event)
}

const handleKeyup = (event) => {
  emit('keyup', event)
}

const handleClear = () => {
  inputValue.value = ''
  emit('clear')
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// Expose methods for parent components
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  select: () => inputRef.value?.select()
})
</script>

<style lang="scss" scoped>
.base-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  
  // Size variants
  &--sm {
    .input-label {
      font-size: var(--font-size-sm);
    }
    
    .input-container {
      min-height: 32px;
    }
    
    .base-input {
      padding: var(--spacing-2) var(--spacing-3);
      font-size: var(--font-size-sm);
    }
  }
  
  &--md {
    .input-label {
      font-size: var(--font-size-sm);
    }
    
    .input-container {
      min-height: 40px;
    }
    
    .base-input {
      padding: var(--spacing-3) var(--spacing-4);
      font-size: var(--font-size-sm);
    }
  }
  
  &--lg {
    .input-label {
      font-size: var(--font-size-base);
    }
    
    .input-container {
      min-height: 48px;
    }
    
    .base-input {
      padding: var(--spacing-4) var(--spacing-5);
      font-size: var(--font-size-base);
    }
  }
  
  // State modifiers
  &--disabled {
    opacity: 0.6;
    pointer-events: none;
  }
  
  &--readonly {
    .input-container {
      background: var(--background-secondary);
    }
  }
  
  &--error {
    .input-container {
      border-color: var(--error-color);
      
      &:focus-within {
        box-shadow: 0 0 0 2px var(--error-100);
      }
    }
  }
  
  &--required {
    .input-required {
      color: var(--error-color);
      margin-left: var(--spacing-1);
    }
  }
}

.input-label {
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  line-height: var(--line-height-tight);
}

.input-required {
  color: var(--error-color);
}

.input-container {
  @include input-base;
  position: relative;
  display: flex;
  align-items: center;
  padding: 0;
  min-height: 40px;
  
  // Variant styles
  &--default {
    background: var(--surface-color);
    border: 1px solid var(--border-color);
  }
  
  &--filled {
    background: var(--background-secondary);
    border: 1px solid transparent;
    
    &:focus-within {
      background: var(--surface-color);
      border-color: var(--primary-color);
    }
  }
  
  &--outlined {
    background: transparent;
    border: 2px solid var(--border-color);
    
    &:focus-within {
      border-color: var(--primary-color);
    }
  }
  
  // State modifiers
  &--focused {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 2px var(--primary-100);
  }
  
  &--disabled {
    background: var(--background-secondary);
    color: var(--text-quaternary);
    cursor: not-allowed;
  }
  
  &--readonly {
    background: var(--background-secondary);
    cursor: default;
  }
  
  &--error {
    border-color: var(--error-color);
    
    &:focus-within {
      box-shadow: 0 0 0 2px var(--error-100);
    }
  }
  
  // Prefix and suffix spacing
  &--with-prefix {
    .base-input {
      padding-left: var(--spacing-2);
    }
  }
  
  &--with-suffix {
    .base-input {
      padding-right: var(--spacing-2);
    }
  }
}

.base-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-family: var(--font-family-primary);
  line-height: var(--line-height-normal);
  transition: var(--transition-all);
  
  &::placeholder {
    color: var(--text-quaternary);
  }
  
  &:disabled {
    cursor: not-allowed;
  }
  
  &:read-only {
    cursor: default;
  }
  
  &--error {
    color: var(--error-color);
  }
}

.input-prefix,
.input-suffix {
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.input-prefix {
  padding-left: var(--spacing-3);
}

.input-suffix {
  padding-right: var(--spacing-3);
  gap: var(--spacing-2);
}

.input-icon {
  font-size: 1em;
  line-height: 1;
}

.input-clear {
  cursor: pointer;
  color: var(--text-tertiary);
  transition: var(--transition-fast);
  
  &:hover {
    color: var(--text-secondary);
  }
}

.input-message {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  min-height: 20px;
}

.input-hint {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  line-height: var(--line-height-tight);
}

.input-error {
  font-size: var(--font-size-xs);
  color: var(--error-color);
  line-height: var(--line-height-tight);
}

// Dark mode adjustments
[data-theme="dark"] {
  .input-container {
    &--filled {
      background: var(--surface-secondary);
      
      &:focus-within {
        background: var(--surface-color);
      }
    }
  }
}
</style>
