<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">充电运营平台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">数据统计</el-menu-item>
        <el-menu-item index="/stations">充电站管理</el-menu-item>
        <el-menu-item index="/devices">充电桩管理</el-menu-item>
        <el-menu-item index="/orders">订单管理</el-menu-item>
        <el-menu-item index="/prices">价格管理</el-menu-item>
        <el-menu-item index="/faults">故障管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title }}</span>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
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
.aside { background: #304156; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-weight: bold; font-size: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; }
</style>
