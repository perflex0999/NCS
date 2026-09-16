<template>
  <div class="profile-page">
    <!-- 顶部品牌条 + 用户卡片 -->
    <div class="header ncs-enter">
      <div class="brand-bar"></div>
      <div class="user-card ncs-card">
        <div class="avatar">充</div>
        <div class="user-info">
          <div class="nickname">{{ nickname || '测试用户1' }}</div>
          <div class="phone">{{ phone || '138****0001' }}</div>
        </div>
      </div>
    </div>

    <!-- 余额卡片（蓝绿渐变） -->
    <div class="balance ncs-enter" style="animation-delay:80ms">
      <div class="balance-label">账户余额</div>
      <div class="balance-value">¥ 100.00</div>
      <div class="balance-sub">赠送积分 200</div>
    </div>

    <!-- 功能入口 -->
    <div class="entries ncs-card ncs-enter" style="animation-delay:160ms">
      <div class="entry" @click="$router.push('/orders')">
        <span class="entry-ico">🧾</span><span>我的订单</span><span class="arrow">›</span>
      </div>
      <div class="entry" @click="$router.push('/scan')">
        <span class="entry-ico">⚡</span><span>扫码充电</span><span class="arrow">›</span>
      </div>
      <div class="entry">
        <span class="entry-ico">🎫</span><span>我的优惠券</span><span class="arrow">›</span>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-wrap">
      <van-button round block class="logout-btn" @click="logout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { clearToken } from '../utils/auth'

const router = useRouter()
const nickname = ref('')
const phone = ref('')

const logout = () => {
  clearToken()
  showToast('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.profile-page { padding: 14px; min-height: 100%; display: flex; flex-direction: column; gap: 14px; }
.header { position: relative; padding-top: 20px; }
.brand-bar {
  position: absolute; top: 0; left: 6px; right: 6px; height: 6px; border-radius: 3px;
  background: linear-gradient(90deg, #5EA8FF, #3EC9C0, #45D094);
}
.user-card { display: flex; align-items: center; gap: 14px; padding: 18px; }
.avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0);
  color: #fff; font-size: 24px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.nickname { font-size: 17px; font-weight: 600; color: #2A3240; }
.phone { font-size: 13px; color: #8B93A1; margin-top: 4px; }
.balance {
  border-radius: 20px; padding: 22px; color: #fff;
  background: linear-gradient(135deg, #5EA8FF, #3EC9C0 60%, #45D094);
  box-shadow: 0 10px 26px rgba(62, 201, 192, 0.28);
}
.balance-label { font-size: 13px; opacity: 0.9; }
.balance-value { font-size: 34px; font-weight: 700; margin-top: 6px; }
.balance-sub { font-size: 12px; opacity: 0.85; margin-top: 6px; }
.entries { padding: 6px 16px; }
.entry {
  display: flex; align-items: center; gap: 12px;
  padding: 15px 4px; font-size: 15px; color: #2A3240;
  border-bottom: 1px solid #F4F7FB; cursor: pointer;
}
.entry:last-child { border-bottom: none; }
.entry-ico { font-size: 20px; }
.arrow { margin-left: auto; color: #C0C7D2; font-size: 20px; }
.logout-wrap { margin-top: auto; padding: 8px 20px 4px; }
.logout-btn { color: #F06A6A; background: #fff; border: 1px solid #FAD2D2; }
</style>
