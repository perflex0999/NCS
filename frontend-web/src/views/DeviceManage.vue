<template>
  <div>
    <el-form inline>
      <el-form-item label="充电站">
        <el-select v-model="stationId" clearable filterable placeholder="全部" style="width: 200px" @change="handleFilterChange">
          <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="openCreate">新增充电桩</el-button>
    </el-form>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="deviceNo" label="设备编号" />
      <el-table-column label="所属站" width="160">
        <template #default="{ row }">{{ stationName(row.stationId) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ row.deviceType === 1 ? '快充' : '慢充' }}</template>
      </el-table-column>
      <el-table-column prop="powerKw" label="功率(kW)" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑充电桩' : '新增充电桩'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="设备编号"><el-input v-model="form.deviceNo" /></el-form-item>
        <el-form-item label="所属充电站">
          <el-select v-model="form.stationId" filterable style="width: 100%">
            <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.deviceType">
            <el-option label="快充" :value="1" />
            <el-option label="慢充" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="功率(kW)"><el-input v-model="form.powerKw" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="空闲" :value="0" />
            <el-option label="使用中" :value="1" />
            <el-option label="故障" :value="2" />
            <el-option label="离线" :value="3" />
            <el-option label="维修中" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { stationList, deviceList, deviceCreate, deviceUpdate, deviceDelete } from '../api'

const list = ref([])
const stations = ref([])
const stationId = ref(null)
const dialogVisible = ref(false)
const form = ref({})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusTextMap = { 0: '空闲', 1: '使用中', 2: '故障', 3: '离线', 4: '维修中' }
const statusTagMap = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'info', 4: 'primary' }
const statusText = (s) => statusTextMap[s] ?? '未知'
const statusTag = (s) => statusTagMap[s] ?? 'info'

const stationName = (id) => stations.value.find(s => s.id === id)?.name || '-'

const load = async () => {
  const res = await deviceList({ stationId: stationId.value, page: page.value, pageSize: pageSize.value })
  list.value = res.records || []
  total.value = res.total || 0
}
const handleFilterChange = () => { page.value = 1; load() }
const handleSizeChange = () => { page.value = 1; load() }

const loadStations = async () => { stations.value = await stationList() }

const openCreate = () => { form.value = { deviceType: 1, status: 0 }; dialogVisible.value = true }
const openEdit = (row) => { form.value = { ...row }; dialogVisible.value = true }

const save = async () => {
  if (form.value.id) await deviceUpdate(form.value.id, form.value)
  else await deviceCreate(form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该充电桩？', '提示', { type: 'warning' })
  await deviceDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => { loadStations(); load() })
</script>
