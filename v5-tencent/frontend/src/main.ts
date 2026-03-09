import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import TDesign from 'tdesign-vue-next'
import 'tdesign-vue-next/es/style/index.css'
import i18n from './i18n'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(TDesign)
app.use(i18n)
app.mount('#app')
