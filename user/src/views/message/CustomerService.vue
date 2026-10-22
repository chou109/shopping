<template>
  <div class="customer-service-page">
    <!-- 顶部导航栏 -->
    <header class="cs-header">
      <div class="header-content">
        <button type="button" class="back-btn" @click="goBack">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          <span>返回</span>
        </button>
        <div class="header-title">
          <el-icon class="title-icon">
            <Service />
          </el-icon>
          <span>{{ shopName ? `联系 ${shopName}` : '人工客服' }}</span>
        </div>
        <div class="header-actions">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
            <el-icon class="action-icon">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
      </div>
    </header>

    <!-- 聊天容器 -->
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <!-- 欢迎消息 -->
        <div class="welcome-message" v-if="messages.length === 0">
          <div class="welcome-icon">
            <el-icon>
              <Service />
            </el-icon>
          </div>
          <div class="welcome-text">
            <h3>欢迎咨询人工客服</h3>
            <p>您可以在这里向我们的客服咨询任何问题</p>
          </div>
          <div class="quick-questions">
            <div class="quick-title">常见问题：</div>
            <div class="quick-btns">
              <el-button v-for="(question, index) in quickQuestions" :key="index" size="small"
                @click="sendQuickQuestion(question)">
                {{ question }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in messages" :key="msg.id || index"
          :class="['message-item', msg.senderType === 1 ? 'user-message' : 'admin-message']">
          <!-- 时间分隔 -->
          <div class="time-divider" v-if="shouldShowTime(index)">
            <span>{{ formatTime(msg.createTime) }}</span>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            <div class="avatar">
              <img v-if="msg.senderType === 1" :src="userAvatar" alt="用户头像" />
              <el-icon v-else class="admin-avatar">
                <Service />
              </el-icon>
            </div>
            <div class="bubble">
              <div class="sender-name" v-if="msg.senderType === 2">
                {{ msg.senderName || '客服' }}
              </div>
              <div class="text">{{ msg.content }}</div>
            </div>
          </div>
        </div>

        <!-- 加载提示 -->
        <div class="loading-tip" v-if="loading">
          <el-icon class="loading-icon">
            <Loading />
          </el-icon>
          <span>正在加载...</span>
        </div>

        <!-- 滚动锚点 -->
        <div ref="scrollAnchor"></div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input v-model="inputMessage" type="textarea" :rows="2" :maxlength="500"
            label="发送给人工客服的消息" placeholder="请输入您的问题..."
            @keyup.ctrl.enter="sendMessage" resize="none" class="message-input" />
          <div class="input-actions">
            <span class="char-count">{{ inputMessage.length }}/500</span>
            <el-button type="primary" :disabled="!inputMessage.trim() || sending" :loading="sending"
              @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
        <div class="input-tip">按 Ctrl+Enter 发送消息</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { DEFAULT_AVATAR_IMAGE } from '@/utils/image'
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Service, Bell, Loading } from '@element-plus/icons-vue'
import { sendMessage as sendMessageApi, getMessages, getUnreadCount, markAsRead } from '@/api/system/customerMessage'
import { getProductImageUrl } from '@/utils/image'

const router = useRouter()
const store = useStore()
const route = useRoute()

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const unreadCount = ref(0)
const messageListRef = ref(null)
const scrollAnchor = ref(null)
let messageLoadVersion = 0
let componentActive = false

// 获取路由参数
const receiverId = computed(() => {
  const id = route.query.receiverId
  return id ? Number(id) : null
})
const shopId = computed(() => route.query.shopId || null)
const shopName = computed(() => route.query.shopName || '客服')

// 快捷问题
const quickQuestions = ref([
  '如何退款？',
  '订单什么时候发货？',
  '商品质量问题怎么处理？',
  '如何修改收货地址？',
  '优惠券怎么使用？'
])

