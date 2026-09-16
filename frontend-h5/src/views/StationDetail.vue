<template>
  <div>
    <van-nav-bar title="充电站详情" left-arrow @click-left="$router.back()" />
    <div v-if="detail">
      <van-cell-group inset>
        <van-cell title="名称" :value="detail.name" />
        <van-cell title="地址" :value="detail.address" />
        <van-cell title="营业时间" :value="detail.businessHours" />
        <van-cell title="停车说明" :value="detail.parkingInfo || '-'" />
        <van-cell title="联系方式" :value="detail.contact || '-'" />
      </van-cell-group>

      <h4 style="padding: 0 16px;">充电桩</h4>
      <van-cell-group inset>
        <van-cell v-for="d in detail.devices" :key="d.deviceId"
          :title="`${d.deviceNo}（${d.deviceTypeDesc}）`" :label="`功率 ${d.powerKw} kW`">
          <template #value>
            <span>{{ d.statusDesc }}</span>
            <van-button v-if="d.status === 0" size="mini" type="primary" @click="goStart(d.deviceNo)">充电</van-button>
          </template>
        </van-cell>
      </van-cell-group>

      <h4 style="padding: 0 16px;">收费标准</h4>
      <van-cell-group inset>
        <van-cell v-for="p in detail.prices" :key="p.startTime + p.deviceType"
          :title="`${p.deviceTypeDesc} ${p.startTime} - ${p.endTime}`"
          :value="`电${p.elecPrice}+服${p.servicePrice} 元/度`" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { stationDetail } from '../api'

const route = useRoute()
const router = useRouter()
const detail = ref(null)

const goStart = (deviceNo) => router.push(`/start?deviceNo=${deviceNo}`)

onMounted(async () => {
  try {
    detail.value = await stationDetail(route.params.id)
  } catch (e) {
    // 已由拦截器提示
  }
})
</script>
