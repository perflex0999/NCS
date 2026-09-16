<template>
  <div class="login-wrap">
    <div class="login-card ncs-enter">
      <div class="brand">
        <div class="brand-dot"></div>
        <h1>充电运营管理后台</h1>
      </div>
      <p class="sub">智能充电桩运营服务平台</p>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="phone" placeholder="手机号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="code" placeholder="验证码（固定 123456）" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" @click="onSubmit">登录</el-button>
      </el-form>
    </div>
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
  background: linear-gradient(135deg, #f0fdf4 0%, #f5f5f7 50%, #dcfce7 100%);
}
.login-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06), 0 24px 60px rgba(0, 0, 0, 0.08);
  padding: 40px;
  width: 380px;
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-dot {
  width: 14px; height: 14px; border-radius: 50%;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.15);
}
.brand h1 { font-size: 20px; margin: 0; }
.sub { color: var(--ncs-text-2); font-size: 13px; margin: 8px 0 24px; }
.submit { width: 100%; margin-top: 8px; }
</style>
