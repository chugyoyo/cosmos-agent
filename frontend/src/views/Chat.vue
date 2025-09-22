<template>
  <div class="chat-page">
    <!-- 左侧会话管理区域 -->
    <aside class="chat-sidebar">
      <!-- Agent 选择器 -->
      <div class="agent-selector">
        <div class="selector-header">
          <h3>选择 Agent</h3>
        </div>
        <div class="agent-dropdown">
          <el-select
            v-model="selectedAgentId"
            placeholder="请选择 Agent"
            @change="onAgentChange"
            class="agent-select"
            size="large"
          >
            <el-option
              v-for="agent in agents"
              :key="agent.id"
              :label="agent.name"
              :value="agent.id"
            >
              <div class="agent-option">
                <div class="agent-icon">
                  <el-icon><Robot /></el-icon>
                </div>
                <div class="agent-details">
                  <div class="agent-name">{{ agent.name }}</div>
                  <div class="agent-type">{{ agent.type || 'AI Agent' }}</div>
                </div>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>

      <!-- 会话管理 -->
      <div class="session-management" v-if="selectedAgentId">
        <div class="session-header">
          <h3>会话列表</h3>
          <el-button 
            type="primary" 
            size="small" 
            @click="createNewSession" 
            class="new-session-btn"
            round
          >
            <el-icon><Plus /></el-icon>
            新建会话
          </el-button>
        </div>

        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: session.id === selectedSessionId }]"
            @click="selectSession(session.id)"
          >
            <div class="session-content">
              <div class="session-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="session-info">
                <div class="session-name">{{ session.name }}</div>
                <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
              </div>
            </div>
            <div class="session-actions">
              <el-dropdown trigger="click" @click.stop>
                <el-button type="text" size="small" class="action-btn">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="editSessionName(session)">
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item @click="duplicateSession(session)">
                      <el-icon><CopyDocument /></el-icon>
                      复制会话
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteSession(session.id)" divided>
                      <el-icon><Delete /></el-icon>
                      删除会话
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <!-- 聊天头部 -->
      <header class="chat-header" v-if="selectedAgentId">
        <div class="header-content">
          <div class="agent-info">
            <div class="agent-avatar">
              <el-icon><Robot /></el-icon>
            </div>
            <div class="agent-details">
              <h2>{{ currentAgent?.name || '智能助手' }}</h2>
              <p>{{ currentSession?.name || 'AI聊天助手' }}</p>
            </div>
          </div>
          <div class="header-actions">
            <el-tooltip content="清空对话" placement="bottom">
              <el-button type="text" @click="clearMessages">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="导出对话" placement="bottom">
              <el-button type="text" @click="exportMessages">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </header>

      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <!-- 空状态 -->
        <div v-if="!selectedAgentId" class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">
              <el-icon size="80"><ChatDotRound /></el-icon>
            </div>
            <h3>选择一个 Agent 开始对话</h3>
            <p>从左侧选择一个 Agent 来开始您的智能对话体验</p>
          </div>
        </div>
        
        <div v-else-if="!selectedSessionId" class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">
              <el-icon size="80"><ChatDotRound /></el-icon>
            </div>
            <h3>选择或创建会话</h3>
            <p>选择一个现有会话或创建新会话来开始对话</p>
            <el-button type="primary" size="large" @click="createNewSession">
              <el-icon><Plus /></el-icon>
              创建新会话
            </el-button>
          </div>
        </div>
        
        <div v-else-if="messages.length === 0" class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">
              <el-icon size="80"><ChatDotRound /></el-icon>
            </div>
            <h3>开始您的对话</h3>
            <p>向 AI 助手提出您的问题，开始智能对话吧！</p>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-list">
          <div
            v-for="message in messages"
            :key="message.id"
            :class="['message', message.role]"
          >
            <div class="message-avatar">
              <div class="avatar" :class="message.role">
                <el-icon v-if="message.role === 'user'"><User /></el-icon>
                <el-icon v-else><Robot /></el-icon>
              </div>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="sender-name">{{ message.role === 'user' ? '您' : currentAgent?.name || 'AI助手' }}</span>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-body">
                <div class="message-text" v-html="formatMessage(message.content)"></div>
                <div class="message-actions" v-if="message.role === 'assistant'">
                  <el-button type="text" size="small" @click="copyMessage(message.content)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                  <el-button type="text" size="small" @click="regenerateMessage(message)">
                    <el-icon><Refresh /></el-icon>
                    重新生成
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area" v-if="selectedSessionId">
        <div class="input-container">
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 6 }"
              placeholder="输入您的问题..."
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.enter.shift.exact="handleShiftEnter"
              :disabled="isLoading"
              class="message-input"
            />
            <div class="input-actions">
              <el-tooltip content="发送 (Enter)" placement="top">
                <el-button
                  type="primary"
                  :loading="isLoading"
                  :disabled="!inputMessage.trim()"
                  @click="sendMessage"
                  class="send-btn"
                  circle
                >
                  <el-icon v-if="!isLoading"><Position /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
          <div class="input-footer">
            <div class="input-tips">
              <span>按 Enter 发送，Shift + Enter 换行</span>
            </div>
            <div class="input-char-count">
              <span>{{ inputMessage.length }}/2000</span>
            </div>
          </div>
        </div>
      </div>
    </main>
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
  Edit,
  MoreFilled,
  CopyDocument,
  Download,
  Refresh,
  Position
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
    const response = await chatApi.getAgents()
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
        if (line.startsWith('data:')) {
          const data = line.slice(5)
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

// 新增方法
const duplicateSession = async (session) => {
  try {
    const newSessionName = `${session.name} - 副本`
    const response = await chatApi.createSession({
      agentId: selectedAgentId.value,
      name: newSessionName,
      status: 0
    })

    if (response.data && response.data.code === 200) {
      await loadSessions(selectedAgentId.value)
      ElMessage.success('会话复制成功')
    }
  } catch (error) {
    console.error('复制会话失败:', error)
    ElMessage.error('复制会话失败')
  }
}

const clearMessages = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前会话的所有消息吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    messages.value = []
    ElMessage.success('消息已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空消息失败')
    }
  }
}

