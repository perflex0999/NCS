<template>
  <div>
    <!-- Bento 统计卡片网格 -->
    <div class="bento">
      <div class="ncs-card stat hero ncs-card--hover ncs-enter">
        <div class="stat-top">
          <span class="stat-label">总收入（元）</span>
          <span class="stat-ico">💰</span>
        </div>
        <div class="stat-value hero-value">{{ overview.revenue ?? '-' }}</div>
        <div class="stat-sub">累计充电量 {{ overview.chargedKwh ?? '-' }} kWh</div>
      </div>

      <div class="ncs-card stat ncs-card--hover ncs-enter">
        <div class="stat-top">
          <span class="stat-label">注册用户</span>
          <span class="stat-ico">👥</span>
        </div>
        <div class="stat-value">{{ overview.userCount ?? '-' }}</div>
      </div>

      <div class="ncs-card stat ncs-card--hover ncs-enter">
        <div class="stat-top">
          <span class="stat-label">充电订单</span>
          <span class="stat-ico">🧾</span>
        </div>
        <div class="stat-value">{{ overview.orderCount ?? '-' }}</div>
      </div>

      <div class="ncs-card stat ncs-card--hover ncs-enter">
        <div class="stat-top">
          <span class="stat-label">充电设备</span>
          <span class="stat-ico">⚡</span>
        </div>
        <div class="stat-value">{{ overview.deviceCount ?? '-' }}</div>
      </div>

      <div class="ncs-card stat ncs-card--hover ncs-enter">
        <div class="stat-top">
          <span class="stat-label">故障设备</span>
          <span class="stat-ico">⚠️</span>
        </div>
        <div class="stat-value danger">{{ overview.faultCount ?? '-' }}</div>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="charts">
      <div class="ncs-card chart-card ncs-enter">
        <div ref="orderChartEl" class="chart"></div>
      </div>
      <div class="ncs-card chart-card ncs-enter">
        <div ref="revenueChartEl" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statsOverview, statsTrend } from '../api'

const overview = ref({})
const orderChartEl = ref(null)
const revenueChartEl = ref(null)
let orderChart = null
let revenueChart = null

const loadOverview = async () => {
  overview.value = await statsOverview()
}

// 订单数、收入是两种量纲，按规范拆成两个单序列图（不用双轴）
const loadTrend = async () => {
  const data = await statsTrend(7)
  const dates = data.map(d => d.date)
  const orderCounts = data.map(d => d.orderCount)
  const revenues = data.map(d => d.revenue)

  orderChart.setOption({
    title: { text: '近7日订单趋势', textStyle: { fontSize: 14, color: '#1d1d1f' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e5ea' } } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f2' } } },
    series: [{
      name: '订单数', type: 'line', data: orderCounts, smooth: true,
      color: '#22c55e', lineStyle: { width: 2.5 }, areaStyle: { color: 'rgba(34,197,94,0.10)' },
      symbolSize: 6
    }]
  })

  revenueChart.setOption({
    title: { text: '近7日收入趋势(元)', textStyle: { fontSize: 14, color: '#1d1d1f' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e5ea' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f2' } } },
    series: [{
      name: '收入', type: 'bar', data: revenues, color: '#16a34a', barMaxWidth: 30,
      itemStyle: { borderRadius: [6, 6, 0, 0] }
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
.bento {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
.hero { grid-column: span 2; }
.stat { padding: 24px; }
.stat-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.stat-label { color: var(--ncs-text-2); font-size: 13px; }
.stat-ico { font-size: 22px; }
.stat-value { font-size: 30px; font-weight: 700; letter-spacing: -0.5px; }
.hero-value { font-size: 40px; }
.danger { color: #ef4444; }
.stat-sub { color: var(--ncs-text-2); font-size: 13px; margin-top: 6px; }
.charts { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 18px; }
.chart-card { padding: 20px; }
.chart { height: 300px; }
@media (max-width: 1200px) {
  .bento { grid-template-columns: repeat(2, 1fr); }
  .charts { grid-template-columns: 1fr; }
}
</style>
