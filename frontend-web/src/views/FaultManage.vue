<template>
  <div>
    <el-button type="primary" @click="openCreate">记录故障</el-button>

    <el-table :data="list" border style="margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="deviceNo" label="故障设备" width="130" />
      <el-table-column prop="faultType" label="故障类型" width="140" />
      <el-table-column prop="faultTime" label="故障时间" width="170" />
      <el-table-column prop="description" label="故障描述" />
      <el-table-column label="处理状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">处理</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '处理故障' : '记录故障'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="故障设备"><el-input v-model="form.deviceNo" placeholder="如 DEV-3001" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="故障类型"><el-input v-model="form.faultType" /></el-form-item>
        <el-form-item label="故障描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="form.status">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
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
import { faultList, faultCreate, faultUpdate, faultDelete } from '../api'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({})

const statusTextMap = { 0: '待处理', 1: '处理中', 2: '已处理' }
const statusTagMap = { 0: 'danger', 1: 'warning', 2: 'success' }
const statusText = (s) => statusTextMap[s] ?? '未知'
const statusTag = (s) => statusTagMap[s] ?? 'info'

const load = async () => { list.value = await faultList() }
const openCreate = () => { form.value = { status: 0 }; dialogVisible.value = true }
const openEdit = (row) => { form.value = { ...row }; dialogVisible.value = true }

const save = async () => {
  if (form.value.id) await faultUpdate(form.value.id, form.value)
  else await faultCreate(form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该故障记录？', '提示', { type: 'warning' })
  await faultDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
