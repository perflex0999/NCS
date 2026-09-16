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

    <!-- 底部充电站抽屉（可上拉下拉，抓手拖动、列表滚动加载） -->
    <div class="sheet" :style="{ height: sheetHeight + 'px' }">
      <div class="sheet-grabber"
        @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"></div>
      <div class="sheet-header">
        <span class="sheet-title">附近充电站</span>
        <span class="sheet-count">共 {{ filteredStations.length }} 个</span>
      </div>
      <div class="station-scroll" ref="scrollEl" @scroll="onScroll">
        <div v-for="(s, i) in visibleStations" :key="s.stationId" class="ncs-card station-mini ncs-enter"
          :style="{ animationDelay: (i % PAGE) * 50 + 'ms' }" @click="goDetail(s)">
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
        <div v-if="visibleStations.length < filteredStations.length" class="load-more">上拉加载更多…</div>
        <van-empty v-if="!filteredStations.length" description="附近暂无充电站" />
      </div>
    </div>

    <!-- AI 悬浮球 + 主动互动气泡 -->
    <transition name="bubble-fade">
      <div v-if="showBubble" class="ai-bubble" @click="goAssistant">
        <span class="ai-bubble-text">{{ bubbleText }}</span>
        <span class="ai-bubble-close" @click.stop="dismissBubble">×</span>
      </div>
    </transition>
    <div class="ai-ball" ref="ballEl"
      :style="ballPos.x != null ? { left: ballPos.x + 'px', top: ballPos.y + 'px', right: 'auto', bottom: 'auto' } : {}"
      @touchstart="onBallStart" @touchmove="onBallMove" @touchend="onBallEnd" @click="onBallClick">
      <span class="ai-ball-ico">🤖</span>
      <span class="ai-ball-pulse"></span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
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

const selectCategory = (v) => { deviceType.value = v; visibleCount.value = PAGE }

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

// 获取定位：优先浏览器定位，失败回退到杭州默认坐标
function getLocation() {
  return new Promise((resolve) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => resolve({ lat: 30.2741, lng: 120.1551 }),
        { timeout: 5000 }
      )
    } else {
      resolve({ lat: 30.2741, lng: 120.1551 })
    }
  })
}

