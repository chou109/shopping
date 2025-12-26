<template>
  <div class="customer-service-admin">
    <!-- 左侧会话列表 -->
    <div class="session-list-panel">
      <div class="panel-header">
        <h3>
          <el-icon>
            <ChatLineRound />
          </el-icon>
          客服消息
        </h3>
        <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" class="unread-total">
          <el-button type="primary" size="small" @click="refreshSessions" :loading="loadingSession">
            <el-icon>
              <Refresh />
            </el-icon>
          </el-button>
        </el-badge>
      </div>

      <div class="session-list" v-loading="loadingSession">
        <div v-for="session in sessionList" :key="session.sessionId"
          :class="['session-item', { active: currentSession?.sessionId === session.sessionId }]"
          @click="selectSession(session)">
          <div class="session-avatar">
            <el-avatar :size="45" :src="session.userAvatar">
              <el-icon>
                <User />
              </el-icon>
            </el-avatar>
            <el-badge v-if="session.unreadCount > 0" :value="session.unreadCount" class="unread-badge" />
          </div>
          <div class="session-info">
            <div class="session-header">
              <span class="user-name">{{ session.userName || '用户' + session.sessionId.replace('CS_', '') }}</span>
              <span class="last-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="last-message">{{ session.lastMessage || '暂无消息' }}</div>
          </div>
        </div>

        <div class="empty-session" v-if="sessionList.length === 0 && !loadingSession">
          <el-empty description="暂无会话" :image-size="100" />
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-panel">
      <!-- 未选中会话时 -->
      <div class="no-session-selected" v-if="!currentSession">
        <el-empty description="请选择一个会话开始回复">
          <template #image>
            <el-icon class="empty-icon">
              <ChatDotRound />
            </el-icon>
          </template>
        </el-empty>
      </div>

      <!-- 选中会话时 -->
      <template v-else>
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="40" :src="currentSession.userAvatar">
              <el-icon>
                <User />
              </el-icon>
            </el-avatar>
            <div class="user-detail">
              <span class="name">{{ currentSession.userName || '用户' + currentSession.sessionId.replace('CS_', '')
                }}</span>
              <span class="session-id">会话ID: {{ currentSession.sessionId }}</span>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef" v-loading="loadingMessages">
          <div v-for="(msg, index) in messageList" :key="msg.id || index"
            :class="['message-item', msg.senderType === 2 ? 'admin-message' : 'user-message']">
            <!-- 时间分隔 -->
            <div class="time-divider" v-if="shouldShowTime(index)">
              <span>{{ formatFullTime(msg.createTime) }}</span>
            </div>

            <!-- 消息内容 -->
            <div class="message-content">
              <div class="avatar">
                <el-avatar v-if="msg.senderType === 1" :size="36" :src="currentSession.userAvatar">
                  <el-icon>
                    <User />
                  </el-icon>
                </el-avatar>
                <el-avatar v-else :size="36" class="admin-avatar">
                  <el-icon>
                    <Service />
                  </el-icon>
                </el-avatar>
              </div>
              <div class="bubble">
                <div class="sender-info">
                  <span class="sender-name">{{ msg.senderType === 2 ? (msg.senderName || '客服') :
                    (currentSession.userName || '用户') }}</span>
                  <span class="send-time">{{ formatMsgTime(msg.createTime) }}</span>
                </div>
                <div class="text">{{ msg.content }}</div>
              </div>
            </div>
          </div>

          <div ref="scrollAnchor"></div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="quick-replies">
            <span class="quick-label">快捷回复：</span>
            <el-tag v-for="(reply, index) in quickReplies" :key="index" class="quick-tag" @click="useQuickReply(reply)"
              effect="plain" size="small">
              {{ reply }}
            </el-tag>
          </div>
          <div class="input-wrapper">
            <el-input v-model="inputMessage" type="textarea" :rows="3" :maxlength="500" placeholder="输入回复内容..."
              @keyup.ctrl.enter="sendReply" resize="none" />
            <div class="input-actions">
              <span class="char-count">{{ inputMessage.length }}/500</span>
              <el-button type="primary" :disabled="!inputMessage.trim() || sending" :loading="sending"
                @click="sendReply">
                发送回复
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup name="CustomerServiceAdmin">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Refresh, User, ChatDotRound, Service } from '@element-plus/icons-vue'
import {
  getSessionList,
  getSessionMessages,
  replyMessage,
  getUnreadCount,
  markAsRead
} from '@/api/message/customerMessage'

// 响应式数据
const sessionList = ref([])
const currentSession = ref(null)
const messageList = ref([])
const inputMessage = ref('')
const loadingSession = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const totalUnreadCount = ref(0)

const messageListRef = ref(null)
const scrollAnchor = ref(null)

// 快捷回复
const quickReplies = ref([
  '您好，请问有什么可以帮您？',
  '好的，我马上为您处理。',
  '感谢您的咨询，祝您购物愉快！',
  '您的问题已收到，正在核实中。',
  '请稍等，我为您查询一下。'
])

