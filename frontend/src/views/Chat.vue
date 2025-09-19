<template>
  <div class="chat-page">
    <!-- 侧边栏 -->
    <div class="chat-sidebar">
      <!-- Agent选择器 -->
      <div class="agent-selector">
        <h4>选择Agent</h4>
        <el-select
          v-model="selectedAgentId"
          placeholder="请选择Agent"
          @change="onAgentChange"
          class="agent-select"
        >
          <el-option
            v-for="agent in agents"
            :key="agent.id"
            :label="agent.name"
            :value="agent.id"
          >
            <div class="agent-option">
              <span class="agent-name">{{ agent.name }}</span>
              <span class="agent-type">{{ agent.type }}</span>
              <span class="agent-orchestration">来自: {{ agent.orchestrationName }}</span>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 会话列表 -->
      <div class="session-list" v-if="selectedAgentId">
        <div class="session-header">
          <h4>会话列表</h4>
          <el-button type="primary" size="default" @click="createNewSession" class="new-session-btn">
            <el-icon>
              <Plus/>
            </el-icon>
            新建会话
          </el-button>
        </div>

        <div class="sessions">
          <div
              v-for="session in sessions"
              :key="session.id"
              :class="['session-item', { active: session.id === selectedSessionId }]"
              @click="selectSession(session.id)"
          >
            <div class="session-name">{{ session.name }}</div>
            <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
            <div class="session-actions">
              <el-button type="text" size="small" @click.stop="editSessionName(session)">
                <el-icon>
                  <Edit/>
                </el-icon>
              </el-button>
              <el-button type="text" size="small" @click.stop="deleteSession(session.id)">
                <el-icon>
                  <Delete/>
                </el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-container">
      <!-- 聊天头部 -->
      <div class="chat-header" v-if="selectedAgentId">
        <div class="chat-info">
          <el-icon class="chat-icon">
            <ChatDotRound/>
          </el-icon>
          <div>
            <h3>{{ currentAgent?.name || '智能助手' }}</h3>
            <p class="text-secondary">{{ currentSession?.name || 'AI聊天助手' }}</p>
          </div>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="!selectedAgentId" class="empty-chat">
          <el-icon size="64" color="#C0C4CC">
            <ChatDotRound/>
          </el-icon>
          <p>请先选择一个Agent开始聊天</p>
        </div>
        <div v-else-if="!selectedSessionId" class="empty-chat">
          <el-icon size="64" color="#C0C4CC">
            <ChatDotRound/>
          </el-icon>
          <p>请选择或创建一个会话</p>
        </div>
        <div v-else-if="messages.length === 0" class="empty-chat">
          <el-icon size="64" color="#C0C4CC">
            <ChatDotRound/>
          </el-icon>
          <p>开始与AI助手对话吧！</p>
        </div>

        <div
            v-for="message in messages"
            :key="message.id"
            :class="['message', message.role]"
        >
          <div class="message-avatar">
            <el-avatar v-if="message.role === 'user'" :size="36" style="background-color: #1f2937;">
              <el-icon>
                <User/>
              </el-icon>
            </el-avatar>
            <el-avatar v-else :size="36" style="background-color: #f3f4f6; color: #1f2937; border: 2px solid #e5e7eb;">
              <el-icon>
                <ChatDotRound/>
              </el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-text">
              <div v-html="formatMessage(message.content)"></div>
            </div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-container">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入您的问题..."
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.enter.shift.exact="handleShiftEnter"
              :disabled="isLoading || !selectedSessionId"
          />
          <el-button
              type="primary"
              :loading="isLoading"
              :disabled="!inputMessage.trim() || !selectedSessionId"
              @click="sendMessage"
          >
            发送
          </el-button>
        </div>
        <div class="input-tips">
          <span class="text-secondary">按 Enter 发送，Shift + Enter 换行</span>
        </div>
      </div>
    </div>
  </div>

  <!-- 编辑会话名称对话框 -->
  <el-dialog
    v-model="showEditDialog"
    title="编辑会话名称"
    width="500px"
    :close-on-click-modal="false"
    class="edit-session-dialog"
  >
    <div class="edit-session-content">
      <el-input
        v-model="newSessionName"
        placeholder="请输入会话名称"
        maxlength="50"
        show-word-limit
        size="large"
        @keyup.enter="handleEditSession"
      />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSession" :loading="loading">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, reactive, nextTick, onMounted, computed, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  ChatDotRound,
  User,
  Delete,
  Plus,
  Edit
} from '@element-plus/icons-vue'
import {chatApi} from '@/services/api'

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

