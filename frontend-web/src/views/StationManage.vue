<template>
  <div>
    <el-button type="primary" @click="openCreate">新增充电站</el-button>
    <el-table :data="list" border style="margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="businessHours" label="营业时间" width="120" />
      <el-table-column prop="contact" label="联系方式" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '营业' : '停业' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑充电站' : '新增充电站'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="纬度"><el-input v-model="form.lat" /></el-form-item>
        <el-form-item label="经度"><el-input v-model="form.lng" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" /></el-form-item>
        <el-form-item label="停车说明"><el-input v-model="form.parkingInfo" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="营业" :value="1" />
            <el-option label="停业" :value="0" />
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
import { stationList, stationCreate, stationUpdate, stationDelete } from '../api'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({})

const load = async () => { list.value = await stationList() }
const openCreate = () => { form.value = { status: 1 }; dialogVisible.value = true }
const openEdit = (row) => { form.value = { ...row }; dialogVisible.value = true }

const save = async () => {
  if (form.value.id) await stationUpdate(form.value.id, form.value)
  else await stationCreate(form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该充电站？', '提示', { type: 'warning' })
  await stationDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