// 加载会话列表
const loadSessions = async () => {
  loadingSession.value = true
  try {
    const response = await getSessionList()
    if (response.code === 200) {
      sessionList.value = response.data || []
      // 计算总未读数
      totalUnreadCount.value = sessionList.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    loadingSession.value = false
  }
}

// 刷新会话列表
const refreshSessions = () => {
  loadSessions()
}

// 选择会话
const selectSession = async (session) => {
  currentSession.value = session
  await loadMessages(session.sessionId)
  // 标记为已读
  if (session.unreadCount > 0) {
    await markAsRead(session.sessionId)
    session.unreadCount = 0
    totalUnreadCount.value = sessionList.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
  }
}

// 加载消息列表
const loadMessages = async (sessionId) => {
  loadingMessages.value = true
  try {
    const response = await getSessionMessages(sessionId)
    if (response.code === 200) {
      messageList.value = response.data || []
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
  }
}

// 发送回复
const sendReply = async () => {
  if (!inputMessage.value.trim() || !currentSession.value || sending.value) return

  const content = inputMessage.value.trim()
  sending.value = true

  try {
    const response = await replyMessage(currentSession.value.sessionId, content, 1)
    if (response.code === 200) {
      // 添加消息到列表
      messageList.value.push(response.data)
      inputMessage.value = ''
      await nextTick()
      scrollToBottom()

      // 更新会话的最后消息
      currentSession.value.lastMessage = content
      currentSession.value.lastMessageTime = new Date().toISOString()

      ElMessage.success('回复成功')
    } else {
      ElMessage.error(response.msg || '回复失败')
    }
  } catch (error) {
    console.error('发送回复失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 使用快捷回复
const useQuickReply = (reply) => {
  inputMessage.value = reply
}

// 滚动到底部
const scrollToBottom = () => {
  if (scrollAnchor.value) {
    scrollAnchor.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// 判断是否显示时间
const shouldShowTime = (index) => {
  if (index === 0) return true
  const current = new Date(messageList.value[index].createTime).getTime()
  const prev = new Date(messageList.value[index - 1].createTime).getTime()
  return current - prev > 5 * 60 * 1000
}

// 格式化时间（会话列表）
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

// 格式化完整时间
const formatFullTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 格式化消息时间
const formatMsgTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 定时刷新
let refreshTimer = null
const startRefresh = () => {
  refreshTimer = setInterval(async () => {
    // 刷新会话列表
    try {
      const response = await getSessionList()
      if (response.code === 200) {
        sessionList.value = response.data || []
        totalUnreadCount.value = sessionList.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
      }
    } catch (error) {
      console.error('刷新会话失败:', error)
    }

    // 如果有选中的会话，刷新消息
    if (currentSession.value) {
      try {
        const response = await getSessionMessages(currentSession.value.sessionId)
        if (response.code === 200) {
          const newMessages = response.data || []
          if (newMessages.length > messageList.value.length) {
            messageList.value = newMessages
            await nextTick()
            scrollToBottom()
          }
        }
      } catch (error) {
        console.error('刷新消息失败:', error)
      }
    }
  }, 5000)
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadSessions()
  startRefresh()
})

onUnmounted(() => {
  stopRefresh()
})
</script>

<style scoped lang="scss">
.customer-service-admin {
  display: flex;
  height: calc(100vh - 84px);
  background: #f5f5f5;
}

/* 左侧会话列表 */
.session-list-panel {
  width: 320px;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    margin: 0;
    font-size: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  padding: 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;

  &:hover {
    background: #f9f9f9;
  }

  &.active {
    background: #fff5f0;
    border-left: 3px solid #ff5500;
  }
}

.session-avatar {
  position: relative;
  margin-right: 12px;

  .unread-badge {
    position: absolute;
    top: -5px;
    right: -5px;
  }
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.last-time {
  font-size: 12px;
  color: #999;
}

.last-message {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-session {
  padding: 50px 20px;
}

/* 右侧聊天区域 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.no-session-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  .empty-icon {
    font-size: 80px;
    color: #ddd;
  }
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;

  .name {
    font-weight: 500;
    font-size: 15px;
    color: #333;
  }

  .session-id {
    font-size: 12px;
    color: #999;
  }
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.time-divider {
  text-align: center;
  margin: 15px 0;

  span {
    background: #e8e8e8;
    color: #999;
    font-size: 12px;
    padding: 3px 10px;
    border-radius: 10px;
  }
}

.message-item {
  margin-bottom: 15px;
}

.message-content {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.admin-message .message-content {
  flex-direction: row-reverse;
}

.bubble {
  max-width: 60%;
  padding: 10px 15px;
  border-radius: 8px;
}

.user-message .bubble {
  background: #f5f5f5;
  border-bottom-left-radius: 2px;
}

.admin-message .bubble {
  background: #ff5500;
  color: white;
  border-bottom-right-radius: 2px;
}

.sender-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
}

.user-message .sender-info {
  color: #999;
}

.admin-message .sender-info {
  color: rgba(255, 255, 255, 0.8);
}

.text {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.admin-avatar {
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #e8e8e8;
  padding: 15px 20px;
}

.quick-replies {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-label {
  font-size: 12px;
  color: #999;
}

.quick-tag {
  cursor: pointer;

  &:hover {
    border-color: #ff5500;
    color: #ff5500;
  }
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

.input-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.char-count {
  font-size: 12px;
  color: #999;
}
</style>