// 用户头像
const userAvatar = computed(() => {
  const avatar = store.state.user?.avatar
  if (avatar) {
    return getProductImageUrl(avatar)
  }
  return DEFAULT_AVATAR_IMAGE
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载消息列表
const loadMessages = async () => {
  const requestReceiverId = receiverId.value
  const requestVersion = ++messageLoadVersion
  loading.value = true
  try {
    // 根据receiverId获取与指定接收者的对话
    const response = await getMessages(requestReceiverId)
    if (response.code === 200 && componentActive && requestVersion === messageLoadVersion) {
      messages.value = response.data || []
      await nextTick()
      if (!componentActive || requestVersion !== messageLoadVersion) return
      scrollToBottom()
      // 标记已读，传递receiverId
      try {
        await markAsRead(requestReceiverId)
        if (!componentActive || requestVersion !== messageLoadVersion) return
        await loadUnreadCount()
      } catch (error) {
        console.warn('标记消息已读失败:', error)
      }
    }
  } catch (error) {
    if (componentActive && requestVersion === messageLoadVersion) {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败')
    }
  } finally {
    if (componentActive && requestVersion === messageLoadVersion) loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const response = await getUnreadCount()
    if (response.code === 200 && componentActive) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return

  const content = inputMessage.value.trim()
  sending.value = true

  // 先在界面上显示用户消息
  const tempMessage = {
    id: 'temp_' + Date.now(),
    content: content,
    senderType: 1,
    senderName: store.state.user?.name || '我',
    createTime: new Date().toISOString()
  }
  messages.value.push(tempMessage)
  inputMessage.value = ''
  await nextTick()
  scrollToBottom()

  try {
    const response = await sendMessageApi(content, 1, receiverId.value)
    if (response.code === 200) {
      // 替换临时消息
      const index = messages.value.findIndex(m => m.id === tempMessage.id)
      if (index !== -1 && response.data) {
        messages.value[index] = response.data
      } else if (!response.data) {
        await loadMessages()
      }
    } else {
      // 发送失败，移除临时消息
      messages.value = messages.value.filter(m => m.id !== tempMessage.id)
      ElMessage.error(response.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    // 发送失败，移除临时消息
    messages.value = messages.value.filter(m => m.id !== tempMessage.id)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 发送快捷问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
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
  const current = new Date(messages.value[index].createTime).getTime()
  const prev = new Date(messages.value[index - 1].createTime).getTime()
  if (!Number.isFinite(current) || !Number.isFinite(prev)) return true
  // 超过5分钟显示时间
  return current - prev > 5 * 60 * 1000
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  if (!Number.isFinite(date.getTime())) return '时间未知'
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  if (isToday) {
    return `${hours}:${minutes}`
  }

  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 定时刷新消息
let refreshTimer = null
let refreshInFlight = false
const startRefresh = () => {
  refreshTimer = setInterval(async () => {
    if (refreshInFlight) return
    refreshInFlight = true
    const requestReceiverId = receiverId.value
    try {
      const response = await getMessages(requestReceiverId)
      if (!componentActive || requestReceiverId !== receiverId.value) return
      if (response.code === 200) {
        const newMessages = response.data || []
        const oldIds = messages.value.map(message => message.id)
        const newIds = newMessages.map(message => message.id)
        if (oldIds.length !== newIds.length || oldIds.some((id, index) => id !== newIds[index])) {
          messages.value = newMessages
          await nextTick()
          if (!componentActive || requestReceiverId !== receiverId.value) return
          scrollToBottom()
          try {
            await markAsRead(requestReceiverId)
            if (!componentActive || requestReceiverId !== receiverId.value) return
            await loadUnreadCount()
          } catch (error) {
            console.warn('标记消息已读失败:', error)
          }
        }
      }
    } catch (error) {
      console.error('刷新消息失败:', error)
    } finally {
      refreshInFlight = false
    }
  }, 5000) // 每5秒刷新一次
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  componentActive = true
  loadMessages()
  loadUnreadCount()
  startRefresh()
})

watch(receiverId, () => {
  messages.value = []
  loadMessages()
})

// 组件卸载时停止刷新
onUnmounted(() => {
  componentActive = false
  messageLoadVersion += 1
  stopRefresh()
})
</script>

<style scoped>
.customer-service-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 顶部导航 */
.cs-header {
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  color: white;
  padding: 15px 20px;
  flex-shrink: 0;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  border: 0;
  background: transparent;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.title-icon {
  font-size: 22px;
}

.header-actions {
  padding: 5px;
}

.action-icon {
  font-size: 20px;
  cursor: pointer;
}

.back-btn:focus-visible {
  outline: 3px solid currentColor;
  outline-offset: 3px;
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  background: white;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
  font-size: 36px;
}

.welcome-text h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #333;
}

.welcome-text p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.quick-questions {
  margin-top: 30px;
  text-align: left;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.quick-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.quick-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-btns .el-button {
  border-color: #ff6034;
  color: #ff6034;
}

.quick-btns .el-button:hover {
  background: #fff5f0;
}

/* 时间分隔 */
.time-divider {
  text-align: center;
  margin: 15px 0;
}

.time-divider span {
  background: #e8e8e8;
  color: #999;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
}

/* 消息项 */
.message-item {
  margin-bottom: 15px;
}

.message-content {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.bubble {
  max-width: 70%;
  padding: 12px 15px;
  border-radius: 8px;
  position: relative;
}

.user-message .bubble {
  background: #ff6034;
  color: white;
  border-bottom-right-radius: 2px;
}

.admin-message .bubble {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 2px;
}

.sender-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.text {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

/* 加载提示 */
.loading-tip {
  text-align: center;
  padding: 20px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #eee;
  padding: 15px 20px;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 10px 15px;
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

.input-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: right;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-container {
    max-width: 100%;
  }

  .message-list {
    padding: 15px;
  }

  .bubble {
    max-width: 80%;
  }

  .welcome-message {
    padding: 20px;
  }

  .welcome-icon {
    width: 60px;
    height: 60px;
    font-size: 28px;
  }
}
</style>
