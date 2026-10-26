<template>
  <div class="chat-page">
    <!-- 顶部导航 -->
    <div class="chat-header">
      <button type="button" class="back-btn" aria-label="返回上一页" @click="goBack">←</button>
      <div class="service-info">
        <div class="service-avatar">
          <img :src="aiAvatarImage" alt="逛逛头像" class="ai-avatar">
        </div>
        <div class="service-name-status">
          <div class="service-name">逛逛 <span class="assistant-badge">商城 AI 助手</span></div>
          <div class="service-status">
            <span class="online-dot" :class="{ online: isConnected }"></span>
            {{ isConnected ? '已连接 · 商品咨询与订单问答' : '正在连接逛逛...' }}
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button type="button" class="action-btn" aria-label="切换快捷回复" :aria-pressed="showQuickReplies" @click="toggleQuickReplies">💡</button>
        <button type="button" class="action-btn" aria-label="清空聊天记录" @click="clearChat">🗑️</button>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container" ref="chatContainer">
      <section v-if="selectedProduct" class="product-context" aria-label="当前咨询商品">
        <img :src="getProductImageUrl(selectedProduct.mainImage) || DEFAULT_PRODUCT_IMAGE" :alt="selectedProduct.name" @error="handleImageError" />
        <div class="product-context-copy">
          <span class="context-label">正在咨询这件商品</span>
          <strong>{{ selectedProduct.name }}</strong>
          <div class="context-meta"><span class="context-price">¥{{ formatPrice(selectedProduct.price) }}</span><span>库存 {{ selectedProduct.stock ?? '—' }}</span></div>
        </div>
        <button type="button" class="view-product-btn" @click="viewSelectedProduct">查看商品</button>
        <button type="button" class="clear-context-btn" aria-label="取消商品上下文" title="取消商品上下文" @click="clearProductContext">×</button>
      </section>

      <!-- 欢迎消息 -->
      <div class="welcome-message" v-if="showWelcomeMessage">
        <button type="button" class="welcome-close-btn" aria-label="关闭欢迎提示" @click="closeWelcomeMessage">×</button>
        <div class="welcome-content">
          <div class="welcome-eyebrow"><span class="welcome-sparkle">✦</span> GOGO SHOPPING ASSISTANT</div>
          <div class="welcome-title">{{ selectedProduct ? '关于这件商品，尽管问我' : '你好，我是逛逛' }}</div>
          <div class="welcome-text">{{ selectedProduct ? `我已为你带入「${selectedProduct.name}」的商品信息，可以帮你了解参数、价格和购买前常见问题。` : '从挑选商品到了解商城服务，我会结合商品资料和商城知识帮你解答。' }}</div>
          <div class="welcome-tips">
            <span class="tip-item">商品推荐</span><span class="tip-item">价格与库存</span>
            <span class="tip-item">配送与售后</span><span class="tip-item">登录后查询本人订单</span>
          </div>
        </div>
      </div>

      <!-- 聊天记录 -->
      <div class="chat-history" ref="chatHistory">
        <div v-for="(msg, index) in chatList" :key="index" :class="msg.type === 'user' ? 'user-msg' : 'ai-msg'"
          :ref="el => { if (el && index === chatList.length - 1) lastMessageRef = el }">
          <template v-if="msg.type === 'ai'">
            <div class="avatar-wrapper ai-avatar-wrapper">
              <img :src="aiAvatarImage" class="avatar ai-avatar" alt="逛逛">
              <div class="avatar-badge ai-badge">助手</div>
            </div>
          </template>
          <div class="msg-content" :class="{ 'product-list': isProductList(msg.content) }">
            <span v-html="formatChatMessage(msg.content)"></span>
            <section v-if="msg.toolResults?.length" class="tool-result-list" aria-label="业务查询结果">
              <article v-for="(result, resultIndex) in msg.toolResults" :key="`${result.tool}-${resultIndex}`" class="tool-result-card">
                <strong>{{ result.tool === 'search_products' ? '商品查询结果' : '本人订单状态' }}</strong>
                <template v-if="result.tool === 'search_products'">
                  <div v-for="item in result.items" :key="item.id" class="tool-product-row">
                    <span class="tool-product-name">{{ item.name }}</span>
                    <span class="tool-product-meta">¥{{ formatPrice(item.price) }} · 库存 {{ item.stock ?? '—' }}</span>
                    <button type="button" class="source-product-link" @click="viewSourceProduct(item.id)">查看商品 →</button>
                  </div>
                </template>
                <template v-else-if="result.tool === 'get_my_order_status'">
                  <div v-for="(item, itemIndex) in result.items" :key="`${item.orderNoMasked}-${itemIndex}`" class="tool-order-row">
                    <span>订单尾号 {{ item.orderNoMasked }}</span><strong>{{ item.status }}</strong>
                  </div>
                </template>
              </article>
            </section>
            <details v-if="msg.sources && msg.sources.length" class="chat-sources">
              <summary>参考资料（{{ msg.sources.length }}）</summary>
              <ul class="source-list">
                <li v-for="(source, sourceIndex) in msg.sources" :key="`${source.title || '来源'}-${sourceIndex}`">
                  <strong>{{ source.title || '商城资料' }}</strong>
                  <small>{{ sourceTypeLabel(source.sourceType) }}<span v-if="source.sourceId != null"> · #{{ source.sourceId }}</span></small>
                  <p>{{ source.content }}</p>
                  <button v-if="source.sourceType === 'product' && source.sourceId != null" type="button" class="source-product-link" @click="viewSourceProduct(source.sourceId)">查看商品 →</button>
                </li>
              </ul>
            </details>
            <button v-if="msg.handoff" type="button" class="handoff-link" @click="goToHumanService">转接人工客服</button>
            <div class="msg-time">{{ formatTime(msg.time) }}</div>
          </div>
          <template v-if="msg.type === 'user'">
            <div class="avatar-wrapper user-avatar-wrapper">
              <img :src="userAvatarImage" class="avatar user-avatar" alt="用户">
              <div class="avatar-badge user-badge">你</div>
            </div>
          </template>
        </div>

        <!-- 助手正在回复指示器 -->
        <div v-if="isAiTyping" class="ai-msg typing-indicator">
          <div class="avatar-wrapper ai-avatar-wrapper">
            <img :src="aiAvatarImage" class="avatar ai-avatar" alt="逛逛">
            <div class="avatar-badge ai-badge">助手</div>
          </div>
          <div class="typing-bubble">
            <div class="typing-text">逛逛正在回复...</div>
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- 底部锚点元素，用于自动滚动 -->
        <div ref="scrollAnchor" class="scroll-anchor"></div>
      </div>

      <!-- 快捷回复区域 -->
      <div class="quick-replies" v-if="!isAiTyping && showQuickReplies">
        <div class="quick-reply-title">💭 不知道问什么？试试这些：</div>
        <div class="quick-reply-buttons">
          <button type="button" v-for="(example, index) in exampleQuestions" :key="index" @click="useExample(example)"
            :disabled="!isConnected" class="quick-reply-btn">
            {{ example }}
          </button>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <div class="user-icon-wrapper">
          <img :src="userAvatarImage" class="user-icon" alt="用户">
          </div>
          <input v-model="inputMsg" aria-label="咨询问题" :placeholder="productContextLoading ? '正在准备商品信息...' : isRequestInFlight ? '逛逛正在整理答案...' : '请输入您想咨询的问题...'" @keyup.enter="sendMessage"
            @keyup.ctrl.enter="inputMsg += '\n'" :disabled="!isConnected || productContextLoading || isRequestInFlight" class="chat-input" ref="chatInput" />
          <div class="input-actions">
            <button type="button" @click="sendMessage" :disabled="!isConnected || productContextLoading || isRequestInFlight || !inputMsg.trim()" class="send-btn">
              <span v-if="isConnected">发送</span>
              <span v-else>连接中...</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import aiAvatarImage from '@/assets/logo/logo.png'
