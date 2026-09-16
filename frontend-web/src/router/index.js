import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import Layout from '../views/Layout.vue'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据统计' } },
      { path: 'stations', name: 'StationManage', component: () => import('../views/StationManage.vue'), meta: { title: '充电站管理' } },
      { path: 'devices', name: 'DeviceManage', component: () => import('../views/DeviceManage.vue'), meta: { title: '充电桩管理' } },
      { path: 'orders', name: 'OrderManage', component: () => import('../views/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'prices', name: 'PriceManage', component: () => import('../views/PriceManage.vue'), meta: { title: '价格管理' } },
      { path: 'faults', name: 'FaultManage', component: () => import('../views/FaultManage.vue'), meta: { title: '故障管理' } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  if (!getToken() && to.name !== 'Login') {
    next('/login')
  } else {
    next()
  }
})

export default router
