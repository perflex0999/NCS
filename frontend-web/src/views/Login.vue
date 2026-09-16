<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>充电运营管理后台</h2>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="code" placeholder="验证码（固定 123456）" />
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="onSubmit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { setToken } from '../utils/auth'

const router = useRouter()
const phone = ref('13800000001')
const code = ref('123456')

const onSubmit = async () => {
  try {
    const data = await login(phone.value, code.value)
    setToken(data.token)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch (e) {
    // 已由拦截器提示
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-card { width: 360px; }
.login-card h2 { text-align: center; margin-bottom: 20px; }
</style>