import { DEFAULT_AVATAR_IMAGE, DEFAULT_PRODUCT_IMAGE, getProductImageUrl, handleImageError } from '@/utils/image'
import { formatChatMessage } from '@/utils/chat'
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue';
import { useStore } from 'vuex';
import { getWebSocketUrl } from '@/utils/request';
import { getToken } from '@/utils/auth';
import { useRoute, useRouter } from 'vue-router';
import { getProduct } from '@/api/system/product'

// Vuex store
const store = useStore();
const router = useRouter();
const route = useRoute();

const userAvatarImage = computed(() => store.state.user?.avatar || DEFAULT_AVATAR_IMAGE)
// 响应式数据
const chatList = ref([]);
const inputMsg = ref('');
const isConnected = ref(false);
const isRequestInFlight = ref(false)
const isAiTyping = ref(false);
const showQuickReplies = ref(true);
const currentAiMessage = ref('');
const currentSources = ref([]);
const currentToolResults = ref([])
const currentHandoff = ref(false);
const conversationId = ref('')
const autoScrollEnabled = ref(true);
const showWelcomeMessage = ref(true); // 控制欢迎消息显示
const selectedProduct = ref(null)
const productContextLoading = ref(false)
let productContextRequestId = 0

// 获取DOM引用
const chatContainer = ref(null);
const chatHistory = ref(null);
const chatInput = ref(null);
const scrollAnchor = ref(null);
const lastMessageRef = ref(null);

