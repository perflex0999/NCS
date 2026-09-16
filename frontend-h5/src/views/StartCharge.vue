<template>
  <div>
    <van-nav-bar title="开始充电" left-arrow @click-left="$router.back()" />
    <p style="text-align:center;color:#969799;padding:16px 0;">模拟扫码：输入设备编号开始充电</p>
    <van-cell-group inset>
      <van-field v-model="deviceNo" label="设备编号" placeholder="如 DEV-1001" />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" @click="start">开始充电</van-button>
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
    router.replace('/charging')
  } catch (e) {
    // 已由拦截器提示
  }
}
</script>
