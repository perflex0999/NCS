<template>
  <div class="detail-page">
    <van-nav-bar title="充电站详情" left-arrow @click-left="$router.back()" />
    <div v-if="detail">
      <!-- 站点信息卡 -->
      <div class="ncs-card info-card ncs-enter">
        <div class="info-name">{{ detail.name }}</div>
        <div class="info-row"><span class="lbl">地址</span>{{ detail.address }}</div>
        <div class="info-row"><span class="lbl">营业时间</span>{{ detail.businessHours }}</div>
        <div class="info-row"><span class="lbl">停车</span>{{ detail.parkingInfo || '-' }}</div>
        <div class="info-row"><span class="lbl">电话</span>{{ detail.contact || '-' }}</div>
      </div>

      <!-- 充电桩列表（每个桩带二维码） -->
      <h4 class="section-title">充电桩</h4>
      <div class="device-list">
        <div v-for="d in detail.devices" :key="d.deviceId" class="ncs-card device-card">
          <div class="device-info">
            <div class="device-no">{{ d.deviceNo }}</div>
            <div class="device-meta">{{ d.deviceTypeDesc }} · {{ d.powerKw }}kW</div>
            <span class="status" :class="statusClass(d.status)">{{ d.statusDesc }}</span>
          </div>
          <div class="device-qr">
            <img v-if="d.qrCode" :src="d.qrCode" class="qr-img" alt="扫码充电" />
            <div class="qr-hint">扫码充电</div>
          </div>
          <van-button v-if="d.status === 0 && !reserved.has(d.deviceNo)" round block class="ncs-gradient-btn charge-btn" @click="reserve(d.deviceNo)">
            预约
          </van-button>
          <van-button v-if="d.status === 0 && reserved.has(d.deviceNo)" round block class="charge-btn reserved-btn" @click="goCharge(d.deviceNo)">
            已预约 · 去充电
          </van-button>
          <van-button v-if="d.status !== 0" round block disabled class="charge-btn">暂不可用</van-button>
        </div>
      </div>

      <!-- 收费标准 -->
      <h4 class="section-title">收费标准</h4>
      <div class="ncs-card price-card">
        <div v-for="p in detail.prices" :key="p.startTime + p.deviceType" class="price-row">
          <span class="p-type">{{ p.deviceTypeDesc }}</span>
          <span class="p-time">{{ p.startTime }} - {{ p.endTime }}</span>
          <span class="p-value">电{{ p.elecPrice }} + 服{{ p.servicePrice }} 元/度</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import QRCode from 'qrcode'
import { stationDetail, reserveDevice } from '../api'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const reserved = ref(new Set())

const reserve = async (deviceNo) => {
  try {
    await reserveDevice(deviceNo)
    reserved.value = new Set([...reserved.value, deviceNo])
    showToast('预约成功，30 分钟内有效')
  } catch (e) {
    // 已由拦截器提示
  }
}

const goCharge = (deviceNo) => router.push(`/scan?deviceNo=${deviceNo}`)

const statusClass = (s) => ({ 0: 'ok', 1: 'busy', 2: 'err', 3: 'err', 4: 'busy' }[s] || '')

onMounted(async () => {
  try {
    const d = await stationDetail(route.params.id)
    detail.value = d
    // 为每个设备生成二维码（内容=设备编号）
    for (const dev of d.devices) {
      dev.qrCode = await QRCode.toDataURL(dev.deviceNo, { width: 90, margin: 1, color: { dark: '#2A3240', light: '#FFFFFF' } })
    }
  } catch (e) {
    // 已由拦截器提示
  }
})
</script>

<style scoped>
.detail-page { min-height: 100vh; padding-bottom: 20px; }
.info-card { margin: 12px 16px; padding: 16px; }
.info-name { font-size: 18px; font-weight: 700; color: #2A3240; margin-bottom: 8px; }
.info-row { font-size: 13px; color: #8B93A1; padding: 4px 0; }
.info-row .lbl { display: inline-block; width: 60px; color: #B0B7C3; }
.section-title { padding: 8px 20px; font-size: 15px; font-weight: 700; color: #2A3240; }
.device-list { display: flex; flex-direction: column; gap: 12px; padding: 0 16px; }
.device-card { padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.device-info { position: relative; }
.device-no { font-size: 16px; font-weight: 700; color: #2A3240; }
.device-meta { font-size: 12px; color: #8B93A1; margin-top: 3px; }
.status { position: absolute; top: 0; right: 0; font-size: 12px; padding: 3px 10px; border-radius: 999px; }
.status.ok { color: #45D094; background: #EBF9F1; }
.status.busy { color: #F5B544; background: #FEF5E7; }
.status.err { color: #F06A6A; background: #FDF0F0; }
.device-qr { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.qr-img { width: 90px; height: 90px; border: 1px solid #EEF1F6; border-radius: 10px; padding: 4px; }
.qr-hint { font-size: 11px; color: #B0B7C3; }
.charge-btn { height: 42px; font-size: 15px; }
.reserved-btn { background: #F0F6FF; color: #5EA8FF; border: 1px solid #BBD7FF; }
.price-card { margin: 0 16px; padding: 8px 16px; }
.price-row { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid #F4F7FB; font-size: 13px; }
.price-row:last-child { border-bottom: none; }
.p-type { color: #2A3240; font-weight: 600; }
.p-time { color: #8B93A1; }
.p-value { margin-left: auto; color: #3EC9C0; }
</style>
