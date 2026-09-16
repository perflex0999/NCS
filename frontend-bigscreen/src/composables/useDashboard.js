import { ref, onMounted, onBeforeUnmount } from 'vue'
import fallbackData from '../data/dashboard.json'

// 大屏数据源：从本仓库后端拉取真实数据(需 admin 登录拿 token)；
// ML 负荷预测读 ml_data 离线产物(已同步到 web/public/data/)。
// 后端不可达 / CORS 被拦 / file:// 双击打开时回退到 fallbackData(真实种子数据)。
let adminToken = ''

async function ensureToken() {
  if (adminToken) return adminToken
  const lr = await fetch('/api/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' })
  })
  const j = await lr.json()
  adminToken = (j && j.data && j.data.token) || ''
  return adminToken
}

async function apiJson(path, { auth = true } = {}) {
  const res = await fetch(path, {
    cache: 'no-store',
    headers: auth ? { Authorization: 'Bearer ' + adminToken } : {}
  })
  const body = await res.json()
  if (!body || body.code !== 0) throw new Error('后端业务错误')
  return body.data
}

function yuan(cents) {
  return Math.round((cents || 0) / 100 * 100) / 100
}
function round2(x) {
  return Math.round((x || 0) * 100) / 100
}

// 功率档位(与后端 power_tier 一致)：慢 <30 / 快 30–180 / 超 ≥180 kW
function powerTier(kw) {
  if (kw >= 180) return 'ultra'
  if (kw >= 30) return 'fast'
  return 'slow'
}

const DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 近 30 日逐日注册用户数(registered_at 为 UTC ISO)
function buildUserGrowth(users) {
  const days = 30
  const counts = {}
  for (const u of users || []) {
    if (!u.registered_at) continue
    const d = String(u.registered_at).slice(0, 10) // yyyy-MM-dd(UTC)
    counts[d] = (counts[d] || 0) + 1
  }
  const out = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(Date.now() - i * 86400000)
    const key = d.toISOString().slice(0, 10)
    out.push({ date: key.slice(5), count: counts[key] || 0 })
  }
  return out
}

async function loadBackend() {
  const res = await fetch('/api/bigscreen/data', { cache: 'no-store' })
  const body = await res.json()
  if (!body || body.code !== 200) throw new Error('后端业务错误')
  return body.data
}

export function useDashboard(interval = 30000) {
  const data = ref(fallbackData)
  const error = ref(false)
  let timer = null

  async function fetchData() {
    try {
      data.value = await loadBackend()
      error.value = false
    } catch (e) {
      error.value = true
      // 保留 fallbackData(真实种子数据)，不覆盖
    }
  }

  onMounted(() => {
    fetchData()
    timer = setInterval(fetchData, interval)
  })

  onBeforeUnmount(() => {
    if (timer) clearInterval(timer)
  })

  return { data, error, refresh: fetchData }
}
