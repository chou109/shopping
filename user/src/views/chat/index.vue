<template>
  <div class="chat-page">
    <!-- 顶部导航 -->
    <div class="chat-header">
      <div class="back-btn" @click="goBack">←</div>
      <div class="service-info">
        <div class="service-avatar">
          <img src="https://cdn-icons-png.flaticon.com/512/4712/4712039.png" alt="AI客服头像" class="ai-avatar">
        </div>
        <div class="service-name-status">
          <div class="service-name">智能客服小蜜</div>
          <div class="service-status">
            <span class="online-dot" :class="{ online: isConnected }"></span>
            {{ isConnected ? '在线' : '连接中...' }}
          </div>
        </div>
      </div>
      <div class="header-actions">
        <span class="action-btn" @click="toggleQuickReplies">💡</span>
        <span class="action-btn" @click="clearChat">🗑️</span>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container" ref="chatContainer">
      <!-- 欢迎消息（添加了关闭按钮） -->
      <div class="welcome-message" v-if="showWelcomeMessage">
        <div class="welcome-close-btn" @click="closeWelcomeMessage">×</div>
        <div class="welcome-content">
          <div class="welcome-icon">🤖</div>
          <div class="welcome-title">👋 您好！</div>
          <div class="welcome-text">我是智能客服小蜜，很高兴为您服务~</div>
          <div class="welcome-tips">
            我可以帮您：
            <span class="tip-item">• 查询商品信息</span>
            <span class="tip-item">• 推荐商品</span>
            <span class="tip-item">• 解答售后问题</span>
            <span class="tip-item">• 查询物流信息</span>
            <span class="tip-item">• 查询订单状态</span>
          </div>
        </div>
      </div>
      
      <!-- 聊天记录 -->
      <div class="chat-history" ref="chatHistory">
        <div v-for="(msg, index) in chatList" :key="index" :class="msg.type === 'user' ? 'user-msg' : 'ai-msg'" :ref="el => { if (el && index === chatList.length - 1) lastMessageRef = el }">
          <template v-if="msg.type === 'ai'">
            <div class="avatar-wrapper ai-avatar-wrapper">
              <img src="https://cdn-icons-png.flaticon.com/512/4712/4712039.png" class="avatar ai-avatar" alt="AI客服">
              <div class="avatar-badge ai-badge">AI</div>
            </div>
          </template>
          <div class="msg-content" :class="{ 'product-list': isProductList(msg.content) }">
            <span v-html="formatMessage(msg.content)"></span>
            <div class="msg-time">{{ formatTime(msg.time) }}</div>
          </div>
          <template v-if="msg.type === 'user'">
            <div class="avatar-wrapper user-avatar-wrapper">
              <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" class="avatar user-avatar" alt="用户">
              <div class="avatar-badge user-badge">你</div>
            </div>
          </template>
        </div>
        
        <!-- AI正在输入指示器 -->
        <div v-if="isAiTyping" class="ai-msg typing-indicator">
          <div class="avatar-wrapper ai-avatar-wrapper">
            <img src="https://cdn-icons-png.flaticon.com/512/4712/4712039.png" class="avatar ai-avatar" alt="AI客服">
            <div class="avatar-badge ai-badge">AI</div>
          </div>
          <div class="typing-bubble">
            <div class="typing-text">AI正在输入...</div>
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
          <button 
            v-for="(example, index) in exampleQuestions" 
            :key="index"
            @click="useExample(example)"
            :disabled="!isConnected"
            class="quick-reply-btn"
          >
            {{ example }}
          </button>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <div class="user-icon-wrapper">
            <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" class="user-icon" alt="用户">
          </div>
          <input 
            v-model="inputMsg" 
            placeholder="请输入您想咨询的问题..." 
            @keyup.enter="sendMessage"
            @keyup.ctrl.enter="inputMsg += '\n'"
            :disabled="!isConnected"
            class="chat-input"
            ref="chatInput"
          />
          <div class="input-actions">
            <button 
              @click="sendMessage" 
              :disabled="!isConnected || !inputMsg.trim()"
              class="send-btn"
            >
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
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useStore } from 'vuex';
import { getWebSocketUrl } from '@/utils/request';

// Vuex store
const store = useStore();

// 获取当前用户ID
const currentUserId = computed(() => {
  return store.state.user?.userId || localStorage.getItem('userId') || null;
});
// 响应式数据
const chatList = ref([]);
const inputMsg = ref('');
const isConnected = ref(false);
const isAiTyping = ref(false);
const showQuickReplies = ref(true);
const currentAiMessage = ref('');
const autoScrollEnabled = ref(true);
const showWelcomeMessage = ref(true); // 控制欢迎消息显示

// 获取DOM引用
const chatContainer = ref(null);
const chatHistory = ref(null);
const chatInput = ref(null);
const scrollAnchor = ref(null);
const lastMessageRef = ref(null);

