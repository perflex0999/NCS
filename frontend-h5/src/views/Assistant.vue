<template>
  <div class="assistant-page">
    <van-nav-bar title="AI 智能助手" left-arrow @click-left="$router.back()">
      <template #right>
        <span class="clear-btn" @click="clearHistory">清空</span>
      </template>
    </van-nav-bar>

    <!-- 场景切换 -->
    <div class="scenario-tabs">
      <div v-for="s in scenarios" :key="s.value" class="tab"
        :class="{ active: scenario === s.value }" @click="switchScenario(s.value)">
        <span class="tab-ico">{{ s.icon }}</span>{{ s.text }}
      </div>
    </div>

    <!-- 消息区 -->
    <div class="messages" ref="messagesEl">
      <div v-if="!messages.length && !streaming" class="welcome">
        <div class="welcome-ico">🤖</div>
        <div class="welcome-title">你好，我是{{ currentScenario.text }}</div>
        <div class="welcome-sub">试着问我：{{ currentScenario.hint }}</div>
      </div>
      <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
        <div class="bubble" :class="m.role" v-html="renderMd(m.content)"></div>
      </div>
      <div v-if="streaming" class="msg assistant">
        <div class="bubble assistant"><span class="cursor">▍</span><span v-html="renderMd(streamingText)"></span></div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-bar">
      <textarea v-model="input" class="input-text" rows="1"
        :placeholder="'问' + currentScenario.text + '...'" @keydown.enter.exact.prevent="send" />
      <button class="send-btn" :disabled="streaming || !input.trim()" @click="send">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { showToast } from 'vant'
import { marked } from 'marked'
import { getToken } from '../utils/auth'
import { agentHistory, clearAgentHistory } from '../api'

// Markdown 渲染：**加粗**、换行、列表、标题（breaks:true 让单个换行也渲染成 <br>）
const renderMd = (text) => marked.parse(text || '', { breaks: true })

const scenarios = [
  { value: 'user_assistant', text: '用户充电助手', icon: '⚡', hint: '「附近哪里有空闲的快充？」' },
  { value: 'fault_consult', text: '智能故障咨询', icon: '🔧', hint: '「为什么我的充电桩无法启动？」' }
]
const scenario = ref('user_assistant')
const messages = ref([])
const input = ref('')
const streaming = ref(false)
const streamingText = ref('')
const messagesEl = ref(null)

const currentScenario = () => scenarios.find(s => s.value === scenario.value)

const scrollToBottom = async () => {
  await nextTick()
  if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
}

const loadHistory = async () => {
  try {
    const list = await agentHistory(scenario.value)
    messages.value = list.map(h => ({ role: h.role, content: h.content }))
    scrollToBottom()
  } catch (e) {}
}

const switchScenario = (v) => {
  scenario.value = v
  streamingText.value = ''
  messages.value = []
  loadHistory()
}

const clearHistory = async () => {
  try {
    await clearAgentHistory(scenario.value)
    messages.value = []
    showToast('已清空')
  } catch (e) {}
}

// 流式调用（原生 fetch + ReadableStream 解析 SSE）
const streamChat = async (scenarioVal, message, onChunk) => {
  const token = getToken()
  const res = await fetch('/api/agent/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': token },
    body: JSON.stringify({ message, scenario: scenarioVal })
  })
  if (!res.ok || !res.body) throw new Error('请求失败')
  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let idx
    while ((idx = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, idx).trim()
      buffer = buffer.slice(idx + 1)
      if (line.startsWith('data:')) {
        const raw = line.slice(5).trim()
        if (raw && raw !== '[DONE]') {
          try { onChunk(JSON.parse(raw)) } catch (e) { onChunk(raw) }
        }
      }
    }
  }
}

const send = async () => {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  streaming.value = true
  streamingText.value = ''
  scrollToBottom()
  try {
    await streamChat(scenario.value, text, (chunk) => {
      streamingText.value += chunk
      scrollToBottom()
    })
  } catch (e) {
    streamingText.value += '（连接失败，请重试）'
  }
  if (streamingText.value) {
    messages.value.push({ role: 'assistant', content: streamingText.value })
  }
  streamingText.value = ''
  streaming.value = false
  scrollToBottom()
}

onMounted(loadHistory)
</script>

<style scoped>
.assistant-page { display: flex; flex-direction: column; height: 100vh; }
.clear-btn { font-size: 13px; color: #8B93A1; }
.scenario-tabs { display: flex; gap: 10px; padding: 10px 14px; background: #fff; }
.tab { flex: 1; text-align: center; padding: 8px 0; border-radius: 18px; font-size: 13px; color: #8B93A1; background: #F4F7FB; cursor: pointer; }
.tab.active { background: linear-gradient(90deg, #5EA8FF, #3EC9C0); color: #fff; font-weight: 600; }
.tab-ico { margin-right: 4px; }
.messages { flex: 1; overflow-y: auto; padding: 16px 14px; }
.welcome { text-align: center; padding-top: 60px; }
.welcome-ico { font-size: 48px; }
.welcome-title { font-size: 17px; font-weight: 600; margin-top: 12px; color: #2A3240; }
.welcome-sub { font-size: 13px; color: #8B93A1; margin-top: 8px; }
.msg { display: flex; margin-bottom: 14px; }
.msg.user { justify-content: flex-end; }
.msg.assistant { justify-content: flex-start; }
.bubble { max-width: 78%; padding: 11px 14px; border-radius: 16px; font-size: 14px; line-height: 1.5; white-space: pre-wrap; word-break: break-word; }
.bubble.user { background: linear-gradient(90deg, #5EA8FF, #3EC9C0); color: #fff; border-bottom-right-radius: 4px; }
.bubble.assistant { background: #fff; color: #2A3240; border: 1px solid #EEF1F6; border-bottom-left-radius: 4px; }
.cursor { color: #3EC9C0; animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }
.bubble :deep(p) { margin: 0 0 6px; }
.bubble :deep(p:last-child) { margin-bottom: 0; }
.bubble :deep(strong) { font-weight: 700; }
.bubble :deep(ul), .bubble :deep(ol) { margin: 4px 0; padding-left: 1.2em; }
.bubble :deep(li) { margin: 2px 0; }
.bubble :deep(h1), .bubble :deep(h2), .bubble :deep(h3), .bubble :deep(h4) { font-size: 15px; font-weight: 700; margin: 6px 0; }
.bubble :deep(code) { background: #F4F7FB; padding: 1px 5px; border-radius: 4px; font-size: 13px; }
.input-bar { display: flex; align-items: flex-end; gap: 10px; padding: 10px 14px; background: #fff; border-top: 1px solid #EEF1F6; }
.input-text { flex: 1; resize: none; border: 1px solid #E6EBF2; border-radius: 20px; padding: 10px 16px; font-size: 14px; line-height: 1.4; max-height: 100px; outline: none; font-family: inherit; }
.send-btn { background: linear-gradient(90deg, #5EA8FF, #3EC9C0); color: #fff; border: none; border-radius: 20px; padding: 10px 18px; font-size: 14px; font-weight: 600; cursor: pointer; }
.send-btn:disabled { opacity: 0.5; }
</style>
