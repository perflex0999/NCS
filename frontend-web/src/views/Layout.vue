<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-dot"></span>
        <span class="logo-text">熠熠ee</span>
      </div>
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item index="/dashboard"><span class="menu-ico">📊</span><span>数据统计</span></el-menu-item>
        <el-menu-item index="/stations"><span class="menu-ico">🔌</span><span>充电站管理</span></el-menu-item>
        <el-menu-item index="/devices"><span class="menu-ico">⚡</span><span>充电桩管理</span></el-menu-item>
        <el-menu-item index="/orders"><span class="menu-ico">🧾</span><span>订单管理</span></el-menu-item>
        <el-menu-item index="/prices"><span class="menu-ico">💰</span><span>价格管理</span></el-menu-item>
        <el-menu-item index="/faults"><span class="menu-ico">⚠️</span><span>故障管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main">
      <el-header class="header">
        <span class="title">{{ $route.meta.title }}</span>
        <div class="header-actions">
          <el-button link type="primary" @click="openBigScreen">📺 数据大屏</el-button>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
const openBigScreen = () => {
  window.open('/bigscreen/', '_blank')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: #0E1628;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
}
.logo { display: flex; align-items: center; gap: 10px; padding: 20px; }
.logo-dot {
  width: 12px; height: 12px; border-radius: 50%;
  background: linear-gradient(135deg, var(--blue), var(--cyan));
  box-shadow: 0 0 12px var(--glow-cyan);
}
.logo-text { font-weight: 700; font-size: 16px; color: var(--text-main); letter-spacing: 1px; }
.menu { border-right: none; background: transparent; padding: 0 10px; }
.menu :deep(.el-menu-item) {
  color: var(--text-sub);
  border-radius: 8px;
  margin: 3px 0;
  transition: all 0.25s ease;
}
.menu :deep(.el-menu-item:hover) { background: rgba(77, 159, 255, 0.08); color: var(--text-main); }
.menu :deep(.el-menu-item.is-active) {
  background: rgba(77, 159, 255, 0.14);
  color: var(--cyan);
  border-left: 3px solid var(--cyan);
  font-weight: 600;
}
.menu-ico { margin-right: 8px; }
.header {
  display: flex; align-items: center; justify-content: space-between;
  background: rgba(13, 23, 42, 0.7);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 10;
}
.title { font-size: 18px; font-weight: 600; color: var(--text-main); }
.header-actions { display: flex; align-items: center; gap: 8px; }
.content { padding: 24px; }
</style>
