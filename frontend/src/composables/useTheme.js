import { ref, computed, watch, onMounted } from 'vue'

// Theme options
export const THEME_OPTIONS = {
  LIGHT: 'light',
  DARK: 'dark',
  AUTO: 'auto'
}

// Theme state
const currentTheme = ref(THEME_OPTIONS.AUTO)
const isDarkMode = ref(false)
const systemPrefersDark = ref(false)

// Initialize theme detection
const initializeTheme = () => {
  // Check system preference
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  systemPrefersDark.value = mediaQuery.matches
  
  // Listen for system theme changes
  const handleSystemThemeChange = (e) => {
    systemPrefersDark.value = e.matches
    updateTheme()
  }
  
  mediaQuery.addEventListener('change', handleSystemThemeChange)
  
  // Load saved theme preference
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme && Object.values(THEME_OPTIONS).includes(savedTheme)) {
    currentTheme.value = savedTheme
  }
  
  updateTheme()
}

// Update theme based on current preference
const updateTheme = () => {
  let shouldBeDark = false
  
  switch (currentTheme.value) {
    case THEME_OPTIONS.DARK:
      shouldBeDark = true
      break
    case THEME_OPTIONS.LIGHT:
      shouldBeDark = false
      break
    case THEME_OPTIONS.AUTO:
    default:
      shouldBeDark = systemPrefersDark.value
      break
  }
  
  isDarkMode.value = shouldBeDark
  
  // Apply theme to document
  if (shouldBeDark) {
    document.documentElement.setAttribute('data-theme', 'dark')
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.removeAttribute('data-theme')
    document.documentElement.classList.remove('dark-mode')
  }
  
  // Save theme preference
  localStorage.setItem('theme', currentTheme.value)
}

// Set theme
const setTheme = (theme) => {
  if (Object.values(THEME_OPTIONS).includes(theme)) {
    currentTheme.value = theme
  }
}

// Toggle between light and dark (ignoring auto)
const toggleTheme = () => {
  if (currentTheme.value === THEME_OPTIONS.AUTO) {
    // If currently auto, switch to opposite of system preference
    currentTheme.value = systemPrefersDark.value ? THEME_OPTIONS.LIGHT : THEME_OPTIONS.DARK
  } else {
    // Toggle between light and dark
    currentTheme.value = currentTheme.value === THEME_OPTIONS.LIGHT ? THEME_OPTIONS.DARK : THEME_OPTIONS.LIGHT
  }
}

// Get theme icon
const getThemeIcon = computed(() => {
  switch (currentTheme.value) {
    case THEME_OPTIONS.DARK:
      return 'moon'
    case THEME_OPTIONS.LIGHT:
      return 'sunny'
    case THEME_OPTIONS.AUTO:
    default:
      return 'monitor'
  }
})

// Get theme label
const getThemeLabel = computed(() => {
  switch (currentTheme.value) {
    case THEME_OPTIONS.DARK:
      return '深色模式'
    case THEME_OPTIONS.LIGHT:
      return '浅色模式'
    case THEME_OPTIONS.AUTO:
    default:
      return '跟随系统'
  }
})

// Watch for theme changes
watch(currentTheme, updateTheme)

// Initialize on mount
onMounted(() => {
  initializeTheme()
})

// Export composable
export function useTheme() {
  return {
    // State
    currentTheme: computed(() => currentTheme.value),
    isDarkMode: computed(() => isDarkMode.value),
    systemPrefersDark: computed(() => systemPrefersDark.value),
    
    // Computed
    themeIcon: getThemeIcon,
    themeLabel: getThemeLabel,
    
    // Methods
    setTheme,
    toggleTheme,
    updateTheme,
    
    // Constants
    THEME_OPTIONS
  }
}