// 示例问题
const exampleQuestions = computed(() => selectedProduct.value
  ? [`${selectedProduct.value.name}有什么特点？`, '这件商品现在有货吗？', '这件商品适合什么人群？', '配送和售后规则是什么？']
  : ['帮我推荐一款手机', '怎么查看订单物流？', '商城支持哪些支付方式？', '如何申请退换货？'])

// WebSocket实例
let ws = null;
let reconnectTimer = null;
let isUnmounted = false;
const pendingTimeouts = new Set();

const conversationStorageKey = () => `gogo-chat-conversation:${store.state.user?.userId || (getToken() ? 'authenticated' : 'guest')}`
const saveConversationId = (value) => {
  conversationId.value = value || ''
  try {
    if (conversationId.value) localStorage.setItem(conversationStorageKey(), conversationId.value)
    else localStorage.removeItem(conversationStorageKey())
  } catch (error) {
    console.warn('逛逛会话编号无法保存到本地', error?.message || error)
  }
}

const scheduleTimeout = (callback, delay) => {
  const timer = window.setTimeout(() => {
    pendingTimeouts.delete(timer);
    if (!isUnmounted) callback();
  }, delay);
  pendingTimeouts.add(timer);
  return timer;
};

const scheduleReconnect = (delay) => {
  if (isUnmounted || reconnectTimer) return;
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null;
    initWebSocket();
  }, delay);
};

// 关闭欢迎消息
const closeWelcomeMessage = () => {
  showWelcomeMessage.value = false;
  forceScrollToBottom();
};

// 判断是否是商品列表
const isProductList = (content) => {
  return content.includes('商品列表') || content.includes('AI 智能推荐');
};

const formatPrice = (value) => {
  if (value == null || value === '') return '暂无报价'
  const price = Number(value)
  return Number.isFinite(price) ? price.toFixed(2) : '暂无报价'
}

const sourceTypeLabel = (type) => ({ faq: '常见问题', policy: '商城政策', product: '商品资料' })[type] || '商城资料'

const loadProductContext = async (rawId) => {
  const requestId = ++productContextRequestId
  const productId = Number(rawId)
  selectedProduct.value = null
  productContextLoading.value = Number.isSafeInteger(productId) && productId > 0
  if (!productContextLoading.value) return
  try {
    const response = await getProduct(productId)
    if (requestId !== productContextRequestId) return
    if (response?.code === 200 && response.data?.id) selectedProduct.value = response.data
  } catch (error) {
    if (requestId === productContextRequestId) console.warn('商品咨询上下文加载失败', error?.message || error)
  } finally {
    if (requestId === productContextRequestId) productContextLoading.value = false
  }
}

const viewSelectedProduct = () => {
  if (selectedProduct.value?.id) router.push({ path: '/product/detail', query: { id: selectedProduct.value.id } })
}

const viewSourceProduct = (productId) => router.push({ path: '/product/detail', query: { id: productId } })

