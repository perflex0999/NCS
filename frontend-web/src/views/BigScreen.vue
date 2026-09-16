<template>
  <div class="bigscreen">
    <!-- 标题栏 -->
    <div class="titlebar">
      <div class="tl-left">
        <div class="logo"></div>
        <div>
          <div class="main-title">充电桩运营数据大屏</div>
          <div class="sub-title">CHARGING OPERATION CENTER</div>
        </div>
      </div>
      <div class="tl-right">
        <div class="clock">{{ clock }}</div>
        <div class="data-through">● 实时数据</div>
        <button class="exit-btn" @click="$router.push('/dashboard')">退出大屏</button>
      </div>
    </div>

    <!-- KPI 指标带 -->
    <div class="kpi-band">
      <div v-for="(k, i) in kpis" :key="k.label" class="kpi-item ncs-panel"
        :style="{ animationDelay: (i * 60) + 'ms' }">
        <div class="kpi-label">{{ k.label }}</div>
        <div class="kpi-value" :style="{ color: k.color, textShadow: `0 0 16px ${k.color}55` }">{{ k.value }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts">
      <div class="ncs-panel chart-panel">
        <div class="panel-title"><span class="bar"></span>近7日订单趋势</div>
        <div ref="orderEl" class="chart"></div>
      </div>
      <div class="ncs-panel chart-panel">
        <div class="panel-title"><span class="bar"></span>近7日收入趋势</div>
        <div ref="revenueEl" class="chart"></div>
      </div>
    </div>

    <!-- 底部补充信息 -->
    <div class="footer-band">
      <div class="ncs-panel f-item"><span class="f-label">充电设备总数</span><span class="f-value">{{ overview.deviceCount ?? '-' }} 台</span></div>
      <div class="ncs-panel f-item"><span class="f-label">累计充电量</span><span class="f-value">{{ overview.chargedKwh ?? '-' }} kWh</span></div>
      <div class="ncs-panel f-item"><span class="f-label">故障设备</span><span class="f-value warn">{{ overview.faultCount ?? '-' }} 台</span></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statsOverview, statsTrend } from '../api'

const overview = ref({})
const kpis = ref([
  { label: '注册用户', value: '-', color: '#4D9FFF' },
  { label: '充电订单', value: '-', color: '#00E5FF' },
  { label: '总收入(元)', value: '-', color: '#2DD4A7' },
  { label: '充电设备', value: '-', color: '#A78BFA' }
])
const clock = ref('')
const orderEl = ref(null)
const revenueEl = ref(null)
let orderChart = null
let revenueChart = null
let clockTimer = null

const updateClock = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  clock.value = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const load = async () => {
  const o = await statsOverview()
  overview.value = o
  kpis.value[0].value = o.userCount
  kpis.value[1].value = o.orderCount
  kpis.value[2].value = o.revenue
  kpis.value[3].value = o.deviceCount

  const trend = await statsTrend(7)
  const dates = trend.map(d => d.date)
  const orderCounts = trend.map(d => d.orderCount)
  const revenues = trend.map(d => d.revenue)

  const axisStyle = { axisLine: { lineStyle: { color: '#1B2740' } }, axisLabel: { color: '#8A94A8' } }
  const splitStyle = { splitLine: { lineStyle: { color: '#1B2740', type: 'dashed' } } }

  orderChart.setOption({
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(18,26,41,0.95)', borderColor: '#1B2740', textStyle: { color: '#E6ECF5' } },
    xAxis: { type: 'category', data: dates, ...axisStyle },
    yAxis: { type: 'value', minInterval: 1, ...splitStyle, axisLabel: { color: '#8A94A8' } },
    series: [{ type: 'line', data: orderCounts, smooth: true, color: '#4D9FFF', lineStyle: { width: 2.5 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(77,159,255,0.3)' }, { offset: 1, color: 'rgba(77,159,255,0)' }] } } }]
  })

  revenueChart.setOption({
    grid: { left: 60, right: 20, top: 30, bottom: 30 },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(18,26,41,0.95)', borderColor: '#1B2740', textStyle: { color: '#E6ECF5' } },
    xAxis: { type: 'category', data: dates, ...axisStyle },
    yAxis: { type: 'value', ...splitStyle, axisLabel: { color: '#8A94A8' } },
    series: [{ type: 'bar', data: revenues, barMaxWidth: 28, itemStyle: { borderRadius: [6, 6, 0, 0],
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#00E5FF' }, { offset: 1, color: 'rgba(0,229,255,0.25)' }] } } }]
  })
}

const onResize = () => { orderChart && orderChart.resize(); revenueChart && revenueChart.resize() }

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  orderChart = echarts.init(orderEl.value)
  revenueChart = echarts.init(revenueEl.value)
  load()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  clearInterval(clockTimer)
  window.removeEventListener('resize', onResize)
  orderChart && orderChart.dispose()
  revenueChart && revenueChart.dispose()
})
</script>

<style scoped>
.bigscreen {
  min-height: 100vh;
  padding: 18px 22px;
  background:
    radial-gradient(ellipse 60% 50% at 18% -5%, rgba(0, 229, 255, 0.10) 0%, transparent 55%),
    radial-gradient(ellipse 55% 45% at 85% 105%, rgba(77, 159, 255, 0.12) 0%, transparent 55%),
    linear-gradient(180deg, #040A16 0%, #081120 50%, #050C18 100%);
  display: flex; flex-direction: column;
}
.titlebar { display: flex; align-items: center; justify-content: space-between; padding-bottom: 14px; border-bottom: 1px solid var(--border-strong); margin-bottom: 16px; }
.tl-left { display: flex; align-items: center; gap: 14px; }
.logo { width: 46px; height: 46px; border-radius: 50%; background: rgba(255,255,255,0.06); border: 1px solid var(--border-strong); box-shadow: 0 0 18px var(--glow-cyan); }
.main-title {
  font-size: 26px; font-weight: 700; letter-spacing: 3px;
  background: linear-gradient(90deg, #EAF0FA, #6FD4FF 45%, #00E5FF 70%, #2DD4A7);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
}
.sub-title { font-size: 11px; color: var(--text-sub); letter-spacing: 4px; margin-top: 2px; }
.tl-right { display: flex; align-items: center; gap: 16px; }
.clock { font-size: 22px; font-weight: 700; color: var(--cyan); letter-spacing: 2px; text-shadow: 0 0 12px var(--glow-cyan); }
.data-through { font-size: 12px; color: var(--green); }
.exit-btn { background: rgba(77,159,255,0.12); color: var(--blue); border: 1px solid var(--border); border-radius: 8px; padding: 6px 14px; cursor: pointer; font-size: 13px; }
.kpi-band { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 14px; }
.kpi-item { padding: 18px 20px; }
.kpi-label { font-size: 13px; color: var(--text-sub); }
.kpi-value { font-size: 32px; font-weight: 700; margin-top: 8px; font-variant-numeric: tabular-nums; }
.charts { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; flex: 1; }
.chart-panel { padding: 16px; }
.panel-title { display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--text-main); padding-bottom: 10px; }
.bar { width: 4px; height: 14px; border-radius: 2px; background: linear-gradient(180deg, var(--blue), var(--cyan)); box-shadow: 0 0 8px var(--glow-cyan); }
.chart { height: 280px; }
.footer-band { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-top: 14px; }
.f-item { padding: 14px 18px; display: flex; justify-content: space-between; align-items: center; }
.f-label { font-size: 13px; color: var(--text-sub); }
.f-value { font-size: 20px; font-weight: 700; color: var(--cyan); }
.f-value.warn { color: var(--red); }
</style>
