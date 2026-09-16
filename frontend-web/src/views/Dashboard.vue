<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <div ref="orderChartEl" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="revenueChartEl" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statsOverview, statsTrend } from '../api'

const cards = ref([
  { label: '用户数', value: '-' },
  { label: '订单数', value: '-' },
  { label: '充电量(kWh)', value: '-' },
  { label: '收入(元)', value: '-' },
  { label: '设备数', value: '-' },
  { label: '故障数', value: '-' }
])

const orderChartEl = ref(null)
const revenueChartEl = ref(null)
let orderChart = null
let revenueChart = null

const loadOverview = async () => {
  const d = await statsOverview()
  cards.value[0].value = d.userCount
  cards.value[1].value = d.orderCount
  cards.value[2].value = d.chargedKwh
  cards.value[3].value = d.revenue
  cards.value[4].value = d.deviceCount
  cards.value[5].value = d.faultCount
}

// 订单数、收入是两种量纲，按规范拆成两个单序列图（不用双轴）
const loadTrend = async () => {
  const data = await statsTrend(7)
  const dates = data.map(d => d.date)
  const orderCounts = data.map(d => d.orderCount)
  const revenues = data.map(d => d.revenue)

  orderChart.setOption({
    title: { text: '近7日订单趋势', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '订单数',
      type: 'line',
      data: orderCounts,
      smooth: true,
      color: '#409EFF',
      areaStyle: { opacity: 0.15 }
    }]
  })

  revenueChart.setOption({
    title: { text: '近7日收入趋势(元)', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{
      name: '收入',
      type: 'bar',
      data: revenues,
      color: '#67C23A',
      barMaxWidth: 32
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
.stat-label { color: #909399; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: bold; margin-top: 6px; }
.chart { height: 300px; }
</style>