const clearProductContext = async () => {
  selectedProduct.value = null
  const query = { ...route.query }
  delete query.productId
  await router.replace({ path: route.path, query })
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';

  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 增强的滚动到底部函数
const scrollToBottom = () => {
  if (!autoScrollEnabled.value) return;

  nextTick(() => {
    if (scrollAnchor.value) {
      scrollAnchor.value.scrollIntoView({
        behavior: 'smooth',
        block: 'end'
      });
    }

    scheduleTimeout(() => {
      if (chatHistory.value) {
        chatHistory.value.scrollTop = chatHistory.value.scrollHeight;
      }
    }, 50);
  });
};

// 强制滚动到底部（无视autoScrollEnabled设置）
const forceScrollToBottom = () => {
  nextTick(() => {
    if (scrollAnchor.value) {
      scrollAnchor.value.scrollIntoView({
        behavior: 'smooth',
        block: 'end'
      });
    }
  });
};

// 检查用户是否在手动滚动
const checkUserScroll = () => {
  if (!chatHistory.value) return;

  const { scrollTop, scrollHeight, clientHeight } = chatHistory.value;
  const isAtBottom = scrollHeight - scrollTop - clientHeight < 50;

  if (scrollTop < 100) {
    autoScrollEnabled.value = false;
  }
  else if (isAtBottom) {
    autoScrollEnabled.value = true;
    forceScrollToBottom();
  }
};

// 初始化WebSocket连接
const initWebSocket = () => {
  try {
    const wsUrl = getWebSocketUrl();

    ws = new WebSocket(wsUrl);

    ws.onopen = (event) => {
      if (isUnmounted) return;
      isConnected.value = true;
      addSystemMessage('已连接到逛逛');
      if (conversationId.value) {
        ws.send(JSON.stringify({ action: 'resume', conversationId: conversationId.value, token: getToken() || '' }))
      }
      forceScrollToBottom();
    };

    ws.onclose = (event) => {
      if (isUnmounted) return;
      isConnected.value = false;
      isRequestInFlight.value = false
      isAiTyping.value = false;
      addSystemMessage('连接已断开，正在尝试重新连接...');
      forceScrollToBottom();

      scheduleReconnect(3000);
    };

    ws.onerror = (error) => {
      if (isUnmounted) return;
      console.error('❌ WebSocket错误：', error);
      isConnected.value = false;
      isRequestInFlight.value = false
      isAiTyping.value = false;
      addSystemMessage('连接失败，请检查后端服务是否启动');
      forceScrollToBottom();
    };

    ws.onmessage = (event) => {
      if (isUnmounted) return;
      try {
        const response = JSON.parse(event.data);
        const { type, content } = response;

        if (type === 'conversation') {
          saveConversationId(content)
        } else if (type === 'history') {
          chatList.value = Array.isArray(content) ? content
            .filter(message => ['user', 'assistant'].includes(message?.role) && typeof message.content === 'string')
            .map(message => ({ type: message.role === 'user' ? 'user' : 'ai', content: message.content, time: Date.now() })) : []
          forceScrollToBottom()
        } else if (type === 'stream') {
          currentAiMessage.value += content;

          const lastMsg = chatList.value[chatList.value.length - 1];
          if (lastMsg && lastMsg.type === 'ai') {
            lastMsg.content = currentAiMessage.value;
          } else {
            chatList.value.push({
              type: 'ai',
              content: currentAiMessage.value,
              time: Date.now()
            });
          }

          isAiTyping.value = true;
          forceScrollToBottom();

        } else if (type === 'sources') {
          currentSources.value = Array.isArray(content) ? content : [];
        } else if (type === 'tool_result') {
          if (content && ['search_products', 'get_my_order_status'].includes(content.tool) && Array.isArray(content.items)) {
            currentToolResults.value.push(content)
          }
        } else if (type === 'handoff') {
          currentHandoff.value = true;
        } else if (type === 'end') {
          isRequestInFlight.value = false
          isAiTyping.value = false;
          const lastMsg = chatList.value[chatList.value.length - 1];
          if (lastMsg && lastMsg.type === 'ai') {
            lastMsg.sources = currentSources.value;
            lastMsg.toolResults = currentToolResults.value.slice()
            lastMsg.handoff = currentHandoff.value;
          }
          currentSources.value = [];
          currentToolResults.value = []
          currentHandoff.value = false;
          currentAiMessage.value = '';

          scheduleTimeout(() => {
            forceScrollToBottom();
          }, 100);

        } else if (type === 'error') {
          addErrorMessage(content);
          isRequestInFlight.value = false
          isAiTyping.value = false;
          currentToolResults.value = []
          currentSources.value = []
          currentAiMessage.value = '';
          forceScrollToBottom();
        }
      } catch (error) {
        console.error('消息解析失败', error);
        addErrorMessage('消息格式错误，请重试');
        isRequestInFlight.value = false
        isAiTyping.value = false;
        currentToolResults.value = []
        currentSources.value = []
        currentAiMessage.value = '';
        forceScrollToBottom();
      }
    };

  } catch (error) {
    console.error('创建WebSocket连接失败:', error);
    addSystemMessage('连接失败: ' + error.message);
    forceScrollToBottom();

    scheduleReconnect(5000);
  }
};

// 添加用户消息
const addUserMessage = (text) => {
  chatList.value.push({
    type: 'user',
    content: text,
    time: Date.now()
  });
  forceScrollToBottom();
  autoScrollEnabled.value = true;
};

// 添加系统消息
const addSystemMessage = (text) => {
  chatList.value.push({
    type: 'ai',
    content: `系统提示：${text}`,
    time: Date.now()
  });
  forceScrollToBottom();
};

// 添加错误消息
const addErrorMessage = (text) => {
  chatList.value.push({
    type: 'ai',
    content: `错误：${text}`,
    time: Date.now()
  });
  forceScrollToBottom();
};

// 发送消息
const sendMessage = () => {
  const text = inputMsg.value.trim();
  if (!text || !isConnected.value || !ws || isRequestInFlight.value || productContextLoading.value) {
    return;
  }

  try {
    ws.send(JSON.stringify({
      question: text,
      productId: selectedProduct.value?.id || null,
      token: getToken() || '',
      conversationId: conversationId.value || null
    }));
    isRequestInFlight.value = true
    addUserMessage(text);
  } catch (error) {
    addSystemMessage('消息发送失败，请检查连接后重试');
    return;
  }

  inputMsg.value = '';

  nextTick(() => {
    if (chatInput.value) {
      chatInput.value.focus();
    }
  });
};

// 使用示例问题
const useExample = (example) => {
  if (!isConnected.value) {
    addSystemMessage('连接未建立，请等待连接成功');
    return;
  }

  inputMsg.value = example;
  sendMessage();
};

// 转到平台人工客服
const goToHumanService = () => router.push('/customer-service');

// 返回上一页
const goBack = () => {
  window.history.back();
};

// 切换快捷回复显示
const toggleQuickReplies = () => {
  showQuickReplies.value = !showQuickReplies.value;
  forceScrollToBottom();
};

// 清空聊天记录
const clearChat = () => {
  if (confirm('确定要清空聊天记录吗？')) {
    if (isConnected.value && ws && conversationId.value) {
      try { ws.send(JSON.stringify({ action: 'clear', conversationId: conversationId.value, token: getToken() || '' })) }
      catch (error) { console.warn('逛逛会话清理请求发送失败', error?.message || error) }
    }
    saveConversationId('')
    chatList.value = [];
    currentSources.value = []
    currentToolResults.value = []
    addSystemMessage('聊天记录已清空');
    forceScrollToBottom();
  }
};

// 手动滚动到底部
const scrollToBottomManually = () => {
  autoScrollEnabled.value = true;
  forceScrollToBottom();
};

// 生命周期钩子
const handleGlobalKeydown = (event) => {
  if (event.ctrlKey && event.key === 'ArrowDown') {
    event.preventDefault();
    scrollToBottomManually();
  }
};

onMounted(() => {
  isUnmounted = false;
  try { conversationId.value = localStorage.getItem(conversationStorageKey()) || '' }
  catch (error) { conversationId.value = '' }
  loadProductContext(route.query.productId)
  initWebSocket();

  nextTick(() => {
    if (chatInput.value) {
      chatInput.value.focus();
    }
  });

  if (chatHistory.value) {
    chatHistory.value.addEventListener('scroll', checkUserScroll);
  }

  document.addEventListener('keydown', handleGlobalKeydown);
});

watch(() => route.query.productId, (productId) => loadProductContext(productId))

onUnmounted(() => {
  isUnmounted = true;
  isRequestInFlight.value = false
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  pendingTimeouts.forEach((timer) => window.clearTimeout(timer));
  pendingTimeouts.clear();
  if (ws) {
    ws.onclose = null;
    ws.close();
    ws = null;
  }

  if (chatHistory.value) {
    chatHistory.value.removeEventListener('scroll', checkUserScroll);
  }

  document.removeEventListener('keydown', handleGlobalKeydown);
});
</script>

<style scoped>
.chat-page {
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #e5e5e5;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.1);
}

/* 头部样式 */
.chat-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 24px;
  color: #666;
  margin-right: 12px;
  cursor: pointer;
  transition: color 0.3s;
}

