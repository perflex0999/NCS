<template>
  <div class="page">
    <van-nav-bar title="附近充电站" right-text="订单" @click-right="$router.push('/orders')" />
    <div class="filters">
      <van-dropdown-menu>
        <van-dropdown-item v-model="deviceType" :options="typeOptions" @change="load" />
        <van-dropdown-item v-model="sortBy" :options="sortOptions" @change="load" />
      </van-dropdown-menu>
    </div>
    <div class="station-list">
      <div v-for="(s, i) in stations" :key="s.stationId" class="ncs-panel station-card ncs-enter"
        :style="{ animationDelay: (i * 60) + 'ms' }" @click="goDetail(s)">
        <div class="station-head">
          <span class="station-name">{{ s.name }}</span>
          <span class="distance">{{ s.distanceKm ?? '-' }} km</span>
        </div>
        <div class="station-addr">{{ s.address }} · {{ s.city }}</div>
        <div class="station-foot">
          <span class="price">¥{{ s.unitPrice ?? '-' }}<em>/度</em></span>
          <div class="chips">
            <span class="chip">快充 {{ s.fastCount }}</span>
            <span class="chip">慢充 {{ s.slowCount }}</span>
            <span class="chip idle">空闲 {{ s.idleCount }}</span>
          </div>
        </div>
      </div>
      <van-empty v-if="!stations.length" description="暂无充电站" />
    </div>
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
.page { min-height: 100vh; }
.filters { background: rgba(13, 23, 42, 0.7); backdrop-filter: blur(14px); }
.station-list { padding: 14px; display: flex; flex-direction: column; gap: 14px; }
.station-card { padding: 16px; }
.station-head { display: flex; align-items: center; justify-content: space-between; }
.station-name { font-size: 17px; font-weight: 600; color: var(--text-main); }
.distance { color: var(--text-sub); font-size: 12px; background: rgba(77, 159, 255, 0.12); color: var(--blue); padding: 3px 10px; border-radius: 999px; }
.station-addr { color: var(--text-sub); font-size: 13px; margin-top: 6px; }
.station-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; }
.price { color: var(--cyan); font-size: 20px; font-weight: 700; text-shadow: 0 0 12px var(--glow-cyan); }
.price em { font-style: normal; font-size: 12px; font-weight: 400; color: var(--text-sub); }
.chips { display: flex; gap: 6px; }
.chip { font-size: 12px; color: var(--text-sub); background: rgba(15, 26, 46, 0.7); border: 1px solid var(--border); padding: 4px 10px; border-radius: 8px; }
.chip.idle { color: var(--green); border-color: rgba(45, 212, 167, 0.35); }
</style>
