<template>
  <div class="recharge-page">
    <van-nav-bar title="账户充值" left-arrow @click-left="$router.back()" />

    <div class="ncs-card balance-card ncs-enter">
      <div class="lbl">当前余额（元）</div>
      <div class="val">¥{{ balance.toFixed(2) }}</div>
    </div>

    <div class="ncs-card amount-card ncs-enter" style="animation-delay:80ms">
      <div class="amount-title">选择充值金额</div>
      <div class="amounts">
        <div v-for="a in amounts" :key="a" class="amount-item"
          :class="{ active: selected === a }" @click="selected = a">
          <div class="amt-val">¥{{ a }}</div>
        </div>
      </div>
    </div>

    <div class="pay-wrap">
      <van-button round block class="ncs-gradient-btn" @click="recharge">确认充值 ¥{{ selected }}</van-button>
      <p class="pay-hint">模拟支付，点击即充值成功</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const amounts = [20, 50, 100, 200]
const selected = ref(50)
const balance = ref(Number(localStorage.getItem('ncs_balance') || 100))

const recharge = () => {
  balance.value += selected.value
  localStorage.setItem('ncs_balance', balance.value)
  showToast(`充值成功 ¥${selected.value}`)
  setTimeout(() => router.back(), 600)
}
</script>

<style scoped>
.recharge-page { min-height: 100vh; padding-bottom: 30px; }
.balance-card { margin: 14px 16px; padding: 20px; text-align: center; }
.lbl { font-size: 13px; color: #8B93A1; }
.val { font-size: 32px; font-weight: 700; color: #3EC9C0; margin-top: 6px; }
.amount-card { margin: 0 16px; padding: 18px; }
.amount-title { font-size: 14px; font-weight: 600; color: #2A3240; margin-bottom: 12px; }
.amounts { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.amount-item {
  padding: 18px 0; text-align: center; border-radius: 12px;
  background: #F4F7FB; border: 1px solid transparent; cursor: pointer;
  transition: all 0.2s ease;
}
.amount-item.active {
  background: linear-gradient(90deg, #5EA8FF, #3EC9C0);
  color: #fff; font-weight: 700;
}
.amt-val { font-size: 18px; font-weight: 700; }
.pay-wrap { margin: 18px 16px; }
.pay-hint { text-align: center; font-size: 12px; color: #B0B7C3; margin-top: 10px; }
</style>
