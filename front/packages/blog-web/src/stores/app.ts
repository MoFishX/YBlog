import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarOpen = ref(false)

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function closeSidebar() {
    sidebarOpen.value = false
  }

  return { sidebarOpen, toggleSidebar, closeSidebar }
})