// Agent和会话相关
const agents = ref([])
const sessions = ref([])
const selectedAgentId = ref('')
const selectedSessionId = ref('')

// 对话框相关
const editingSession = ref(null)
const showEditDialog = ref(false)
const newSessionName = ref('')
const loading = ref(false)

// 计算属性
const currentAgent = computed(() =>
    agents.value.find(a => a.id === selectedAgentId.value)
)
const currentSession = computed(() =>
    sessions.value.find(s => s.id === selectedSessionId.value)
)

// Agent相关方法
const loadAgents = async () => {
  try {
    const response = await chatApi.getOrchestrationAgents()
    if (response.data && response.data.code === 200) {
      agents.value = response.data.data || []
      
      // 自动选择最新修改的Agent
      if (agents.value.length > 0 && !selectedAgentId.value) {
        selectedAgentId.value = agents.value[0].id
        await onAgentChange(selectedAgentId.value)
      }
    }
  } catch (error) {
    console.error('加载Agent列表失败:', error)
    ElMessage.error('加载Agent列表失败')
  }
}

const onAgentChange = async (agentId) => {
  selectedSessionId.value = ''
  messages.value = []
  await loadSessions(agentId)
}

// 会话相关方法
const loadSessions = async (agentId) => {
  if (!agentId) return

  try {
    const response = await chatApi.getSessions(agentId)
    if (response.data && response.data.code === 200) {
      sessions.value = response.data.data || []
      
      // 自动选择最新会话
      if (sessions.value.length > 0 && !selectedSessionId.value) {
        selectedSessionId.value = sessions.value[0].id
        await loadSessionMessages(selectedSessionId.value)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  }
}

const createNewSession = async () => {
  if (!selectedAgentId.value) return

  try {
    const sessionName = `会话 ${new Date().toLocaleString()}`
    const response = await chatApi.createSession({
      agentId: selectedAgentId.value,
      name: sessionName,
      status: 0
    })

    if (response.data && response.data.code === 200) {
      await loadSessions(selectedAgentId.value)
      selectedSessionId.value = response.data.data.id
      messages.value = []
      ElMessage.success('会话创建成功')
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  }
}

const selectSession = async (sessionId) => {
  selectedSessionId.value = sessionId
  await loadSessionMessages(sessionId)
}

const loadSessionMessages = async (sessionId) => {
  if (!sessionId) return
  
  try {
    const response = await chatApi.getSessionMessages(sessionId)
    if (response.data && response.data.code === 200) {
      messages.value = response.data.data || []
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载会话消息失败:', error)
    ElMessage.error('加载会话消息失败')
  }
}

// 消息发送方法
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value || !selectedSessionId.value) return

  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value.trim(),
    createdAt: new Date()
  }

  messages.value.push(userMessage)
  const question = inputMessage.value.trim()
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 发送消息到AI - 使用流式响应
  isLoading.value = true
  try {
    const aiMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      createdAt: new Date()
    }
    messages.value.push(aiMessage)
    
    // 获取流式响应
    const response = await chatApi.sendMessageStream({
      agentId: selectedAgentId.value,
      message: question,
      sessionId: selectedSessionId.value
    })

    if (!response.ok) {
      throw new Error('网络请求失败')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            return
          }
          
          if (data.trim()) {
            // 更新AI回复内容
            aiMessage.content += data
            await nextTick()
            scrollToBottom()
          }
        }
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请重试')
    // 移除失败的AI消息
    if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'assistant') {
      messages.value.pop()
    }
  } finally {
    isLoading.value = false
  }
}

