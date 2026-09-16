<template>
  <div class="main-layout">
    <div class="content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <!-- 底部导航（中间横向椭圆扫码按钮，参考 QT 用户端） -->
    <div class="tabbar">
      <div class="tab-item" :class="{ active: $route.path === '/home' }" @click="$router.push('/home')">
        <span class="tab-ico">🏠</span><span>首页</span>
      </div>
      <div class="scan-btn" @click="$router.push('/scan')">
        <span class="scan-ico">⚡</span><span class="scan-text">扫码充电</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path === '/profile' }" @click="$router.push('/profile')">
        <span class="tab-ico">👤</span><span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.main-layout { display: flex; flex-direction: column; height: 100vh; }
.content { flex: 1; overflow-y: auto; }
.tabbar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 62px;
  background: linear-gradient(90deg, #EDF4FD, #ECF7F3, #EAF6EE);
  border-top: 1px solid #EEF1F6;
  position: relative;
  z-index: 100;
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #8B93A1;
  cursor: pointer;
}
.tab-item.active { color: #5EA8FF; font-weight: 600; }
.tab-ico { font-size: 20px; }
.scan-btn {
  flex: 0 0 auto;
  width: clamp(110px, 28vw, 150px);
  height: 46px;
  margin-top: -18px;
  border-radius: 23px;
  background: linear-gradient(90deg, #5EA8FF, #3EC9C0, #45D094);
  border: 3px solid #fff;
  box-shadow: 0 6px 18px rgba(62, 201, 192, 0.4);
  display: flex; align-items: center; justify-content: center; gap: 5px;
  color: #fff;
  cursor: pointer;
}
.scan-ico { font-size: 18px; }
.scan-text { font-size: 13px; font-weight: 700; white-space: nowrap; }
</style>