.back-btn:hover {
  color: #1890ff;
}

.back-btn:focus-visible,
.action-btn:focus-visible,
.welcome-close-btn:focus-visible {
  outline: 2px solid #1890ff;
  outline-offset: 2px;
}

.service-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.service-avatar img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  margin-right: 12px;
  border: 2px solid #1890ff;
  padding: 2px;
  object-fit: cover;
}

.ai-avatar {
  border-color: #1890ff;
}

.service-name-status {
  display: flex;
  flex-direction: column;
}

.service-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  font-size: 16px;
}

.service-status {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
}

.online-dot {
  width: 8px;
  height: 8px;
  background-color: #ccc;
  border-radius: 50%;
  margin-right: 6px;
  transition: background-color 0.3s;
}

.online-dot.online {
  background-color: #52c41a;
  box-shadow: 0 0 5px #52c41a;
}

.header-actions {
  display: flex;
  gap: 20px;
  font-size: 20px;
  color: #666;
}

.action-btn {
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px;
  border-radius: 4px;
}

.action-btn:hover {
  color: #1890ff;
  background: #f0f7ff;
  transform: scale(1.1);
}

/* 聊天容器 */
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  position: relative;
}

.chat-history {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scroll-behavior: smooth;
  position: relative;
}

/* 底部锚点，用于自动滚动 */
.scroll-anchor {
  height: 1px;
  width: 100%;
  margin: 0;
  padding: 0;
  opacity: 0;
  pointer-events: none;
}

