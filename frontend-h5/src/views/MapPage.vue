<template>
  <div class="map-page">
    <!-- 腾讯地图 -->
    <div id="tmap" class="map"></div>

    <!-- 顶部：定位 + 搜索栏 -->
    <div class="map-top">
      <div class="search-bar" @click="$router.push('/search')">
        <span class="search-ico">🔍</span>
        <span class="search-placeholder">搜索充电站、地址</span>
      </div>
    </div>

    <!-- 分类筛选 chips -->
    <div class="cat-chips">
      <div v-for="c in categories" :key="c.value" class="chip-item"
        :class="{ active: deviceType === c.value }" @click="selectCategory(c.value)">{{ c.text }}</div>
    </div>

    <!-- 底部充电站抽屉 -->
    <div class="sheet">
      <div class="sheet-grabber"></div>
      <div class="sheet-header">
        <span class="sheet-title">附近充电站</span>
        <span class="sheet-count">共 {{ filteredStations.length }} 个</span>
      </div>
      <div class="station-scroll">
        <div v-for="(s, i) in filteredStations" :key="s.stationId" class="ncs-card station-mini ncs-enter"
          :style="{ animationDelay: (i * 50) + 'ms' }" @click="goDetail(s)">
          <div class="mini-head">
            <span class="name">{{ s.name }}</span>
            <span class="dist">{{ s.distanceKm ?? '-' }}km</span>
          </div>
          <div class="mini-addr">{{ s.address }}</div>
          <div class="mini-tags">
            <span class="tag blue">快充 {{ s.fastCount }}</span>
            <span class="tag">慢充 {{ s.slowCount }}</span>
            <span class="tag green">空闲 {{ s.idleCount }}</span>
            <span class="tag">24h</span>
          </div>
          <div class="mini-foot">
            <span class="price"><em>¥</em>{{ s.unitPrice ?? '-' }}<i>/度</i></span>
            <button class="go-btn" @click.stop="goDetail(s)">去充电</button>
          </div>
        </div>
        <van-empty v-if="!filteredStations.length" description="附近暂无充电站" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { nearbyStations } from '../api'

const router = useRouter()
const stations = ref([])
const deviceType = ref(null)
let map = null
let markerLayer = null

const categories = [
  { text: '全部', value: null },
  { text: '快充', value: 1 },
  { text: '慢充', value: 2 },
  { text: '空闲优先', value: 'idle' }
]

const filteredStations = computed(() => {
  if (deviceType.value === 'idle') {
    return stations.value.filter(s => s.idleCount > 0)
  }
  if (deviceType.value == null) return stations.value
  const type = Number(deviceType.value)
  return stations.value.filter(s => (type === 1 ? s.fastCount > 0 : s.slowCount > 0))
})

const selectCategory = (v) => { deviceType.value = v }

function loadTMap() {
  return new Promise((resolve) => {
    if (window.TMap) { resolve(window.TMap); return }
    const script = document.createElement('script')
    script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${import.meta.env.VITE_TMAP_KEY}`
    script.onload = () => resolve(window.TMap)
    script.onerror = () => resolve(null)
    document.head.appendChild(script)
  })
}

onMounted(async () => {
  const TMap = await loadTMap()
  if (TMap) {
    map = new TMap.Map(document.getElementById('tmap'), {
      center: new TMap.LatLng(30.2741, 120.1551),
      zoom: 13
    })
  }
  stations.value = await nearbyStations({ lat: 30.2741, lng: 120.1551 })
  if (map && TMap) {
    markerLayer = new TMap.MultiMarker({
      map,
      geometries: stations.value.filter(s => s.lat && s.lng).map((s, i) => ({
        id: String(i),
        styleId: 'station',
        position: new TMap.LatLng(Number(s.lat), Number(s.lng))
      }))
    })
    markerLayer.on('click', (e) => {
      const s = stations.value[Number(e.geometry.id)]
      if (s) router.push(`/station/${s.stationId}`)
    })
  }
})

const goDetail = (s) => router.push(`/station/${s.stationId}`)
</script>

<style scoped>
.map-page { position: relative; height: 100%; display: flex; flex-direction: column; }
.map { position: absolute; inset: 0; z-index: 0; }
.map-top { position: absolute; top: 10px; left: 12px; right: 12px; z-index: 10; }
.search-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border-radius: 22px; padding: 11px 16px;
  box-shadow: 0 4px 16px rgba(40, 60, 90, 0.12);
  cursor: pointer;
}
.search-ico { font-size: 16px; }
.search-placeholder { color: #B0B7C3; font-size: 14px; }
.cat-chips {
  position: absolute; top: 60px; left: 12px; right: 12px; z-index: 10;
  display: flex; gap: 8px; overflow-x: auto;
}
.chip-item {
  flex-shrink: 0; padding: 7px 14px; border-radius: 18px;
  background: #fff; color: #8B93A1; font-size: 13px;
  box-shadow: 0 2px 8px rgba(40, 60, 90, 0.08); cursor: pointer;
  transition: all 0.2s ease;
}
.chip-item.active {
  background: linear-gradient(90deg, #5EA8FF, #3EC9C0);
  color: #fff; font-weight: 600;
}
.sheet {
  position: absolute; bottom: 0; left: 0; right: 0; z-index: 10;
  background: #fff; border-radius: 22px 22px 0 0;
  padding: 8px 16px 16px;
  box-shadow: 0 -8px 30px rgba(40, 60, 90, 0.12);
  max-height: 48%; display: flex; flex-direction: column;
}
.sheet-grabber { width: 40px; height: 4px; border-radius: 2px; background: #E6EBF2; margin: 4px auto 8px; }
.sheet-header { display: flex; justify-content: space-between; align-items: center; padding: 4px 4px 10px; }
.sheet-title { font-size: 15px; font-weight: 700; color: #2A3240; }
.sheet-count { font-size: 12px; color: #8B93A1; }
.station-scroll { overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding-bottom: 4px; }
.station-mini { padding: 14px; cursor: pointer; }
.mini-head { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 16px; font-weight: 700; color: #2A3240; }
.dist { font-size: 12px; color: #5EA8FF; background: #F0F6FF; padding: 2px 9px; border-radius: 999px; }
.mini-addr { font-size: 12px; color: #8B93A1; margin-top: 4px; }
.mini-tags { display: flex; gap: 6px; margin-top: 8px; }
.tag { font-size: 11px; color: #8B93A1; background: #F4F7FB; padding: 3px 8px; border-radius: 6px; }
.tag.blue { color: #5EA8FF; background: #F0F6FF; }
.tag.green { color: #45D094; background: #EBF9F1; }
.mini-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.price { color: #3EC9C0; font-size: 20px; font-weight: 700; }
.price em { font-style: normal; font-size: 13px; }
.price i { font-style: normal; font-size: 11px; font-weight: 400; color: #8B93A1; }
.go-btn {
  background: linear-gradient(90deg, #5EA8FF, #3EC9C0);
  color: #fff; border: none; border-radius: 18px;
  padding: 8px 18px; font-size: 13px; font-weight: 600; cursor: pointer;
}
</style>
