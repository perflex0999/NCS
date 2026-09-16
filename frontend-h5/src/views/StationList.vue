<template>
  <div>
    <van-nav-bar title="附近充电站" right-text="订单" @click-right="$router.push('/orders')" />
    <van-dropdown-menu>
      <van-dropdown-item v-model="deviceType" :options="typeOptions" @change="load" />
      <van-dropdown-item v-model="sortBy" :options="sortOptions" @change="load" />
    </van-dropdown-menu>
    <van-cell-group inset v-if="stations.length">
      <van-cell v-for="s in stations" :key="s.stationId" :title="s.name"
        :label="`${s.address} · 距离 ${s.distanceKm ?? '-'} km`" is-link @click="goDetail(s)">
        <template #value>
          <div class="price">¥{{ s.unitPrice ?? '-' }}/度</div>
          <div class="sub">快{{ s.fastCount }} 慢{{ s.slowCount }} 空闲{{ s.idleCount }}</div>
        </template>
      </van-cell>
    </van-cell-group>
    <van-empty v-else description="暂无充电站" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { nearbyStations } from '../api'

const router = useRouter()
const stations = ref([])
const deviceType = ref(null)
const sortBy = ref('distance')

const typeOptions = [
  { text: '全部类型', value: null },
  { text: '快充', value: 1 },
  { text: '慢充', value: 2 }
]
const sortOptions = [
  { text: '按距离', value: 'distance' },
  { text: '按价格', value: 'price' }
]

// 演示用默认定位（杭州），真实场景可用浏览器定位
const load = async () => {
  try {
    stations.value = await nearbyStations({
      lat: 30.2741,
      lng: 120.1551,
      deviceType: deviceType.value,
      sortBy: sortBy.value
    })
  } catch (e) {
    // 已由拦截器提示
  }
}

const goDetail = (s) => router.push(`/station/${s.stationId}`)

onMounted(load)
</script>

<style scoped>
.price { color: #ee0a24; font-weight: bold; }
.sub { color: #969799; font-size: 12px; }
</style>
