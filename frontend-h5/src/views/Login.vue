<template>
  <div class="login-page">
    <div class="brand ncs-enter">
      <div class="brand-dot"></div>
      <h1>充电服务</h1>
      <p>智能充电桩运营服务平台</p>
    </div>
    <div class="ncs-card login-card ncs-enter">
      <van-form @submit="onSubmit">
        <van-cell-group inset class="fields">
          <van-field v-model="phone" label="手机号" placeholder="请输入手机号" type="tel" />
          <van-field v-model="code" label="验证码" placeholder="固定验证码 123456" />
        </van-cell-group>
        <div class="btn-wrap">
          <van-button round block type="primary" native-type="submit">登录</van-button>
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
  padding: 60px 24px 24px;
  background: linear-gradient(160deg, #dcfce7 0%, #f5f5f7 45%, #f0fdf4 100%);
}
.brand { text-align: center; margin-bottom: 28px; }
.brand-dot {
  width: 48px; height: 48px; border-radius: 16px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  margin: 0 auto 14px;
  box-shadow: 0 8px 20px rgba(34, 197, 94, 0.3);
}
.brand h1 { margin: 0; font-size: 22px; }
.brand p { color: #6e6e73; font-size: 13px; margin: 6px 0 0; }
.login-card { margin: 0 8px; }
.fields { margin: 0; }
.btn-wrap { margin: 16px; }
</style>
