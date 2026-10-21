<template>
  <div class="user-message">
    <!-- 消息列表 -->
    <div class="message-list" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && sessionList.length === 0" class="empty-state">
        <el-icon class="empty-icon">
          <ChatDotRound />
        </el-icon>
        <p>暂无消息</p>
        <p class="sub-text">您可以在商品详情页联系客服</p>
      </div>

      <!-- 会话列表 -->
      <div v-else class="session-list">
        <div v-for="session in sessionList" :key="session.sessionId" class="session-item" role="button"
          tabindex="0" :aria-label="`打开与${session.shopName || '客服'}的消息`" @click="openChat(session)"
          @keydown.enter.prevent="openChat(session)" @keydown.space.prevent="openChat(session)">
          <div class="session-avatar">
            <el-icon v-if="!session.shopLogo" class="default-avatar">
              <Shop />
            </el-icon>
            <img v-else :src="getImageUrl(session.shopLogo)" alt="店铺头像" @error="handleImageError" />
          </div>
          <div class="session-content">
            <div class="session-header">
              <span class="session-name">{{ session.shopName || '客服' }}</span>
              <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="session-message">
              <span class="last-message">{{ session.lastMessage || '暂无消息' }}</span>
              <span v-if="session.unreadCount > 0" class="unread-badge">
                {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Shop } from '@element-plus/icons-vue'
import { getUserSessions } from '@/api/system/customerMessage'
import { getProductImageUrl } from '@/utils/image'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const sessionList = ref([])

// 获取图片URL
const getImageUrl = (url) => {
  return getProductImageUrl(url)
}

// 图片加载失败处理
const handleImageError = (e) => {
  e.target.style.display = 'none'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 一周内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['日', '一', '二', '三', '四', '五', '六']
    return '周' + days[date.getDay()]
  }

  // 其他
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const response = await getUserSessions()
    if (response.code === 200) {
      sessionList.value = response.data || []
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 打开聊天
const openChat = (session) => {
  router.push({
    path: '/customer-service',
    query: {
      sessionId: session.sessionId,
      receiverId: session.receiverId || session.shopUserId,
      shopName: session.shopName || '客服'
    }
  })
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.user-message {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 400px;
}

.message-list {
  min-height: 300px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  color: #ddd;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.empty-state .sub-text {
  font-size: 14px;
  color: #bbb;
  margin-top: 8px;
}

/* 会话列表 */
.session-list {
  display: flex;
  flex-direction: column;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover {
  background: #fafafa;
}

.session-item:focus-visible {
  outline: 3px solid #ee0a24;
  outline-offset: -3px;
}

.session-item:last-child {
  border-bottom: none;
}

.session-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
}

.session-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.session-avatar .default-avatar {
  font-size: 28px;
  color: #bbb;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 8px;
}

.session-message {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-badge {
  background: #ff5500;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  flex-shrink: 0;
  margin-left: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-message {
    padding: 10px;
  }

  .session-item {
    padding: 12px;
  }

  .session-avatar {
    width: 44px;
    height: 44px;
  }
}
</style>
