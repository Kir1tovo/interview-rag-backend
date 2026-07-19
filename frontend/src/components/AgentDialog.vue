<template>
  <div class="agent-dialog">
    <div class="agent-toggle" @click="toggleDialog">
      <el-icon><ChatDotSquare /></el-icon>
      <span>智能助手</span>
      <el-icon :class="{ 'rotate': isExpanded }"><ArrowDown /></el-icon>
    </div>
    <div v-show="isExpanded" class="agent-content">
      <div class="agent-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="agent-empty">
          <el-icon :size="32" color="#c0c4cc"><ChatDotSquare /></el-icon>
          <p>有什么可以帮你的？</p>
        </div>
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['agent-message', msg.role === 'user' ? 'agent-message-user' : 'agent-message-assistant']"
        >
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'user'" :size="16"><User /></el-icon>
            <el-icon v-else :size="16"><Monitor /></el-icon>
          </div>
          <div class="message-bubble">
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>
        <div v-if="loading" class="agent-message agent-message-assistant">
          <div class="message-avatar">
            <el-icon :size="16"><Monitor /></el-icon>
          </div>
          <div class="message-bubble">
            <div class="message-loading">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>
      <div class="agent-input-area">
        <el-input
          v-model="message"
          placeholder="输入消息..."
          size="small"
          @keyup.enter="handleSend"
          :disabled="loading"
        >
          <template #append>
            <el-button @click="handleSend" :disabled="!message.trim() || loading" :loading="loading">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotSquare, ArrowDown, User, Monitor, Promotion } from '@element-plus/icons-vue'
import { sendAgentMessage } from '@/utils/api'

const isExpanded = ref(true)
const message = ref('')
const messages = ref([])
const loading = ref(false)
const messagesRef = ref(null)

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const toggleDialog = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    scrollToBottom()
  }
}

const handleSend = async () => {
  const text = message.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  message.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const res = await sendAgentMessage({ message: text })
    const reply = res.data?.reply || res.data?.content || res.data || '收到消息'
    messages.value.push({ role: 'assistant', content: reply })
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，请求失败，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.agent-dialog {
  border-top: 1px solid #e6e6e6;
  background-color: #fff;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.agent-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  cursor: pointer;
  color: #606266;
  font-size: 13px;
  transition: background-color 0.2s;
  user-select: none;
}

.agent-toggle:hover {
  background-color: #f5f7fa;
}

.agent-toggle .rotate {
  transform: rotate(180deg);
  transition: transform 0.2s;
}

.agent-content {
  display: flex;
  flex-direction: column;
  height: 300px;
}

.agent-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px 10px;
  background-color: #f5f7fa;
}

.agent-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.agent-empty p {
  margin-top: 8px;
  font-size: 13px;
}

.agent-message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}

.agent-message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background-color: #e6e6e6;
  color: #909399;
}

.agent-message-user .message-avatar {
  background-color: #409eff;
  color: #fff;
}

.agent-message-assistant .message-avatar {
  background-color: #67c23a;
  color: #fff;
}

.message-bubble {
  max-width: 75%;
  margin: 0 8px;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.agent-message-user .message-bubble {
  background-color: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.agent-message-assistant .message-bubble {
  background-color: #fff;
  color: #303133;
  border: 1px solid #e6e6e6;
  border-top-left-radius: 2px;
}

.message-text {
  white-space: pre-wrap;
}

.message-loading {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.message-loading span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #c0c4cc;
  animation: bounce 1.2s infinite ease-in-out;
}

.message-loading span:nth-child(2) {
  animation-delay: 0.2s;
}

.message-loading span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.agent-input-area {
  padding: 8px 10px;
  border-top: 1px solid #e6e6e6;
  background-color: #fff;
}

/* 响应式设计：小屏幕适配 */
@media screen and (max-height: 600px) {
  .agent-content {
    height: 200px;
  }
}

@media screen and (max-height: 400px) {
  .agent-content {
    height: 150px;
  }
}
</style>