// 示例问题
const exampleQuestions = ref([
  '你好',
  '查看我的订单',
  '我的订单状态',
  '查看待付款订单',
  '有没有推荐的苹果手机？',
  '有没有推荐的小米手机？',
  '我想要2000元以上的苹果手机',
  '什么时候发货？',
<<<<<<< HEAD
  '如何退货？'
=======
  '如何退货？',
  '价格是多少？',
  //'帮我推荐500元以下的商品',
  '我想买华为的手机'
>>>>>>> c9eadacee5ed0110a18e451b8a34a4ddf2f5ce84
]);

// WebSocket实例
let ws = null;

// 关闭欢迎消息
const closeWelcomeMessage = () => {
  showWelcomeMessage.value = false;
  forceScrollToBottom();
};

// 判断是否是商品列表
const isProductList = (content) => {
  return content.includes('商品列表') || content.includes('AI 智能推荐');
};

// 格式化消息（处理换行和特殊字符）
const formatMessage = (content) => {
  if (!content) return '';
  
  // 保留换行符
  return content
    .replace(/\n/g, '<br>')
    .replace(/【(.*?)】/g, '<strong class="highlight">【$1】</strong>')
    .replace(/价格：\s*(\d+(\.\d{1,2})?)元/g, '价格：<span class="price">$1元</span>');
};

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
    
    setTimeout(() => {
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
    
    console.log('WebSocket连接地址:', wsUrl);
     fetch('http://localhost:9091/test/ws-status')
      .then(response => {
        console.log('✅ 后端服务可达');
        return response.text();
      })
      .then(data => {
        console.log('后端状态:', data);
      })
      .catch(error => {
        console.error('❌ 后端服务不可达:', error);
        addSystemMessage('后端服务未启动，请确保 Java 服务在 9091 端口运行');
      });


    ws = new WebSocket(wsUrl);
    // ws = new WebSocket('ws://localhost:9091/ai/chat');
    
        ws.onopen = (event) => {
      console.log('✅ WebSocket连接成功');
      isConnected.value = true;
      addSystemMessage('已连接到智能客服');
      forceScrollToBottom();
    };
    
    ws.onclose = (event) => {
      console.log('🔌 WebSocket连接关闭');
      isConnected.value = false;
      isAiTyping.value = false;
      addSystemMessage('连接已断开，正在尝试重新连接...');
      forceScrollToBottom();
      
      setTimeout(initWebSocket, 3000);
    };
    
    ws.onerror = (error) => {
      console.error('❌ WebSocket错误：', error);
      isConnected.value = false;
      isAiTyping.value = false;
      addSystemMessage('连接失败，请检查后端服务是否启动');
      forceScrollToBottom();
    };
    
    ws.onmessage = (event) => {
      try {
        const response = JSON.parse(event.data);
        const { type, content } = response;
        
        if (type === 'stream') {
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
          
        } else if (type === 'end') {
          isAiTyping.value = false;
          currentAiMessage.value = '';
          
          setTimeout(() => {
            forceScrollToBottom();
          }, 100);
          
        } else if (type === 'error') {
          addErrorMessage(content);
          isAiTyping.value = false;
          currentAiMessage.value = '';
          forceScrollToBottom();
        }
      } catch (error) {
        console.error('消息解析失败', error);
        addErrorMessage('消息格式错误，请重试');
        isAiTyping.value = false;
        currentAiMessage.value = '';
        forceScrollToBottom();
      }
    };
    
  } catch (error) {
    console.error('创建WebSocket连接失败:', error);
    addSystemMessage('连接失败: ' + error.message);
    forceScrollToBottom();
    
    setTimeout(initWebSocket, 5000);
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
  if (!text || !isConnected.value) {
    return;
  }
  
  addUserMessage(text);
  
  ws.send(JSON.stringify({
    question: text,
    productId: null,
    userId: currentUserId.value
  }));
  
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
    chatList.value = [];
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
onMounted(() => {
  initWebSocket();
  
  nextTick(() => {
    if (chatInput.value) {
      chatInput.value.focus();
    }
  });
  
  if (chatHistory.value) {
    chatHistory.value.addEventListener('scroll', checkUserScroll);
  }
  
  document.addEventListener('keydown', (e) => {
    if (e.ctrlKey && e.key === 'ArrowDown') {
      e.preventDefault();
      scrollToBottomManually();
    }
  });
});

onUnmounted(() => {
  if (ws) {
    ws.close();
  }
  
  if (chatHistory.value) {
    chatHistory.value.removeEventListener('scroll', checkUserScroll);
  }
  
  document.removeEventListener('keydown', () => {});
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
  font-size: 24px;
  color: #666;
  margin-right: 12px;
  cursor: pointer;
  transition: color 0.3s;
}

.back-btn:hover {
  color: #1890ff;
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
  width: 20px;
  height: 20px;
  border-radius: 50%;
  color: white;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}

.ai-badge {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.user-badge {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

/* 消息样式 */
.user-msg, .ai-msg {
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
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-4px);
  }
}

@keyframes float {
  0%, 100% {
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