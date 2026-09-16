<template>
  <div>
    <!-- 玻璃拟态 KPI 卡片（数字滚动） -->
    <div class="bento">
      <div class="ncs-panel kpi hero ncs-panel--hover ncs-enter">
        <div class="kpi-top"><span class="kpi-label">总收入（元）</span><span class="kpi-ico">💰</span></div>
        <div class="kpi-value hero-value">{{ disp.revenue }}</div>
        <div class="kpi-sub">累计充电量 {{ overview.chargedKwh ?? '-' }} kWh</div>
      </div>
      <div class="ncs-panel kpi ncs-panel--hover ncs-enter" style="animation-delay:60ms">
        <div class="kpi-top"><span class="kpi-label">注册用户</span><span class="kpi-ico">👥</span></div>
        <div class="kpi-value">{{ disp.userCount }}</div>
      </div>
      <div class="ncs-panel kpi ncs-panel--hover ncs-enter" style="animation-delay:120ms">
        <div class="kpi-top"><span class="kpi-label">充电订单</span><span class="kpi-ico">🧾</span></div>
        <div class="kpi-value">{{ disp.orderCount }}</div>
      </div>
      <div class="ncs-panel kpi ncs-panel--hover ncs-enter" style="animation-delay:180ms">
        <div class="kpi-top"><span class="kpi-label">充电设备</span><span class="kpi-ico">⚡</span></div>
        <div class="kpi-value">{{ disp.deviceCount }}</div>
      </div>
      <div class="ncs-panel kpi ncs-panel--hover ncs-enter" style="animation-delay:240ms">
        <div class="kpi-top"><span class="kpi-label">故障设备</span><span class="kpi-ico">⚠️</span></div>
        <div class="kpi-value danger">{{ disp.faultCount }}</div>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="charts">
      <div class="ncs-panel chart-card ncs-enter" style="animation-delay:300ms">
        <div ref="orderChartEl" class="chart"></div>
      </div>
      <div class="ncs-panel chart-card ncs-enter" style="animation-delay:360ms">
        <div ref="revenueChartEl" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statsOverview, statsTrend } from '../api'

const overview = ref({})
const disp = reactive({ revenue: 0, userCount: 0, orderCount: 0, deviceCount: 0, faultCount: 0 })
const orderChartEl = ref(null)
const revenueChartEl = ref(null)
let orderChart = null
let revenueChart = null

// 数字滚动（easeOutCubic）
function countUp(target, setter, decimals = 0) {
  const start = performance.now()
  const duration = 900
  const tick = (now) => {
    const t = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - t, 3)
    setter(Number((target * eased).toFixed(decimals)))
    if (t < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

const loadOverview = async () => {
  const d = await statsOverview()
  overview.value = d
  countUp(Number(d.revenue || 0), v => disp.revenue = v, 2)
  countUp(Number(d.userCount || 0), v => disp.userCount = v)
  countUp(Number(d.orderCount || 0), v => disp.orderCount = v)
  countUp(Number(d.deviceCount || 0), v => disp.deviceCount = v)
  countUp(Number(d.faultCount || 0), v => disp.faultCount = v)
}

const loadTrend = async () => {
  const data = await statsTrend(7)
  const dates = data.map(d => d.date)
  const orderCounts = data.map(d => d.orderCount)
  const revenues = data.map(d => d.revenue)

  orderChart.setOption({
    title: { text: '近7日订单趋势', textStyle: { fontSize: 14, color: '#EAF0FA' } },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(18,26,41,0.95)', borderColor: '#1B2740',
      textStyle: { color: '#E6ECF5', fontSize: 12 }
    },
    grid: { left: 50, right: 20, top: 44, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#1B2740' } }, axisLabel: { color: '#8A94A8' } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#1B2740', type: 'dashed' } }, axisLabel: { color: '#8A94A8' } },
    series: [{
      name: '订单数', type: 'line', data: orderCounts, smooth: true,
      color: '#4D9FFF', lineStyle: { width: 2.5 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
        { offset: 0, color: 'rgba(77,159,255,0.30)' }, { offset: 1, color: 'rgba(77,159,255,0)' }
      ] } },
      symbolSize: 6
    }]
  })

  revenueChart.setOption({
    title: { text: '近7日收入趋势(元)', textStyle: { fontSize: 14, color: '#EAF0FA' } },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(18,26,41,0.95)', borderColor: '#1B2740',
      textStyle: { color: '#E6ECF5', fontSize: 12 }
    },
    grid: { left: 60, right: 20, top: 44, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#1B2740' } }, axisLabel: { color: '#8A94A8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#1B2740', type: 'dashed' } }, axisLabel: { color: '#8A94A8' } },
    series: [{
      name: '收入', type: 'bar', data: revenues, barMaxWidth: 30,
      itemStyle: { borderRadius: [6, 6, 0, 0], color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
        { offset: 0, color: '#00E5FF' }, { offset: 1, color: 'rgba(0,229,255,0.25)' }
      ] } }
    }]
  })
}

const onResize = () => {
  orderChart && orderChart.resize()
  revenueChart && revenueChart.resize()
}

onMounted(() => {
  orderChart = echarts.init(orderChartEl.value)
  revenueChart = echarts.init(revenueChartEl.value)
  loadOverview()
  loadTrend()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  orderChart && orderChart.dispose()
  revenueChart && revenueChart.dispose()
})
</script>

<style scoped>
.bento { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.hero { grid-column: span 2; }
.kpi { padding: 22px; }
.kpi-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.kpi-label { color: var(--text-sub); font-size: 13px; }
.kpi-ico { font-size: 22px; }
.kpi-value { font-size: 30px; font-weight: 700; letter-spacing: -0.5px; color: var(--cyan); font-variant-numeric: tabular-nums; }
.hero-value { font-size: 42px; text-shadow: 0 0 16px var(--glow-cyan); }
.danger { color: var(--red); }
.kpi-sub { color: var(--text-sub); font-size: 13px; margin-top: 8px; }
.charts { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 18px; }
.chart-card { padding: 18px; }
.chart { height: 300px; }
@media (max-width: 1200px) {
  .bento { grid-template-columns: repeat(2, 1fr); }
  .charts { grid-template-columns: 1fr; }
}
</style>
