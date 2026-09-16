import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import Vant from 'vant'
import 'vant/lib/index.css'
import 'leaflet/dist/leaflet.css'
import './styles/theme.css'

createApp(App).use(router).use(Vant).mount('#app')
