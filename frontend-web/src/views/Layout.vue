<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-dot"></span>
        <span class="logo-text">充电运营平台</span>
      </div>
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item index="/dashboard">
          <span class="menu-ico">📊</span><span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/stations">
          <span class="menu-ico">🔌</span><span>充电站管理</span>
        </el-menu-item>
        <el-menu-item index="/devices">
          <span class="menu-ico">⚡</span><span>充电桩管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <span class="menu-ico">🧾</span><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/prices">
          <span class="menu-ico">💰</span><span>价格管理</span>
        </el-menu-item>
        <el-menu-item index="/faults">
          <span class="menu-ico">⚠️</span><span>故障管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main">
      <el-header class="header">
        <span class="title">{{ $route.meta.title }}</span>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { clearToken } from '../utils/auth'

const router = useRouter()
const logout = () => {
  clearToken()
  router.replace('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: #fff;
  border-right: 1px solid var(--ncs-border);
  display: flex;
  flex-direction: column;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  font-weight: 700;
  font-size: 16px;
}
.logo-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.15);
}
.menu { border-right: none; padding: 0 10px; }
.menu-ico { margin-right: 8px; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--ncs-border);
  position: sticky;
  top: 0;
  z-index: 10;
}
.title { font-size: 18px; font-weight: 600; }
.content { padding: 24px; }
</style>
