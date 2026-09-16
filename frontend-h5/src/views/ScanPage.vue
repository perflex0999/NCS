<template>
  <div class="scan-page">
    <van-nav-bar title="扫码充电" left-arrow @click-left="$router.back()" />

    <!-- 模拟扫码框 -->
    <div class="scan-box ncs-enter">
      <div class="scan-corner tl"></div>
      <div class="scan-corner tr"></div>
      <div class="scan-corner bl"></div>
      <div class="scan-corner br"></div>
      <div class="scan-line"></div>
      <div class="scan-center">⚡</div>
      <div class="scan-hint">对准充电桩上的二维码</div>
    </div>

    <!-- 手动输入设备编号 -->
    <div class="ncs-card input-card ncs-enter" style="animation-delay:100ms">
      <div class="input-label">或手动输入设备编号</div>
      <van-field v-model="deviceNo" placeholder="如 DEV-1001" class="device-field" />
      <van-button round block class="ncs-gradient-btn" @click="start">开始充电</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { startCharge } from '../api'

const route = useRoute()
const router = useRouter()
const deviceNo = ref(route.query.deviceNo || '')

const start = async () => {
  if (!deviceNo.value) {
    showToast('请输入设备编号')
    return
  }
  try {
    await startCharge(deviceNo.value)
    showToast('充电已开始')
    router.replace('/charge')
  } catch (e) {
    // 已由拦截器提示
  }
}
</script>

<style scoped>
.scan-page { min-height: 100vh; }
.scan-box {
  position: relative;
  margin: 40px auto;
  width: 240px; height: 240px;
  background: #0B1220;
  border-radius: 20px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;
}
.scan-corner { position: absolute; width: 28px; height: 28px; border: 3px solid #45D094; }
.tl { top: 12px; left: 12px; border-right: none; border-bottom: none; border-radius: 6px 0 0 0; }
.tr { top: 12px; right: 12px; border-left: none; border-bottom: none; border-radius: 0 6px 0 0; }
.bl { bottom: 12px; left: 12px; border-right: none; border-top: none; border-radius: 0 0 0 6px; }
.br { bottom: 12px; right: 12px; border-left: none; border-top: none; border-radius: 0 0 6px 0; }
.scan-line {
  position: absolute; left: 16px; right: 16px; height: 2px;
  background: linear-gradient(90deg, transparent, #45D094, transparent);
  animation: scan-move 2s ease-in-out infinite;
}
@keyframes scan-move {
  0%, 100% { top: 20%; }
  50% { top: 78%; }
}
.scan-center { font-size: 44px; }
.scan-hint { color: #8B93A1; font-size: 12px; }
.input-card { margin: 0 16px; padding: 18px; }
.input-label { font-size: 14px; color: #2A3240; margin-bottom: 8px; }
.device-field { background: #F4F7FB; border-radius: 12px; margin-bottom: 14px; }
</style>
