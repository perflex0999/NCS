<template>
  <div class="map-page">
    <!-- 大地图 -->
    <div ref="mapEl" class="map"></div>

    <!-- 顶部搜索/标题 -->
    <div class="map-top">
      <div class="map-title">
        <span class="brand-bar"></span>
        <span>附近充电站</span>
      </div>
    </div>

    <!-- 底部充电站抽屉 -->
    <div class="sheet ncs-enter">
      <div class="sheet-header">
        <span>共 {{ stations.length }} 个充电站</span>
        <span class="hint">点击卡片查看详情</span>
      </div>
      <div class="station-scroll">
        <div v-for="(s, i) in stations" :key="s.stationId" class="ncs-card station-mini"
          :style="{ animationDelay: (i * 50) + 'ms' }" @click="goDetail(s)">
          <div class="mini-head">
            <span class="name">{{ s.name }}</span>
            <span class="dist">{{ s.distanceKm ?? '-' }} km</span>
          </div>
          <div class="mini-addr">{{ s.address }}</div>
          <div class="mini-foot">
            <span class="price">¥{{ s.unitPrice ?? '-' }}<em>/度</em></span>
            <div class="chips">
              <span class="chip">快充 {{ s.fastCount }}</span>
              <span class="chip idle">空闲 {{ s.idleCount }}</span>
            </div>
          </div>
        </div>
        <van-empty v-if="!stations.length" description="暂无充电站" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import L from 'leaflet'
import { nearbyStations } from '../api'

const router = useRouter()
const mapEl = ref(null)
const stations = ref([])
let map = null

const pinIcon = L.divIcon({
  className: 'station-pin',
  html: '<div class="pin-dot"></div><div class="pin-pulse"></div>',
  iconSize: [22, 22],
  iconAnchor: [11, 22]
})

onMounted(async () => {
  map = L.map(mapEl.value, { zoomControl: false }).setView([30.2741, 120.1551], 12)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18
  }).addTo(map)

  stations.value = await nearbyStations({ lat: 30.2741, lng: 120.1551 })
  stations.value.forEach(s => {
    if (s.lat && s.lng) {
      const marker = L.marker([Number(s.lat), Number(s.lng)], { icon: pinIcon }).addTo(map)
      marker.bindPopup(`<b>${s.name}</b><br/>空闲 ${s.idleCount} 个桩`)
    }
  })
})

const goDetail = (s) => router.push(`/station/${s.stationId}`)
</script>

<style scoped>
.map-page { position: relative; height: 100%; display: flex; flex-direction: column; }
.map { position: absolute; inset: 0; z-index: 0; }
.map-top { position: absolute; top: 12px; left: 12px; right: 12px; z-index: 10; }
.map-title {
  display: inline-flex; align-items: center; gap: 8px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(8px);
  border-radius: 12px; padding: 10px 16px;
  font-size: 15px; font-weight: 600; color: #2A3240;
  box-shadow: 0 4px 16px rgba(40,60,90,0.10);
}
.brand-bar { width: 4px; height: 16px; border-radius: 2px; background: linear-gradient(180deg, #5EA8FF, #45D094); }
.sheet {
  position: absolute; bottom: 0; left: 0; right: 0; z-index: 10;
  background: #fff; border-radius: 22px 22px 0 0;
  padding: 14px 16px 16px;
  box-shadow: 0 -8px 30px rgba(40,60,90,0.12);
  max-height: 46%;
  display: flex; flex-direction: column;
}
.sheet-header { display: flex; justify-content: space-between; align-items: center; padding: 0 4px 10px; font-size: 14px; font-weight: 600; }
.hint { font-size: 12px; color: #8B93A1; font-weight: 400; }
.station-scroll { overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
.station-mini { padding: 13px; }
.mini-head { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 15px; font-weight: 600; color: #2A3240; }
.dist { font-size: 12px; color: #5EA8FF; background: #F0F6FF; padding: 2px 9px; border-radius: 999px; }
.mini-addr { font-size: 12px; color: #8B93A1; margin-top: 4px; }
.mini-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.price { color: #3EC9C0; font-size: 17px; font-weight: 700; }
.price em { font-style: normal; font-size: 11px; font-weight: 400; color: #8B93A1; }
.chips { display: flex; gap: 6px; }
.chip { font-size: 11px; color: #8B93A1; background: #F4F7FB; padding: 3px 9px; border-radius: 8px; }
.chip.idle { color: #45D094; background: #EBF9F1; }
</style>

<style>
.station-pin .pin-dot {
  width: 18px; height: 18px; border-radius: 50%;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0);
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(62, 201, 192, 0.5);
}
.station-pin .pin-pulse {
  width: 18px; height: 18px; border-radius: 50%;
  background: rgba(94, 168, 255, 0.3);
  position: absolute; top: 0; left: 0;
  animation: pin-pulse 1.6s ease-out infinite;
}
@keyframes pin-pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(2.4); opacity: 0; }
}
</style>