const exportMessages = () => {
  if (messages.value.length === 0) {
    ElMessage.warning('没有消息可导出')
    return
  }

  const exportData = {
    agent: currentAgent.value?.name || '未知Agent',
    session: currentSession.value?.name || '未知会话',
    exportTime: new Date().toISOString(),
    messages: messages.value.map(msg => ({
      role: msg.role,
      content: msg.content,
      time: msg.createdAt
    }))
  }

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `chat-export-${new Date().getTime()}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)

  ElMessage.success('对话已导出')
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const regenerateMessage = async (message) => {
  try {
    // 找到用户的上一条消息
    const messageIndex = messages.value.findIndex(msg => msg.id === message.id)
    if (messageIndex > 0) {
      const userMessage = messages.value[messageIndex - 1]
      if (userMessage.role === 'user') {
        // 删除当前AI消息
        messages.value.splice(messageIndex, 1)
        // 重新发送用户消息
        await sendMessage(userMessage.content)
      }
    }
  } catch (error) {
    ElMessage.error('重新生成失败')
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

<style scoped lang="scss">
// 主页面容器
.chat-page {
  height: 100vh;
  display: flex;
  background: var(--background-color);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

// 左侧会话管理区域
.chat-sidebar {
  width: 320px;
  background: var(--surface-color);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

// Agent 选择器
.agent-selector {
  padding: 24px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.02), rgba(114, 46, 209, 0.02));
  
  .selector-header {
    margin-bottom: 16px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-color);
      margin: 0;
    }
  }
  
  .agent-dropdown {
    .agent-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        border: 1px solid var(--border-color);
        padding: 12px 16px;
        font-size: 15px;
        transition: all var(--transition-fast);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        
        &:hover {
          border-color: var(--primary-color);
        }
        
        &.is-focus {
          border-color: var(--primary-color);
          box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
        }
      }
    }
  }
}

// Agent 选项样式
.agent-option {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .agent-icon {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
  }
  
  .agent-details {
    flex: 1;
    
    .agent-name {
      font-weight: 600;
      font-size: 14px;
      color: var(--text-color);
      margin-bottom: 2px;
    }
    
    .agent-type {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

// 会话管理
.session-management {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .session-header {
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-light);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: white;
      margin: 0;
    }
    
    .new-session-btn {
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: white;
      font-weight: 500;
      font-size: 12px;
      padding: 6px 12px;
      height: auto;
      transition: all var(--transition-fast);
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: translateY(-1px);
      }
    }
  }
  
  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }
}

// 会话项
.session-item {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  
  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
    transform: translateY(-2px);
    
    .session-actions {
      opacity: 1;
    }
  }
  
  &.active {
    border-color: var(--primary-color);
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.05), rgba(114, 46, 209, 0.05));
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
      border-radius: 0 4px 4px 0;
    }
  }
  
  .session-content {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .session-icon {
      width: 32px;
      height: 32px;
      background: var(--primary-color);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 16px;
      flex-shrink: 0;
    }
    
    .session-info {
      flex: 1;
      min-width: 0;
      
      .session-name {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-color);
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .session-time {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
  
  .session-actions {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    opacity: 0;
    transition: opacity var(--transition-fast);
    
    .action-btn {
      width: 24px;
      height: 24px;
      padding: 0;
      border-radius: 4px;
    }
  }
}

// 主聊天区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--background-color);
  overflow: hidden;
}

// 聊天头部
.chat-header {
  background: var(--surface-color);
  border-bottom: 1px solid var(--border-color);
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .agent-info {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .agent-avatar {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
      }
      
      .agent-details {
        h2 {
          font-size: 18px;
          font-weight: 600;
          color: var(--text-color);
          margin: 0 0 4px 0;
        }
        
        p {
          font-size: 14px;
          color: var(--text-secondary);
          margin: 0;
        }
      }
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
      
      .el-button {
        width: 36px;
        height: 36px;
        padding: 0;
        border-radius: 8px;
        color: var(--text-secondary);
        
        &:hover {
          background: var(--primary-color);
          color: white;
        }
      }
    }
  }
}

// 消息容器
.messages-container {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
  background: var(--background-color);
}

// 空状态
.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .empty-content {
    text-align: center;
    max-width: 400px;
    
    .empty-icon {
      color: var(--text-tertiary);
      margin-bottom: 24px;
    }
    
    h3 {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-color);
      margin: 0 0 12px 0;
    }
    
    p {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0 0 24px 0;
      line-height: 1.6;
    }
    
    .el-button {
      border-radius: 12px;
      font-weight: 600;
      padding: 12px 24px;
      height: auto;
    }
  }
}

// 消息列表
.messages-list {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

// 消息项
.message {
  display: flex;
  margin-bottom: 32px;
  gap: 16px;
  align-items: flex-start;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      color: white;
      border-radius: 20px 20px 6px 20px;
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    }
  }
  
  &.assistant {
    .message-content {
      background: var(--surface-color);
      color: var(--text-color);
      border: 1px solid var(--border-color);
      border-radius: 20px 20px 20px 6px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    }
  }
  
  .message-avatar {
    flex-shrink: 0;
    
    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      
      &.user {
        background: var(--text-color);
        color: white;
      }
      
      &.assistant {
        background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
        color: white;
      }
    }
  }
  
  .message-content {
    max-width: 70%;
    padding: 16px 20px;
    position: relative;
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .sender-name {
        font-size: 14px;
        font-weight: 600;
        opacity: 0.8;
      }
      
      .message-time {
        font-size: 12px;
        opacity: 0.6;
      }
    }
    
    .message-body {
      .message-text {
        line-height: 1.6;
        word-wrap: break-word;
        font-size: 15px;
        
        :deep(strong) {
          font-weight: 600;
        }
        
        :deep(em) {
          font-style: italic;
        }
        
        :deep(code) {
          background: rgba(0, 0, 0, 0.1);
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 0.9em;
        }
      }
      
      .message-actions {
        margin-top: 12px;
        display: flex;
        gap: 8px;
        opacity: 0;
        transition: opacity var(--transition-fast);
        
        .el-button {
          font-size: 12px;
          padding: 4px 8px;
          height: auto;
          border-radius: 6px;
        }
      }
    }
    
    &:hover .message-actions {
      opacity: 1;
    }
  }
}

// 输入区域
.input-area {
  background: var(--surface-color);
  border-top: 1px solid var(--border-color);
  padding: 20px 24px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  
  .input-container {
    max-width: 800px;
    margin: 0 auto;
    
    .input-wrapper {
      display: flex;
      gap: 12px;
      align-items: flex-end;
      background: var(--background-color);
      border: 1px solid var(--border-color);
      border-radius: 16px;
      padding: 12px 16px;
      transition: all var(--transition-fast);
      
      &:focus-within {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
      }
      
      .message-input {
        flex: 1;
        
        :deep(.el-textarea__inner) {
          border: none;
          background: transparent;
          padding: 0;
          font-size: 15px;
          line-height: 1.5;
          resize: none;
          box-shadow: none;
          
          &:focus {
            box-shadow: none;
          }
        }
      }
      
      .input-actions {
        .send-btn {
          width: 40px;
          height: 40px;
          padding: 0;
          border-radius: 12px;
          background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
          border: none;
          color: white;
          font-size: 16px;
          transition: all var(--transition-fast);
          
          &:hover:not(:disabled) {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(22, 119, 255, 0.4);
          }
          
          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }
    }
    
    .input-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 8px;
      
      .input-tips {
        font-size: 12px;
        color: var(--text-tertiary);
      }
      
      .input-char-count {
        font-size: 12px;
        color: var(--text-tertiary);
      }
    }
  }
}

// 编辑会话对话框样式
.edit-session-dialog {
  :deep(.el-dialog__header) {
    padding: 24px 24px 16px 24px;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    border-radius: 16px 16px 0 0;
  }
  
  :deep(.el-dialog__title) {
    color: white;
    font-size: 18px;
    font-weight: 600;
  }
  
  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: white;
    font-size: 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 32px 24px 24px 24px;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    border: 1px solid var(--border-color);
    padding: 16px 20px;
    font-size: 15px;
    transition: all var(--transition-fast);
    
    &:hover,
    &.is-focus {
      border-color: var(--primary-color);
      box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .chat-page {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
    
    .session-list {
      display: flex;
      gap: 12px;
      padding: 16px;
      overflow-x: auto;
      
      .session-item {
        min-width: 200px;
        margin-bottom: 0;
      }
    }
  }
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
    
    .header-content {
      .agent-info {
        gap: 12px;
        
        .agent-avatar {
          width: 40px;
          height: 40px;
          font-size: 16px;
        }
        
        .agent-details {
          h2 {
            font-size: 16px;
          }
          
          p {
            font-size: 13px;
          }
        }
      }
    }
  }
  
  .messages-list {
    padding: 16px;
  }
  
  .message {
    .message-content {
      max-width: 85%;
    }
  }
  
  .input-area {
    padding: 16px;
  }
}

// 滚动条样式
.session-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

.session-list::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.session-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.session-list::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}
</style>