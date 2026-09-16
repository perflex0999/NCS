<template>
  <div>
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.back()" />
    <van-cell-group inset v-if="orders.length">
      <van-cell v-for="o in orders" :key="o.orderNo" :title="o.stationName"
        :label="`${o.orderNo}\n${o.startTime} - ${o.endTime || '进行中'}`">
        <template #value>
          <div class="amount">¥{{ o.amount ?? '-' }}</div>
          <div class="status">{{ o.statusDesc }}</div>
        </template>
      </van-cell>
    </van-cell-group>
    <van-empty v-else description="暂无订单" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myOrders } from '../api'

const orders = ref([])

onMounted(async () => {
  try {
    orders.value = await myOrders()
  } catch (e) {
    // 已由拦截器提示
  }
})
</script>

<style scoped>
.amount { color: #ee0a24; font-weight: bold; }
.status { color: #969799; font-size: 12px; }
</style>