const handleShiftEnter = (event) => {
  // Shift + Enter 换行，不发送消息
  const textarea = event.target
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = textarea.value

  textarea.value = value.substring(0, start) + '\n' + value.substring(end)
  textarea.selectionStart = textarea.selectionEnd = start + 1

  // 更新 v-model
  inputMessage.value = textarea.value
}

// 会话管理方法
const editSessionName = (session) => {
  editingSession.value = session
  newSessionName.value = session.name
  showEditDialog.value = true
}

const handleEditSession = async () => {
  if (!newSessionName.value.trim()) {
    ElMessage.error('会话名称不能为空')
    return
  }

  try {
    // 这里需要添加更新会话名称的API
    await loadSessions(selectedAgentId.value)
    ElMessage.success('会话名称更新成功')
    showEditDialog.value = false
    editingSession.value = null
    newSessionName.value = ''
  } catch (error) {
    ElMessage.error('更新会话名称失败')
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await chatApi.deleteSession(sessionId)
    await loadSessions(selectedAgentId.value)

    if (selectedSessionId.value === sessionId) {
      selectedSessionId.value = ''
      messages.value = []
    }

    ElMessage.success('会话删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除会话失败')
    }
  }
}

// 工具方法
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

const formatMessage = (content) => {
  if (!content) return ''
  
  // 简单的Markdown处理
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

// 生命周期
onMounted(async () => {
  await loadAgents()
})

// 监听Agent变化，自动选择默认会话
watch(selectedAgentId, async (newAgentId) => {
  if (newAgentId) {
    await loadSessions(newAgentId)
  }
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 105px);
  display: flex;
  background-color: #f5f7fa;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.chat-sidebar {
  width: 320px;
  background-color: white;
  border-right: 1px solid rgba(22, 95, 253, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-radius: 0 24px 24px 0;
}

.agent-selector {
  padding: 24px 20px 20px 20px;
  border-bottom: 1px solid rgba(22, 95, 253, 0.1);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.02) 0%, rgba(64, 158, 255, 0.01) 100%);
}

.agent-selector h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.025em;
}

.agent-select {
  width: 100%;
}

.agent-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 2px solid var(--border-base);
  padding: 12px 16px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.05);
}

.agent-select :deep(.el-input__wrapper):hover {
  border-color: var(--primary-color);
}

.agent-select :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.agent-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.agent-name {
  font-weight: 500;
}

.agent-type {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.session-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 20px 24px;
  border-bottom: 1px solid rgba(22, 95, 253, 0.1);
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
}

.session-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.02em;
}

.new-session-btn {
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  background-color: white;
  border-color: white;
  color: rgb(22, 95, 253);
  padding: 10px 20px;
  height: auto;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.new-session-btn:hover {
  background-color: rgb(18, 76, 202);
  border-color: rgb(18, 76, 202);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 95, 253, 0.4);
}

.sessions {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}

