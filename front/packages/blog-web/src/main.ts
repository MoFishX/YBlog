import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './styles/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const userStore = useUserStore()
userStore.restore().then(() => {
  app.mount('#app')
})
