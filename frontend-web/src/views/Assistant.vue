<template>
  <div class="assistant-page">
    <!-- 场景切换 -->
    <div class="scenario-tabs">
      <div v-for="s in scenarios" :key="s.value" class="tab"
        :class="{ active: scenario === s.value }" @click="switchScenario(s.value)">
        <span class="tab-ico">{{ s.icon }}</span>{{ s.text }}
      </div>
      <span class="clear-btn" @click="clearHistory">清空历史</span>
    </div>

    <!-- 消息区 -->
    <div class="messages" ref="messagesEl">
      <div v-if="!messages.length && !streaming" class="welcome">
        <div class="welcome-ico">🤖</div>
        <div class="welcome-title">{{ currentScenario.text }}</div>
        <div class="welcome-sub">试试问：{{ currentScenario.hint }}</div>
      </div>
      <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
        <div class="bubble" :class="m.role">{{ m.content }}</div>
      </div>
      <div v-if="streaming" class="msg assistant">
        <div class="bubble assistant"><span class="cursor">▍</span>{{ streamingText }}</div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-bar">
      <textarea v-model="input" class="input-text" rows="2"
        :placeholder="'问' + currentScenario.text + '...'" @keydown.enter.exact.prevent="send" />
      <button class="send-btn" :disabled="streaming || !input.trim()" @click="send">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getToken } from '../utils/auth'
import { agentHistory, clearAgentHistory } from '../api'

const scenarios = [
  { value: 'ops_assistant', text: 'AI 运营助手', icon: '📊', hint: '「今天哪个充电站订单最多？」' },
  { value: 'ops_report', text: 'AI 运营报告', icon: '📈', hint: '「帮我生成一份本周运营报告」' }
]
const scenario = ref('ops_assistant')
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
    ElMessage.success('已清空')
  } catch (e) {}
}

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
        const content = line.slice(5).trim()
        if (content && content !== '[DONE]') onChunk(content)
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
.assistant-page { display: flex; flex-direction: column; height: 100%; }
.scenario-tabs { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.tab { padding: 8px 18px; border-radius: 18px; font-size: 13px; color: var(--text-sub); background: rgba(15, 26, 46, 0.6); border: 1px solid var(--border); cursor: pointer; transition: all 0.2s; }
.tab.active { background: linear-gradient(90deg, #4D9FFF, #00E5FF); color: #040A16; font-weight: 600; border-color: transparent; }
.tab-ico { margin-right: 4px; }
.clear-btn { margin-left: auto; font-size: 12px; color: var(--text-sub); cursor: pointer; }
.messages { flex: 1; overflow-y: auto; padding: 8px 4px; }
.welcome { text-align: center; padding-top: 80px; }
.welcome-ico { font-size: 52px; }
.welcome-title { font-size: 18px; font-weight: 600; margin-top: 14px; color: var(--text-main); }
.welcome-sub { font-size: 13px; color: var(--text-sub); margin-top: 8px; }
.msg { display: flex; margin-bottom: 14px; }
.msg.user { justify-content: flex-end; }
.msg.assistant { justify-content: flex-start; }
.bubble { max-width: 80%; padding: 12px 16px; border-radius: 14px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.bubble.user { background: linear-gradient(90deg, #4D9FFF, #00E5FF); color: #040A16; border-bottom-right-radius: 4px; }
.bubble.assistant { background: var(--panel-solid); color: var(--text-main); border: 1px solid var(--border); border-bottom-left-radius: 4px; }
.cursor { color: var(--cyan); animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }
.input-bar { display: flex; align-items: flex-end; gap: 10px; margin-top: 12px; }
.input-text { flex: 1; resize: none; background: rgba(15, 23, 38, 0.8); border: 1px solid var(--border); border-radius: 14px; padding: 12px 16px; font-size: 14px; line-height: 1.5; color: var(--text-main); outline: none; font-family: inherit; max-height: 120px; }
.input-text:focus { border-color: var(--blue); }
.send-btn { background: linear-gradient(90deg, #4D9FFF, #00E5FF); color: #040A16; border: none; border-radius: 14px; padding: 12px 22px; font-size: 14px; font-weight: 700; cursor: pointer; }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