onMounted(async () => {
  const loc = await getLocation()
  const TMap = await loadTMap()
  if (TMap) {
    map = new TMap.Map(document.getElementById('tmap'), {
      center: new TMap.LatLng(loc.lat, loc.lng),
      zoom: 13
    })
  }
  stations.value = await nearbyStations({ lat: loc.lat, lng: loc.lng })
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

// 抽屉拖拽（仅抓手区上拉放大、下拉缩小，列表区正常滚动）
const sheetHeight = ref(300)
let dragging = false
let startY = 0
let startH = 0
const onTouchStart = (e) => {
  dragging = true
  startY = e.touches[0].clientY
  startH = sheetHeight.value
}
const onTouchMove = (e) => {
  if (!dragging) return
  const dy = startY - e.touches[0].clientY
  const maxH = window.innerHeight * 0.88
  sheetHeight.value = Math.max(120, Math.min(maxH, startH + dy))
}
const onTouchEnd = () => { dragging = false }

// 列表分页：每次渲染 PAGE 条，滚到底部再加载下一批
const PAGE = 12
const visibleCount = ref(PAGE)
const scrollEl = ref(null)
const visibleStations = computed(() => filteredStations.value.slice(0, visibleCount.value))
const onScroll = () => {
  const el = scrollEl.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 30) {
    if (visibleCount.value < filteredStations.value.length) {
      visibleCount.value += PAGE
    }
  }
}

// AI 悬浮球 + 定期主动互动
const showBubble = ref(false)
const bubbleText = ref('')
const greetings = [
  '附近有空闲快充哦，需要我帮你找吗？⚡',
  '充电费用有疑问？点我问 AI～',
  '充电桩出问题了？我帮你诊断一下 🔧'
]
let bubbleTimer = null
let bubbleHideTimer = null
const goAssistant = () => router.push('/assistant')
const dismissBubble = () => { showBubble.value = false }

// AI 悬浮球可随意拖动（区分点击与拖动）
const ballEl = ref(null)
const ballPos = reactive({ x: null, y: null })
const BALL_SIZE = 52
let ballDragging = false
let ballMoved = false
let ballStartX = 0
let ballStartY = 0
let ballOrigX = 0
let ballOrigY = 0
const onBallStart = (e) => {
  const rect = ballEl.value.getBoundingClientRect()
  ballOrigX = rect.left
  ballOrigY = rect.top
  ballStartX = e.touches[0].clientX
  ballStartY = e.touches[0].clientY
  ballDragging = true
  ballMoved = false
}
const onBallMove = (e) => {
  if (!ballDragging) return
  const dx = e.touches[0].clientX - ballStartX
  const dy = e.touches[0].clientY - ballStartY
  if (Math.abs(dx) + Math.abs(dy) > 6) ballMoved = true
  ballPos.x = Math.max(0, Math.min(window.innerWidth - BALL_SIZE, ballOrigX + dx))
  ballPos.y = Math.max(0, Math.min(window.innerHeight - BALL_SIZE, ballOrigY + dy))
}
const onBallEnd = () => { ballDragging = false }
const onBallClick = () => { if (!ballMoved) goAssistant() }
const showGreeting = () => {
  bubbleText.value = greetings[Math.floor(Math.random() * greetings.length)]
  showBubble.value = true
  if (bubbleHideTimer) clearTimeout(bubbleHideTimer)
  bubbleHideTimer = setTimeout(() => { showBubble.value = false }, 8000)
}
onMounted(() => {
  setTimeout(showGreeting, 5000)
  bubbleTimer = setInterval(showGreeting, 30000)
})
onUnmounted(() => {
  if (bubbleTimer) clearInterval(bubbleTimer)
  if (bubbleHideTimer) clearTimeout(bubbleHideTimer)
})
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
  padding: 8px 16px 20px;
  box-shadow: 0 -8px 30px rgba(40, 60, 90, 0.12);
  display: flex; flex-direction: column;
}
.sheet-grabber {
  width: 100%; height: 24px; display: flex; align-items: center; justify-content: center;
  touch-action: none; cursor: grab;
}
.sheet-grabber::before { content: ''; width: 40px; height: 4px; border-radius: 2px; background: #E6EBF2; }
.sheet-header { display: flex; justify-content: space-between; align-items: center; padding: 4px 4px 10px; }
.sheet-title { font-size: 15px; font-weight: 700; color: #2A3240; }
.sheet-count { font-size: 12px; color: #8B93A1; }
.station-scroll { overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding-bottom: 4px; -webkit-overflow-scrolling: touch; }
.load-more { text-align: center; font-size: 12px; color: #B0B7C3; padding: 10px 0 4px; }
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
.ai-ball {
  position: fixed; right: 16px; bottom: 96px; z-index: 50;
  width: 52px; height: 52px; border-radius: 50%;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0, #45D094);
  box-shadow: 0 6px 20px rgba(62, 201, 192, 0.4);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  touch-action: none;
  user-select: none;
  -webkit-user-select: none;
}
.ai-ball-ico { font-size: 26px; }
.ai-ball-pulse {
  position: absolute; inset: -4px; border-radius: 50%;
  border: 2px solid rgba(62, 201, 192, 0.4);
  animation: ai-pulse 2s ease-out infinite;
}
@keyframes ai-pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.5); opacity: 0; }
}
.ai-bubble {
  position: fixed; right: 16px; bottom: 158px; z-index: 50;
  max-width: 220px; background: #fff; border-radius: 14px;
  padding: 12px 14px; box-shadow: 0 6px 20px rgba(40, 60, 90, 0.18);
  display: flex; align-items: flex-start; gap: 8px;
}
.ai-bubble-text { font-size: 13px; color: #2A3240; line-height: 1.5; }
.ai-bubble-close { color: #B0B7C3; font-size: 16px; cursor: pointer; line-height: 1; }
.bubble-fade-enter-active, .bubble-fade-leave-active { transition: opacity 0.3s, transform 0.3s; }
.bubble-fade-enter-from, .bubble-fade-leave-to { opacity: 0; transform: translateY(8px); }
</style>
