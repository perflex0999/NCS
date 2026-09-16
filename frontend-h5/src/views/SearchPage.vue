<template>
  <div class="search-page">
    <van-nav-bar title="搜索充电站" left-arrow @click-left="$router.back()" />
    <div class="search-input">
      <van-search v-model="keyword" placeholder="搜索充电站名称、地址" autofocus @search="doSearch" @update:model-value="doSearch" />
    </div>
    <div class="results">
      <div v-for="(s, i) in results" :key="s.stationId" class="ncs-card result-item ncs-enter"
        :style="{ animationDelay: (i * 40) + 'ms' }" @click="goDetail(s)">
        <div class="ri-head">
          <span class="name">{{ s.name }}</span>
          <span class="dist">{{ s.distanceKm ?? '-' }}km</span>
        </div>
        <div class="ri-addr">{{ s.address }}</div>
        <div class="ri-foot">
          <span class="price">¥{{ s.unitPrice ?? '-' }}/度</span>
          <span class="idle">空闲 {{ s.idleCount }} · 快充 {{ s.fastCount }}</span>
        </div>
      </div>
      <van-empty v-if="!results.length" description="未找到相关充电站" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { nearbyStations } from '../api'

const router = useRouter()
const keyword = ref('')
const all = ref([])
const results = ref([])

onMounted(async () => {
  all.value = await nearbyStations({ lat: 30.2741, lng: 120.1551 })
  results.value = all.value
})

const doSearch = () => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) { results.value = all.value; return }
  results.value = all.value.filter(s =>
    (s.name || '').toLowerCase().includes(kw) ||
    (s.address || '').toLowerCase().includes(kw) ||
    (s.city || '').toLowerCase().includes(kw)
  )
}

const goDetail = (s) => router.push(`/station/${s.stationId}`)
</script>

<style scoped>
.search-page { min-height: 100vh; }
.search-input { padding: 4px 12px; }
.results { padding: 4px 14px; display: flex; flex-direction: column; gap: 10px; }
.result-item { padding: 14px; cursor: pointer; }
.ri-head { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 16px; font-weight: 700; color: #2A3240; }
.dist { font-size: 12px; color: #5EA8FF; background: #F0F6FF; padding: 2px 9px; border-radius: 999px; }
.ri-addr { font-size: 12px; color: #8B93A1; margin-top: 5px; }
.ri-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.price { color: #3EC9C0; font-size: 16px; font-weight: 700; }
.idle { font-size: 12px; color: #8B93A1; }
</style>
