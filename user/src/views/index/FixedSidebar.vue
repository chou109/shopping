<template>
  <!-- 右侧固定侧边栏 -->
  <div class="fixed-sidebar" :class="{ 'sidebar-hidden': isHidden }">
    <!-- 回到首页 -->
    <div v-if="showHomeButton" class="sidebar-item" @click="goToHome" :title="currentRoute === '/' ? '当前已在首页' : '回到首页'">
      <div class="item-icon">
        <el-icon>
          <House />
        </el-icon>
      </div>
      <div class="item-text">首页</div>
    </div>

    <!-- 购物车 -->
    <div v-if="showCartButton" class="sidebar-item" @click="goToCart"
      :title="currentRoute.includes('/user/cart') ? '当前已在购物车' : '购物车'">
      <div class="item-icon">
        <el-icon>
          <ShoppingCart />
        </el-icon>
        <span v-if="cartCount > 0" class="item-badge">{{ formatCount(cartCount) }}</span>
      </div>
      <div class="item-text">购物车</div>
    </div>

    <!-- 消息 -->
    <div v-if="showMessageButton" class="sidebar-item" @click="goToMessage"
      :title="currentRoute.includes('/user/message') ? '当前已在消息页' : '查看消息'">
      <div class="item-icon">
        <el-icon>
          <Message />
        </el-icon>
        <span v-if="messageCount > 0" class="item-badge">{{ formatCount(messageCount) }}</span>
      </div>
      <div class="item-text">消息</div>
    </div>

    <!-- 客服/AI助手 -->
    <div v-if="showServiceButton" class="sidebar-item" @click="goToChat"
      :title="currentRoute.includes('/chat') ? '当前已在AI助手页' : 'AI助手'">
      <div class="item-icon">
        <el-icon>
          <Headset />
        </el-icon>
        <!-- 可选：添加AI助手消息提示 -->
        <span v-if="chatUnreadCount > 0" class="item-badge">{{ formatCount(chatUnreadCount) }}</span>
      </div>
      <div class="item-text">AI助手</div>
    </div>

    <!-- 人工客服 -->
    <div v-if="showHumanServiceButton" class="sidebar-item human-service" @click="goToCustomerService"
      :title="currentRoute.includes('/customer-service') ? '当前已在人工客服页' : '人工客服'">
      <div class="item-icon">
        <el-icon>
          <ChatDotRound />
        </el-icon>
      </div>
      <div class="item-text">客服</div>
    </div>

    <!-- 回到顶部 -->
    <div v-if="showTopButton" class="sidebar-item" @click="scrollToTop" title="回到顶部">
      <div class="item-icon">
        <el-icon>
          <Top />
        </el-icon>
      </div>
      <div class="item-text">顶部</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House,
  ShoppingCart,
  Headset,
  Top,
  Message,
  ChatDotRound
} from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/system/customerMessage'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

// 响应式数据
const isHidden = ref(false)
const scrollY = ref(0)
const cartCount = ref(0)
const messageCount = ref(0) // 未读消息数量
const chatUnreadCount = ref(0) // AI助手未读消息数量

// 计算当前路由路径
const currentRoute = computed(() => route.path)

// 计算属性：控制按钮显示
const showHomeButton = computed(() => {
  return currentRoute.value !== '/' && !currentRoute.value.includes('/index')
})

const showCartButton = computed(() => {
  return !currentRoute.value.includes('/user/cart')
})

const showMessageButton = computed(() => {
  return !currentRoute.value.includes('/user/message')
})

const showServiceButton = computed(() => {
  // 如果当前已经在chat页面，就不显示AI助手按钮
  return !currentRoute.value.includes('/chat')
})

const showHumanServiceButton = computed(() => {
  // 如果当前已经在人工客服页面，就不显示按钮
  return !currentRoute.value.includes('/customer-service')
})

const showTopButton = computed(() => {
  return scrollY.value > 300
})

// 格式化数量显示
const formatCount = (count) => {
  if (count > 99) return '99+'
  return count.toString()
}

// 导航方法
const goToHome = () => {
  if (showHomeButton.value) {
    router.push('/')
  }
}

const goToCart = () => {
  if (showCartButton.value) {
    router.push('/user/cart')
  }
}

const goToMessage = () => {
  if (showMessageButton.value) {
    router.push('/user/message')
  }
}

// 跳转到AI助手页面
const goToChat = () => {
  if (showServiceButton.value) {
    router.push('/chat')
  }
}

