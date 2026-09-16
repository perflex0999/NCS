<template>
  <div class="login-page">
    <div class="brand ncs-enter">
      <div class="brand-dot"></div>
      <h1>熠熠ee</h1>
      <p>智能充电桩运营服务平台</p>
    </div>
    <div class="ncs-card login-card ncs-enter">
      <van-form @submit="onSubmit">
        <van-cell-group inset class="fields">
          <van-field v-model="phone" label="手机号" placeholder="请输入手机号" type="tel" />
          <van-field v-model="code" label="验证码" placeholder="固定验证码 123456" />
        </van-cell-group>
        <div class="btn-wrap">
          <van-button round block native-type="submit" class="ncs-gradient-btn">登录</van-button>
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
    router.replace('/home')
  } catch (e) {
    // 已由拦截器提示
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 56px 24px 24px;
  background: linear-gradient(180deg, #E3EFFB 0%, #F2F6FB 50%, #F8FAFC 100%);
}
.brand { text-align: center; margin-bottom: 28px; }
.brand-dot {
  width: 48px; height: 48px; border-radius: 16px;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0, #45D094);
  margin: 0 auto 14px;
  box-shadow: 0 8px 20px rgba(62, 201, 192, 0.3);
}
.brand h1 { margin: 0; font-size: 22px; color: #2A3240; }
.brand p { color: #8B93A1; font-size: 13px; margin: 6px 0 0; }
.login-card { margin: 0 8px; }
.fields { margin: 0; }
.fields :deep(.van-cell) { background: transparent; }
.btn-wrap { margin: 16px; }
</style>