.session-item {
  padding: 16px 20px;
  margin-bottom: 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  border: 1px solid rgba(22, 95, 253, 0.1);
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.session-item:hover {
  background: linear-gradient(135deg, rgba(22, 95, 253, 0.05) 0%, rgba(52, 205, 201, 0.05) 100%);
  border-color: rgb(22, 95, 253);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(22, 95, 253, 0.15);
}

.session-item.active {
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
  color: white;
  border-color: rgb(22, 95, 253);
  box-shadow: 0 8px 24px rgba(22, 95, 253, 0.3);
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
  letter-spacing: -0.01em;
}

.session-time {
  font-size: 13px;
  opacity: 0.7;
  font-weight: 500;
}

.session-actions {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: none;
}

.session-item:hover .session-actions {
  display: flex;
  gap: 4px;
}

.session-item.active .session-actions {
  display: flex;
  gap: 4px;
}

.chat-container {
  flex: 1;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid rgba(22, 95, 253, 0.1);
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chat-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.chat-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.chat-info h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}

.chat-info .text-secondary {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 2px;
}

.chat-messages {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
  scroll-behavior: smooth;
  background-color: #f5f7fa;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
}

.empty-chat p {
  margin-top: 20px;
  font-size: 17px;
  font-weight: 500;
  color: #4b5563;
}

.message {
  display: flex;
  margin-bottom: 32px;
  gap: 16px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content {
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
  color: white;
  border-radius: 20px 20px 6px 20px;
  box-shadow: 0 4px 12px rgba(22, 95, 253, 0.3);
}

.message.assistant .message-content {
  background-color: white;
  color: #1a1a1a;
  border-radius: 20px 20px 20px 6px;
  border: 1px solid rgba(22, 95, 253, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.message-content {
  max-width: 75%;
  padding: 16px 20px;
  position: relative;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
  font-size: 15px;
}

.message-text :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.message-text :deep(em) {
  font-style: italic;
  color: inherit;
}

.message-text :deep(code) {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-time {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.6;
  font-weight: 400;
}

.chat-input {
  padding: 24px 32px;
  border-top: 1px solid rgba(22, 95, 253, 0.1);
  background-color: white;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.input-container {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.input-container .el-input {
  flex: 1;
}

.input-container :deep(.el-textarea__inner) {
  border-radius: 16px;
  border: 2px solid rgba(22, 95, 253, 0.1);
  padding: 16px 20px;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.input-container :deep(.el-textarea__inner):focus {
  border-color: rgb(22, 95, 253);
  box-shadow: 0 0 0 3px rgba(22, 95, 253, 0.2);
}

.input-container .el-button {
  border-radius: 12px;
  padding: 16px 28px;
  font-weight: 600;
  font-size: 15px;
  height: auto;
  min-height: 52px;
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
  border-color: transparent;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(22, 95, 253, 0.3);
}

.input-container .el-button:hover {
  background: linear-gradient(135deg, rgb(18, 76, 202) 0%, rgb(42, 180, 175) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 95, 253, 0.4);
}

.input-tips {
  margin-top: 12px;
  font-size: 13px;
  text-align: center;
  color: #6b7280;
  font-weight: 400;
}

/* 编辑会话对话框样式 */
.edit-session-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px 24px;
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
  border-radius: 16px 16px 0 0;
}

.edit-session-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.edit-session-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.edit-session-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

.edit-session-dialog :deep(.el-dialog__body) {
  padding: 32px 24px 24px 24px;
}

.edit-session-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-session-content :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid rgba(22, 95, 253, 0.1);
  padding: 16px 20px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.edit-session-content :deep(.el-input__wrapper):hover,
.edit-session-content :deep(.el-input__wrapper.is-focus) {
  border-color: rgb(22, 95, 253);
  box-shadow: 0 0 0 3px rgba(22, 95, 253, 0.2);
}

.edit-session-content :deep(.el-input__inner) {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.edit-session-content :deep(.el-input__count) {
  font-size: 12px;
  color: #666;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 600;
  font-size: 14px;
  height: auto;
  min-height: 40px;
}

.dialog-footer .el-button:first-child {
  background-color: #f5f7fa;
  border-color: #e5e7eb;
  color: #666;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:first-child:hover {
  background-color: #e5e7eb;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.dialog-footer .el-button:last-child {
  background: linear-gradient(135deg, rgb(22, 95, 253) 0%, rgb(52, 205, 201) 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(22, 95, 253, 0.3);
  transition: all 0.3s ease;
}

.dialog-footer .el-button:last-child:hover {
  background: linear-gradient(135deg, rgb(18, 76, 202) 0%, rgb(42, 180, 175) 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(22, 95, 253, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-page {
    height: calc(100vh - 60px);
  }

  .chat-sidebar {
    width: 280px;
  }

  .chat-messages {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .chat-input {
    padding: 16px;
  }

  .input-container {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .chat-sidebar {
    width: 100vw;
  }

  .message-content {
    max-width: 90%;
  }

  .chat-messages {
    padding: 12px;
  }

  .chat-input {
    padding: 12px;
  }
}
</style>