// 跳转到人工客服页面（和id为1的最高管理员对话）
const goToCustomerService = () => {
  if (showHumanServiceButton.value) {
    router.push({
      path: '/customer-service',
      query: {
        receiverId: 1, // 最高管理员ID
        shopName: '平台客服'
      }
    })
  }
}

const scrollToTop = () => {
  if (showTopButton.value) {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 滚动事件处理
const handleScroll = () => {
  scrollY.value = window.scrollY || document.documentElement.scrollTop

  // 向下滚动时隐藏，向上滚动时显示
  const currentScrollY = window.scrollY || document.documentElement.scrollTop
  if (currentScrollY > scrollY.value && currentScrollY > 200) {
    isHidden.value = true
  } else {
    isHidden.value = false
  }

  scrollY.value = currentScrollY
}

// 获取购物车数量（模拟数据）
const getCartCount = () => {
  cartCount.value = 3 // 模拟数据
}

// 获取未读消息数量
const getMessageCount = async () => {
  // 未登录时不调用API
  if (!getToken()) {
    messageCount.value = 0
    return
  }
  try {
    const response = await getUnreadCount()
    console.log('未读消息数量API响应:', response)
    if (response.code === 200) {
      messageCount.value = response.data || 0
      console.log('未读消息数量:', messageCount.value)
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
    messageCount.value = 0
  }
}

// 获取AI助手未读消息数量（模拟数据）
const getChatUnreadCount = () => {
  chatUnreadCount.value = 2 // 模拟未读消息数量
}

// 生命周期
onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  getCartCount()
  getMessageCount()
  getChatUnreadCount()

  // 初始化滚动位置
  scrollY.value = window.scrollY || document.documentElement.scrollTop

  // 定时刷新未读消息数量
  const refreshInterval = setInterval(() => {
    getMessageCount()
  }, 2000) // 每2秒刷新一次
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 样式保持不变 */
.fixed-sidebar {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: all 0.3s ease;
  opacity: 1;
  visibility: visible;
}

.fixed-sidebar.sidebar-hidden {
  opacity: 0;
  visibility: hidden;
  transform: translateY(-50%) translateX(20px);
}

.sidebar-item {
  position: relative;
  width: 50px;
  height: 50px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid #f0f0f0;
}

.sidebar-item:hover {
  background: #fff5f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 85, 0, 0.15);
  border-color: #ff5500;
}

.sidebar-item:hover .item-icon {
  color: #ff5500;
}

.sidebar-item:hover .item-text {
  color: #ff5500;
}

.item-icon {
  position: relative;
  font-size: 20px;
  color: #666;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-text {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  transition: color 0.3s ease;
  font-weight: 500;
}

.item-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff5500;
  color: white;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(255, 85, 0, 0.3);
  animation: pulse 2s infinite;
}

/* 人工客服按钮特殊样式 */
.sidebar-item.human-service {
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
}

.sidebar-item.human-service .item-icon {
  color: white;
}

.sidebar-item.human-service .item-text {
  color: white;
}

.sidebar-item.human-service:hover {
  background: linear-gradient(135deg, #ff6600 0%, #ffaa00 100%);
  border-color: #ff7700;
}

/* 红点脉冲动画 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0.7);
  }

  70% {
    box-shadow: 0 0 0 6px rgba(255, 85, 0, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .fixed-sidebar {
    right: 10px;
    gap: 8px;
  }

  .sidebar-item {
    width: 45px;
    height: 45px;
  }

  .item-icon {
    font-size: 18px;
  }

  .item-text {
    font-size: 11px;
  }

  .item-badge {
    min-width: 16px;
    height: 16px;
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .fixed-sidebar {
    right: 5px;
    gap: 6px;
  }

  .sidebar-item {
    width: 40px;
    height: 40px;
  }

  .item-icon {
    font-size: 16px;
  }

  .item-text {
    font-size: 10px;
  }

  .fixed-sidebar.sidebar-hidden {
    right: -50px;
  }
}

/* 打印时隐藏 */
@media print {
  .fixed-sidebar {
    display: none;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .sidebar-item {
    border: 2px solid #333;
  }

  .sidebar-item:hover {
    border-color: #ff5500;
    background: #fff0e6;
  }
}

/* 减少动画模式支持 */
@media (prefers-reduced-motion: reduce) {

  .fixed-sidebar,
  .sidebar-item,
  .item-icon,
  .item-text {
    transition: none;
  }

  .sidebar-item:hover {
    transform: none;
  }

  .item-badge {
    animation: none;
  }
}
</style>