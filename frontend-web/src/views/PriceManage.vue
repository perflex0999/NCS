<template>
  <div>
    <el-form inline>
      <el-form-item label="充电站">
        <el-select v-model="stationId" clearable filterable placeholder="全部" style="width: 200px" @change="handleFilterChange">
          <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="openCreate">新增价格</el-button>
    </el-form>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="充电站" width="180">
        <template #default="{ row }">{{ stationName(row.stationId) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ row.deviceType === 1 ? '快充' : '慢充' }}</template>
      </el-table-column>
      <el-table-column prop="startTime" label="时段开始" width="110" />
      <el-table-column prop="endTime" label="时段结束" width="110" />
      <el-table-column prop="elecPrice" label="电费(元/kWh)" width="110" />
      <el-table-column prop="servicePrice" label="服务费(元/kWh)" width="120" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑价格' : '新增价格'" width="500px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="充电站">
          <el-select v-model="form.stationId" filterable style="width: 100%">
            <el-option v-for="s in stations" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="form.deviceType">
            <el-option label="快充" :value="1" />
            <el-option label="慢充" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="时段开始">
          <el-time-picker v-model="form.startTime" value-format="HH:mm:ss" format="HH:mm:ss" placeholder="开始时间" />
        </el-form-item>
        <el-form-item label="时段结束">
          <el-time-picker v-model="form.endTime" value-format="HH:mm:ss" format="HH:mm:ss" placeholder="结束时间" />
        </el-form-item>
        <el-form-item label="电费(元/kWh)"><el-input v-model="form.elecPrice" /></el-form-item>
        <el-form-item label="服务费(元/kWh)"><el-input v-model="form.servicePrice" /></el-form-item>
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
import { stationList, priceList, priceCreate, priceUpdate, priceDelete } from '../api'

const list = ref([])
const stations = ref([])
const stationId = ref(null)
const dialogVisible = ref(false)
const form = ref({})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stationName = (id) => stations.value.find(s => s.id === id)?.name || '-'

const load = async () => {
  const res = await priceList({ stationId: stationId.value, page: page.value, pageSize: pageSize.value })
  list.value = res.records || []
  total.value = res.total || 0
}
const handleFilterChange = () => { page.value = 1; load() }
const handleSizeChange = () => { page.value = 1; load() }
const loadStations = async () => { stations.value = await stationList() }

const openCreate = () => { form.value = { deviceType: 1 }; dialogVisible.value = true }
const openEdit = (row) => { form.value = { ...row }; dialogVisible.value = true }

const save = async () => {
  if (form.value.id) await priceUpdate(form.value.id, form.value)
  else await priceCreate(form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该价格？', '提示', { type: 'warning' })
  await priceDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => { loadStations(); load() })
</script>