/* 欢迎消息样式（添加了关闭按钮） */
.welcome-message {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 18px;
  margin: 10px auto;
  max-width: 85%;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  animation: slideIn 0.5s ease-out;
  position: relative;
}

/* 关闭按钮 */
.welcome-close-btn {
  position: absolute;
  top: 8px;
  right: 12px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  border: 0;
  padding: 0;
  font-family: inherit;
  transition: all 0.3s;
  z-index: 10;
}

.welcome-close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.welcome-content {
  text-align: center;
  position: relative;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 12px;
  animation: float 3s ease-in-out infinite;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
}

.welcome-text {
  font-size: 16px;
  margin-bottom: 15px;
  opacity: 0.9;
}

.welcome-tips {
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  opacity: 0.8;
}

.tip-item {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 12px;
  margin: 2px;
}

/* 头像样式 */
.avatar-wrapper {
  position: relative;
  margin: 0 12px;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.ai-avatar {
  border-color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.user-avatar {
  border-color: #52c41a;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.avatar-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 3px;
  border-radius: 10px;
  color: white;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  white-space: nowrap;
}

.ai-badge {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.user-badge {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

/* 消息样式 */
.user-msg,
.ai-msg {
  display: flex;
  align-items: flex-start;
  max-width: 100%;
  animation: fadeIn 0.3s ease-out;
}

.ai-msg {
  align-items: flex-start;
}

.user-msg {
  flex-direction: row-reverse;
}

.msg-content {
  max-width: 75%;
  padding: 14px 18px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.6;
  position: relative;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
  animation: messagePop 0.3s ease-out;
}

.msg-content:hover {
  transform: translateY(-2px);
}

.msg-content.product-list {
  max-width: 90%;
  background: white;
  border-left: 4px solid #1890ff;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.ai-msg .msg-content {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #333;
  border-top-left-radius: 4px;
  border: 1px solid #e8e8e8;
}

.user-msg .msg-content {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border-top-right-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
  text-align: right;
}

.user-msg .msg-time {
  color: rgba(255, 255, 255, 0.7);
  text-align: left;
}

/* 价格高亮 */
:deep(.price) {
  color: #f5222d;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
}

:deep(.highlight) {
  color: #1890ff;
  background: #e6f7ff;
  padding: 1px 4px;
  border-radius: 3px;
}

/* 输入区域 */
.input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #eee;
  position: sticky;
  bottom: 0;
  z-index: 100;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #52c41a;
}

.chat-input {
  flex: 1;
  height: 48px;
  padding: 0 20px;
  border: 2px solid #e8e8e8;
  border-radius: 24px;
  outline: none;
  font-size: 15px;
  transition: all 0.3s;
  background: #fafafa;
}

.chat-input:focus {
  border-color: #1890ff;
  background: white;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  height: 48px;
  padding: 0 24px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #ccc;
}

/* 快捷回复区域 */
.quick-replies {
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
}

.quick-reply-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  font-weight: 500;
}

.quick-reply-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-reply-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.quick-reply-btn:hover:not(:disabled) {
  background: #1890ff;
  color: white;
  border-color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.quick-reply-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 正在输入指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
}

.typing-bubble {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #e5e5e5;
  border-radius: 18px;
  padding: 12px 18px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.typing-text {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background-color: #666;
  border-radius: 50%;
  animation: typing 1.5s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

/* 滚动条美化 */
.chat-history::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* 动画 */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes messagePop {
  0% {
    transform: scale(0.9);
    opacity: 0;
  }

  70% {
    transform: scale(1.05);
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes typing {

  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-4px);
  }
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-10px);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .chat-page {
    border: none;
    height: 100vh;
  }

  .chat-header {
    padding: 12px;
  }

  .chat-history {
    padding: 12px;
  }

  .avatar {
    width: 36px;
    height: 36px;
  }

  .msg-content {
    max-width: 85%;
    padding: 12px 16px;
  }

  .msg-content.product-list {
    max-width: 95%;
  }

  .user-icon {
    width: 32px;
    height: 32px;
  }

  .welcome-message {
    max-width: 90%;
    padding: 20px;
  }

  .welcome-close-btn {
    top: 6px;
    right: 8px;
    width: 20px;
    height: 20px;
    font-size: 16px;
  }
}
</style>

<style scoped>
.chat-sources { margin-top: 8px; color: #7a8491; font-size: 12px; line-height: 1.6; }
.chat-sources summary { width: fit-content; cursor: pointer; color: #546477; }
.source-list { display: grid; gap: 8px; margin: 8px 0 0; padding: 0; list-style: none; }
.source-list li { padding: 8px 10px; border: 1px solid #e8edf2; border-radius: 8px; background: #fafbfc; }
.source-list strong { color: #465466; font-weight: 600; }
.source-list small { display: block; margin-top: 3px; color: #9aa3ad; font-size: 11px; }
.source-list p { margin: 4px 0 0; color: #7a8491; white-space: pre-wrap; }
.handoff-link { margin-top: 8px; border: 0; padding: 0; background: transparent; color: #409eff; cursor: pointer; font-size: 13px; }
</style>

<style scoped>
.chat-page {
  width: min(100% - 40px, 1180px);
  height: min(820px, calc(100vh - 148px));
  min-height: 560px;
  margin: 28px auto;
  overflow: hidden;
  border: 1px solid #ece8e4;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 18px 55px rgba(55, 45, 37, .1);
}
.chat-header { min-height: 76px; padding: 14px 24px; border-bottom: 1px solid #f0eeeb; background: linear-gradient(110deg, #fff, #fffaf6); }
.service-avatar { box-shadow: 0 0 0 4px #fff2e8; }
.service-name { color: #292d32; font-size: 18px; font-weight: 750; }
.assistant-badge { margin-left: 8px; padding: 4px 9px; border-radius: 20px; background: #fff0e5; color: #e76d2f; font-size: 11px; font-weight: 650; vertical-align: 2px; }
.service-status { margin-top: 3px; color: #90979e; font-size: 12px; }
.online-dot.online { background: #43b581; box-shadow: 0 0 0 3px #e8f7ef; }
.action-btn { border-radius: 10px; background: #f6f5f3; }
.action-btn:hover { background: #fff0e5; }
.chat-container { height: calc(100% - 76px); padding: 22px 32px 18px; background: radial-gradient(ellipse at 50% 0%, #fffaf6 0, #fff 48%); }
.product-context { display: flex; align-items: center; gap: 14px; max-width: 820px; margin: 0 auto 16px; padding: 10px 14px; border: 1px solid #f4dfd1; border-radius: 15px; background: #fffaf6; }
.product-context > img { width: 58px; height: 58px; flex: 0 0 auto; border-radius: 10px; background: #f4f2ef; object-fit: cover; }
.product-context-copy { min-width: 0; flex: 1; }
.context-label { display: block; color: #99918b; font-size: 11px; }
.product-context-copy strong { display: block; overflow: hidden; margin-top: 2px; color: #35383c; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.context-meta { display: flex; gap: 12px; margin-top: 3px; color: #99918b; font-size: 11px; }
.context-price { color: #e85d35; font-size: 13px; font-weight: 700; }
.view-product-btn, .source-product-link { border: 0; background: transparent; color: #df6638; cursor: pointer; font-size: 12px; font-weight: 650; white-space: nowrap; }
.clear-context-btn { width: 27px; height: 27px; border: 0; border-radius: 50%; background: #f6eee8; color: #8e8178; cursor: pointer; font-size: 19px; line-height: 1; }
.welcome-message { position: relative; max-width: 820px; margin: 0 auto 18px; padding: 20px 24px; border: 1px solid #f5e6dc; border-radius: 18px; background: linear-gradient(115deg, #fff7f0, #fffdfb 70%); box-shadow: 0 7px 20px rgba(118, 84, 58, .045); }
.welcome-eyebrow { color: #c57950; font-size: 10px; font-weight: 750; letter-spacing: .12em; }
.welcome-sparkle { margin-right: 5px; color: #f09a62; }
.welcome-title { margin-top: 8px; color: #34383d; font-size: 21px; font-weight: 750; }
.welcome-text { margin-top: 5px; color: #777e85; font-size: 13px; line-height: 1.65; }
.welcome-tips { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 12px; }
.tip-item { padding: 5px 9px; border: 1px solid #f2e4da; border-radius: 20px; background: #fff; color: #827870; font-size: 11px; }
.welcome-close-btn { color: #aa9a8e; }
.chat-history, .quick-replies, .input-area { max-width: 900px; margin-left: auto; margin-right: auto; }
.msg-content { border-radius: 4px 16px 16px 16px; box-shadow: 0 3px 12px rgba(48, 48, 48, .045); }
.user-msg .msg-content { border-radius: 16px 4px 16px 16px; background: #e87543; }
.ai-avatar-wrapper .avatar { border: 2px solid #fff; box-shadow: 0 2px 8px #e8d9cf; }
.quick-replies { margin-top: 8px; margin-bottom: 12px; }
.quick-reply-title { color: #8e8c89; font-size: 11px; }
.quick-reply-buttons { gap: 7px; }
.quick-reply-btn { border-color: #eee7e1; border-radius: 18px; background: #fff; color: #696d71; font-size: 12px; }
.quick-reply-btn:hover:not(:disabled) { border-color: #efb08e; background: #fff7f1; color: #d86539; }
.input-wrapper { border: 1px solid #e9e5e1; border-radius: 16px; background: #fff; box-shadow: 0 5px 18px rgba(55, 45, 37, .055); }
.input-wrapper:focus-within { border-color: #e8a17b; box-shadow: 0 0 0 3px rgba(232, 117, 67, .09); }
.send-btn { border-radius: 11px; background: #e87543; }
.send-btn:hover:not(:disabled) { background: #d96333; }
.send-btn:disabled { background: #d8d4d0; }
.source-product-link { margin-top: 6px; padding: 0; }
.tool-result-list { display: grid; gap: 8px; margin-top: 12px; }
.tool-result-card { padding: 11px 12px; border: 1px solid #efe5dc; border-radius: 12px; background: #fffdfa; }
.tool-result-card > strong { display: block; margin-bottom: 7px; color: #69584d; font-size: 12px; }
.tool-product-row { display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: center; gap: 3px 12px; padding: 8px 0; border-top: 1px solid #f3eee9; }
.tool-product-name { overflow: hidden; color: #3f4449; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.tool-product-meta { color: #8f8984; font-size: 11px; }
.tool-product-row .source-product-link { grid-column: 2; grid-row: 1 / span 2; }
.tool-order-row { display: flex; justify-content: space-between; gap: 12px; padding: 8px 0; border-top: 1px solid #f3eee9; color: #77736f; font-size: 12px; }
.tool-order-row strong { color: #cf673e; font-size: 12px; }
@media (max-width: 700px) {
  .chat-page { width: 100%; height: 100dvh; min-height: 0; margin: 0; border: 0; border-radius: 0; }
  .chat-header { min-height: 66px; padding: 10px 14px; }
  .chat-container { height: calc(100% - 66px); padding: 14px 12px 10px; }
  .service-name { font-size: 16px; }
  .service-status { max-width: 210px; font-size: 10px; }
  .product-context { gap: 9px; padding: 8px; }
  .product-context > img { width: 48px; height: 48px; }
  .view-product-btn { font-size: 11px; }
  .welcome-message { padding: 16px; }
  .welcome-title { font-size: 18px; }
  .welcome-text { font-size: 12px; }
  .chat-history { padding: 8px 2px; }
  .quick-reply-buttons { flex-wrap: nowrap; overflow-x: auto; padding-bottom: 4px; }
  .quick-reply-btn { flex: 0 0 auto; }
  .input-area { padding-bottom: max(4px, env(safe-area-inset-bottom)); }
}
@media (max-width: 390px) {
  .assistant-badge { display: none; }
  .header-actions { gap: 4px; }
  .product-context-copy strong { max-width: 135px; }
  .context-meta { gap: 7px; }
}
</style>
