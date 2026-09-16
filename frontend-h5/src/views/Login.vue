<template>
  <div class="login-page">
    <div class="brand ncs-enter">
      <div class="brand-dot"></div>
      <h1>充电服务</h1>
      <p>智能充电桩运营服务平台</p>
    </div>
    <div class="ncs-panel login-card ncs-enter">
      <van-form @submit="onSubmit">
        <van-cell-group inset class="fields">
          <van-field v-model="phone" label="手机号" placeholder="请输入手机号" type="tel" />
          <van-field v-model="code" label="验证码" placeholder="固定验证码 123456" />
        </van-cell-group>
        <div class="btn-wrap">
          <van-button round block type="primary" native-type="submit" class="login-btn">登录</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '../api'
import { setToken } from '../utils/auth'

const router = useRouter()
const phone = ref('13800000001')
const code = ref('123456')

const onSubmit = async () => {
  if (!phone.value || !code.value) {
    showToast('请输入手机号和验证码')
    return
  }
  try {
    const data = await login(phone.value, code.value)
    setToken(data.token)
    showToast('登录成功')
    router.replace('/stations')
  } catch (e) {
    // 已由拦截器提示
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 56px 24px 24px;
  background:
    radial-gradient(ellipse 70% 50% at 20% -10%, rgba(0, 229, 255, 0.14) 0%, transparent 55%),
    radial-gradient(ellipse 60% 45% at 90% 110%, rgba(77, 159, 255, 0.16) 0%, transparent 55%),
    linear-gradient(180deg, #040A16, #081120 55%, #050C18);
}
.brand { text-align: center; margin-bottom: 28px; }
.brand-dot {
  width: 48px; height: 48px; border-radius: 16px;
  background: linear-gradient(135deg, var(--blue), var(--cyan));
  margin: 0 auto 14px;
  box-shadow: 0 0 24px var(--glow-cyan);
}
.brand h1 {
  margin: 0; font-size: 22px; color: var(--text-main);
  background: linear-gradient(90deg, #EAF0FA, #6FD4FF, #00E5FF);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
}
.brand p { color: var(--text-sub); font-size: 13px; margin: 6px 0 0; }
.login-card { margin: 0 8px; }
.fields { margin: 0; }
.fields :deep(.van-cell) { background: transparent; }
.btn-wrap { margin: 16px; }
.login-btn { background: linear-gradient(90deg, #4D9FFF, #00E5FF); border: none; color: #040A16; font-weight: 700; }
</style>
