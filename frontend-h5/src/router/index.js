import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/stations' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/stations', name: 'StationList', component: () => import('../views/StationList.vue') },
  { path: '/station/:id', name: 'StationDetail', component: () => import('../views/StationDetail.vue') },
  { path: '/start', name: 'StartCharge', component: () => import('../views/StartCharge.vue') },
  { path: '/charging', name: 'Charging', component: () => import('../views/Charging.vue') },
  { path: '/orders', name: 'OrderList', component: () => import('../views/OrderList.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (!token && to.name !== 'Login') {
    next('/login')
  } else {
    next()
  }
})

export default router
