import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import MainLayout from '../views/MainLayout.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: 'home', name: 'MapPage', component: () => import('../views/MapPage.vue') },
      { path: 'profile', name: 'ProfilePage', component: () => import('../views/ProfilePage.vue') }
    ]
  },
  { path: '/scan', name: 'ScanPage', component: () => import('../views/ScanPage.vue') },
  { path: '/search', name: 'SearchPage', component: () => import('../views/SearchPage.vue') },
  { path: '/coupons', name: 'CouponPage', component: () => import('../views/CouponPage.vue') },
  { path: '/recharge', name: 'RechargePage', component: () => import('../views/RechargePage.vue') },
  { path: '/station/:id', name: 'StationDetail', component: () => import('../views/StationDetail.vue') },
  { path: '/charge', name: 'Charging', component: () => import('../views/Charging.vue') },
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
