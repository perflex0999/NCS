<template>
  <div class="coupon-page">
    <van-nav-bar title="我的优惠券" left-arrow @click-left="$router.back()" />

    <!-- 顶部 tab -->
    <div class="tabs">
      <div class="tab" :class="{ active: tab === 'valid' }" @click="tab = 'valid'">可使用</div>
      <div class="tab" :class="{ active: tab === 'used' }" @click="tab = 'used'">已使用</div>
      <div class="tab" :class="{ active: tab === 'expired' }" @click="tab = 'expired'">已过期</div>
    </div>

    <!-- 优惠券列表 -->
    <div class="coupon-list">
      <div v-for="(c, i) in filteredCoupons" :key="c.id" class="coupon ncs-enter"
        :style="{ animationDelay: (i * 50) + 'ms' }">
        <div class="coupon-left">
          <div class="amount">¥<em>{{ c.amount }}</em></div>
          <div class="threshold">满{{ c.threshold }}可用</div>
        </div>
        <div class="coupon-divider"></div>
        <div class="coupon-right">
          <div class="title">{{ c.title }}</div>
          <div class="desc">{{ c.desc }}</div>
          <div class="expire">{{ c.expire }}</div>
        </div>
        <div class="coupon-status" v-if="tab !== 'valid'">{{ tab === 'used' ? '已使用' : '已过期' }}</div>
      </div>
      <van-empty v-if="!filteredCoupons.length" description="暂无优惠券" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const tab = ref('valid')
const coupons = ref([
  { id: 1, amount: 5, threshold: 20, title: '充电满减券', desc: '单次充电满20元可用', expire: '有效期至 2026.12.31', status: 'valid' },
  { id: 2, amount: 10, threshold: 50, title: '新用户专享券', desc: '首次充电满50元可用', expire: '有效期至 2026.12.31', status: 'valid' },
  { id: 3, amount: 3, threshold: 0, title: '无门槛优惠券', desc: '不限金额使用', expire: '有效期至 2026.11.30', status: 'used' },
  { id: 4, amount: 8, threshold: 30, title: '周末充电券', desc: '周末单次充电满30元可用', expire: '已过期 2026.09.01', status: 'expired' }
])

const filteredCoupons = computed(() => coupons.value.filter(c => c.status === tab.value))
</script>

<style scoped>
.coupon-page { min-height: 100vh; }
.tabs { display: flex; padding: 12px 16px; gap: 10px; }
.tab { padding: 7px 16px; border-radius: 16px; font-size: 13px; color: #8B93A1; background: #F4F7FB; cursor: pointer; }
.tab.active { background: linear-gradient(90deg, #5EA8FF, #3EC9C0); color: #fff; font-weight: 600; }
.coupon-list { padding: 0 16px 16px; display: flex; flex-direction: column; gap: 12px; }
.coupon {
  position: relative; display: flex; align-items: center;
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 4px 14px rgba(40, 60, 90, 0.06);
}
.coupon-left {
  width: 104px; padding: 18px 12px; text-align: center;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0, #45D094);
  color: #fff;
}
.amount { font-size: 14px; }
.amount em { font-size: 26px; font-weight: 700; font-style: normal; }
.threshold { font-size: 11px; opacity: 0.9; margin-top: 2px; }
.coupon-divider { width: 0; border-left: 1px dashed #E6EBF2; height: 56px; }
.coupon-right { flex: 1; padding: 14px; }
.title { font-size: 15px; font-weight: 700; color: #2A3240; }
.desc { font-size: 12px; color: #8B93A1; margin-top: 3px; }
.expire { font-size: 11px; color: #B0B7C3; margin-top: 6px; }
.coupon-status {
  position: absolute; right: 10px; top: 10px;
  font-size: 10px; color: #B0B7C3; background: #F4F7FB; padding: 2px 8px; border-radius: 999px;
}
</style>
