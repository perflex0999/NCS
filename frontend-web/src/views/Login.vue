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
  background:
    radial-gradient(ellipse 60% 50% at 20% -10%, rgba(0, 229, 255, 0.14) 0%, transparent 55%),
    radial-gradient(ellipse 55% 45% at 85% 110%, rgba(77, 159, 255, 0.16) 0%, transparent 55%),
    linear-gradient(180deg, #040A16, #081120 55%, #050C18);
}
.login-card {
  background: rgba(13, 23, 42, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid var(--border);
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5), 0 0 30px rgba(77, 159, 255, 0.08);
  padding: 40px;
  width: 380px;
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-dot {
  width: 14px; height: 14px; border-radius: 50%;
  background: linear-gradient(135deg, var(--blue), var(--cyan));
  box-shadow: 0 0 14px var(--glow-cyan);
}
.brand h1 {
  font-size: 20px; margin: 0; color: var(--text-main);
  background: linear-gradient(90deg, #EAF0FA, #6FD4FF, #00E5FF);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
}
.sub { color: var(--text-sub); font-size: 13px; margin: 8px 0 24px; }
.submit { width: 100%; margin-top: 8px; background: linear-gradient(90deg, #4D9FFF, #00E5FF); border: none; color: #040A16; font-weight: 700; }
</style>
