<template>
  <div>
    <van-nav-bar title="充电中" />
    <div class="status-card">
      <div class="station">{{ status.stationName }}</div>
      <div class="device">{{ status.deviceNo }} · {{ status.deviceTypeDesc }}</div>
      <div class="amount">¥{{ status.amount }}</div>
      <div class="kwh">{{ status.chargedKwh }} kWh</div>
      <div class="time">已充 {{ formatTime(status.elapsedSeconds) }}</div>
    </div>
    <div style="margin: 16px;">
      <van-button round block type="danger" @click="end">结束充电</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { currentCharge, endCharge } from '../api'

const router = useRouter()
const status = ref({})
let timer = null

const load = async () => {
  try {
    status.value = await currentCharge()
  } catch (e) {
    clearInterval(timer)
    router.replace('/stations')
  }
}

const formatTime = (s) => {
  if (s == null) return '0秒'
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return `${h}时${m}分${sec}秒`
}

const end = async () => {
  try {
    await endCharge()
    showToast('充电结束')
    router.replace('/orders')
  } catch (e) {
    // 已由拦截器提示
  }
}

onMounted(() => {
  load()
  timer = setInterval(load, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.status-card {
  margin: 16px;
  padding: 36px 16px;
  text-align: center;
  border-radius: 20px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 12px 30px rgba(34, 197, 94, 0.3);
  color: #fff;
}
.station { font-size: 18px; font-weight: bold; }
.device { font-size: 13px; opacity: 0.9; margin-top: 6px; }
.amount { font-size: 40px; font-weight: bold; margin-top: 16px; }
.kwh { font-size: 15px; margin-top: 4px; }
.time { font-size: 13px; opacity: 0.9; margin-top: 8px; }
</style>
