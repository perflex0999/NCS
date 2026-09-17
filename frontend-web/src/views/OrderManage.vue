<template>
  <div>
    <el-form inline>
      <el-form-item label="用户ID">
        <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 140px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="充电站">
        <el-select v-model="query.stationId" clearable filterable placeholder="全部" style="width: 180px">
          <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="充电中" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="已支付" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
      </el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </el-form>

    <el-table :data="list" border>
      <el-table-column prop="orderNo" label="订单编号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="充电站" width="150">
        <template #default="{ row }">{{ stationName(row.stationId) }}</template>
      </el-table-column>
      <el-table-column prop="deviceNo" label="设备" width="110" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="chargedKwh" label="充电量(kWh)" width="100" />
      <el-table-column prop="amount" label="金额(元)" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="load"
      style="margin-top: 16px;"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { stationList, orderList } from '../api'

const list = ref([])
const stations = ref([])
const dateRange = ref(null)
const query = reactive({ userId: null, stationId: null, status: null })
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusTextMap = { 0: '充电中', 1: '已完成', 2: '已支付' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'info' }
const statusText = (s) => statusTextMap[s] ?? '未知'
const statusTag = (s) => statusTagMap[s] ?? 'info'

const stationName = (id) => stations.value.find(s => s.id === id)?.name || '-'

const load = async () => {
  const res = await orderList({
    userId: query.userId || null,
    stationId: query.stationId || null,
    status: query.status ?? null,
    startTime: dateRange.value?.[0] || null,
    endTime: dateRange.value?.[1] || null,
    page: page.value,
    pageSize: pageSize.value
  })
  list.value = res.records || []
  total.value = res.total || 0
}
const handleSearch = () => { page.value = 1; load() }
const handleSizeChange = () => { page.value = 1; load() }

onMounted(async () => {
  stations.value = await stationList()
  load()
})
</script>
