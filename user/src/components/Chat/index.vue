<template>
  <div class="container">
    <div style="position: absolute; top: 10px; right: 10px;">
      <i style="font-size: 16px; padding-right: 20px;" @click="handleClose" class="el-icon-close icon-close"></i>
      <i style="font-size: 16px; padding-right: 20px;" @click="handleMinimize" class="el-icon-minus icon-close"></i>
    </div>

    <div class="chat-content">
      <div class="chat-wrapper" :key="item.content + Math.random() + new Date().getTime()" v-for="item in messageList">
        <!--     不是验证消息       -->
        <div v-if="item.event !== 'AUTHENTICATION'">
          <!--    客服      -->
          <div class="chat-friend" v-if="item.identity === 'ADMIN' || item.identity === 'ROBOT'">
            <div class="info-time">
              <img v-if="item.identity === 'ADMIN'" src="@/assets/images/kefu.svg" alt="" />
              <img v-if="item.identity === 'ROBOT'" src="@/assets/logo/logo.png" alt="" />
              <span>{{ item.sendName }}</span>
              <span>{{ getTime(item.messageTime) }}</span>
            </div>
            <div class="chat-text">
              {{ item.content }}
            </div>
          </div>

          <!--    我 （用户）      -->
          <div class="chat-me" v-else-if="item.identity === 'USER'">
            <div class="info-time">
              <img :src="item.senderAvatar" alt="" />
              <span>{{ item.sendName }}</span>
              <span>{{ getTime(item.messageTime) }}</span>

            </div>
            <div class="chat-text">
              {{ item.content }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr 4fr 1fr;">
      <span>
        <el-button class="button" @click="toggleHumanAndRobot">{{ isRobot ? '转人工客服' : '转在线客服' }}</el-button>
      </span>
      <el-popover placement="top">
        <template #reference>
          <el-button class="button">表情图标</el-button>
        </template>

        <div style="display: grid; grid-template-columns: repeat(5, 1fr); width: 250px;">
          <div v-for="(icon, index) in icons" style="height: 1.5em">
            <i ref="icons" :class="icon" @click="addIcon(index)"></i>
          </div>
        </div>
      </el-popover>
      <el-input v-model="userInputMessage" @keydown.enter.native="sendMessage"></el-input>
      <span>
        <el-button class="button" @click="sendMessage" @keyup.enter.native="sendMessage">发送</el-button>
      </span>
    </div>

    <el-dialog v-model="isCancel" append-to-body>
      由于长时间未对话，已断开连接
      <button @click="reConnect">重新连接</button>
      <button @click="cancelConnect">取消</button>
    </el-dialog>
  </div>
</template>

<script>
import { getAnswer } from '@/api/chat/qa'
import { mapGetters } from 'vuex'
import { addHistory } from '@/api/chat/history'
import { parseTime } from '@/utils/neu'

export default {
  name: 'Chat',
  data() {
    return {
      isRobot: true,  // true 机器人 false 人工
      messageList: [{
        identity: 'ADMIN',
        event: 'TALK',
        content: '您好，您有什么需要我帮助的，可以跟我聊聊天气、网站的导航哦',
        sendName: "在线客服",
        messageTime: new Date(),
        recipient: 'Server',
        senderAvatar: '@/assets/images/kefu.svg'
      }],
      socket: null,
      wsCloseTimer: null,
      isCancel: false,
      enterpriseLogo: '@/assets/401_images/401.gif',
      userInputMessage: '',

      icons: [
        'el-icon-platform-eleme',
        'el-icon-eleme',
        'el-icon-delete-solid',
        'el-icon-delete',
        'el-icon-s-tools',
        'el-icon-setting',
        'el-icon-user-solid',
        'el-icon-user',
        'el-icon-phone',
        'el-icon-phone-outline',
        'el-icon-more',
        'el-icon-more-outline',
        'el-icon-star-on',
        'el-icon-star-off',
        'el-icon-s-goods',
        'el-icon-goods',
        'el-icon-warning'
      ]
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
      if (this.socket) this.socket.close()
    },
    handleMinimize() {
      this.$emit('minimize')
    },

    initWs() {
      // 实例化socket
      this.socket = new WebSocket('ws://localhost:9091/websocket')
      // 监听socket连接m
      this.socket.onopen = this.handleOpen
      // 监听socket消息
      this.socket.onmessage = this.handleMessage
      // 监听socket错误信息
      this.socket.onerror = this.handleError
    },
    handleError() {
      this.isCancel = true
    },
    handleOpen() {
      const msg = JSON.stringify({
        id: this.userId,
        identity: 'USER',
        event: 'AUTHENTICATION',
        content: '我正在等待回答',
        sendName: this.name,
        messageTime: this.getNowDateTime(),
        recipient: 'Server',
        senderAvatar: this.avatar
      })
      console.log("onopen", msg)
      this.socket.send(msg)

      this.messageList.push({
        identity: '在线客服',
        event: 'TALK',
        content: '正在为您转接，请稍......',
        sendName: this.name,
        messageTime: new Date(),
        recipient: 'Server',
        senderAvatar: this.avatar
      })
    },

    async sendMessage() {
      const message = this.userInputMessage

      if (!message) return

      this.showMessage(this.createUserMessage(message))
      this.userInputMessage = ''

      if (this.isRobot) {
        const res = await getAnswer(message)
        if (res.data.length > 0) {
          const robotAnswer = {
            id: this.userId,
            identity: 'ROBOT',
            event: 'TALK',
            content: res.data[0].answer,
            sendName: '在线客服',
            messageTime: this.getNowDateTime(),
            recipient: 'Server'
          }
          // 显示回答
          this.showMessage(robotAnswer)
        } else {
          const tip = {
            id: this.userId,
            identity: 'ROBOT',
            event: 'TALK',
            content: '抱歉，您的问题暂时无法回复，请点击【转人工】按钮，进行转接',
            sendName: '在线客服',
            messageTime: this.getNowDateTime(),
            recipient: 'Server'
          }
          this.messageList.push(tip)
        }
      } else {
        this.reConnect()
        this.socket.send(JSON.stringify(this.createUserMessage(message)))
      }
    },

    //接收服务器发来的消息
    handleMessage(message) {
      // 解析 JSON 数据
      const Json = JSON.parse(message.data)
      this.showMessage(Json)
    },

    // 打开计时器
    startWSCloseTimer() {
      this.wsCloseTimer = setTimeout(() => {
        //this.isCancel = true
      }, 10000)
    },
    // 清除计时器
    cleanWSCloseTimer() {
      clearTimeout(this.wsCloseTimer)
    },

    // 重新连接
    reConnect() {
      this.isCancel = false
      this.cleanWSCloseTimer()
      this.startWSCloseTimer()
    },
    // 取消按钮 关闭 dialog 清除计时器 切换开关
    cancelConnect() {
      this.isCancel = false
      this.cleanWSCloseTimer()
      this.toggleHumanAndRobot()
    },

    // 切换在线客服和人工客服
    toggleHumanAndRobot() {
      if (this.isRobot) {
        this.initWs()
        this.startWSCloseTimer()
      } else {
        this.cleanWSCloseTimer()
        this.socket.close()
      }
      this.isRobot = !this.isRobot
      console.log(this.isRobot)
    },

    // 将我的消息显示
    showMessage(message) {
      this.messageList.push(message)
      addHistory(message)
    },

    getTime(timestamp) {
      return parseTime(timestamp)
    },
    getNowDateTime() {
      return this.getTime(new Date().getTime())
    },

    addIcon(index) {
      const content = getComputedStyle(this.$refs.icons[index], '::before')
        .getPropertyValue('content')
      this.userInputMessage += content.slice(1, content.length - 1)
    },

    createUserMessage(message) {
      return {
        id: this.userId,
        identity: 'USER',
        event: 'TALK',
        content: message,
        sendName: this.name,
        messageTime: this.getNowDateTime(),
        recipient: 'ADMIN',
        senderAvatar: this.avatar
      }
    }
  },
  computed: {
    ...mapGetters(['userId', 'avatar', 'name'])
  },
  beforeDestroy() {
    if (this.socket) {
      this.socket.close()
    }
    if (this.wsCloseTimer) {
      this.cleanWSCloseTimer()
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: 80vh;
  width: 100%;
  background-color: rgb(50, 54, 68);
  border-radius: 5px;
  display: grid;
  grid-template-rows: 1fr 40px;
  padding: 35px 20px 20px;
}

.icon-close:hover {
  color: red;
}

.logo {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.message {
  margin-bottom: 10px;
}

.logo {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.content {
  padding: 8px;
  border-radius: 5px;
}

.chat-content {
  width: 100%;
  height: calc(100% - 30px);
  overflow-y: scroll;
  padding: 20px;
  box-sizing: border-box;
  font-family: element-icons, serif;
  background-color: rgba(0, 0, 0, .3);

  &::-webkit-scrollbar {
    width: 0;
    /* Safari,Chrome 隐藏滚动条 */
    height: 0;
    /* Safari,Chrome 隐藏滚动条 */
    display: none;
    /* 移动端、pad 上Safari，Chrome，隐藏滚动条 */
  }

  .chat-wrapper {
    position: relative;
    word-break: break-all;

    .chat-friend {
      width: 100%;
      float: left;
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      align-items: flex-start;

      .chat-text {
        max-width: 90%;
        padding: 20px;
        border-radius: 5px 20px 20px 20px;
        background-color: rgb(56, 60, 75);
        color: #fff;
        box-shadow: #ccc 2px 2px 5px;

        &:hover {
          background-color: rgb(39, 42, 55);
        }
      }

      .chat-img {
        img {
          width: 100px;
          height: 100px;
        }
      }

      .info-time {
        margin: 10px 0;
        color: #fff;
        font-size: 14px;

        img {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          vertical-align: middle;
          margin-right: 10px;
        }

        span:last-child {
          color: rgb(101, 104, 115);
          margin-left: 10px;
          vertical-align: middle;
        }
      }
    }

    .chat-me {
      width: 100%;
      float: right;
      margin-bottom: 20px;
      position: relative;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      align-items: flex-end;

      .chat-text {
        box-shadow: #ccc 2px 2px 5px;
        float: right;
        max-width: 90%;
        padding: 20px;
        border-radius: 20px 5px 20px 20px;
        background-color: white;
        color: #000000;
      }

      .chat-img {
        img {
          max-width: 300px;
          max-height: 200px;
          border-radius: 10px;
        }
      }

      .info-time {
        margin: 10px 0;
        color: #fff;
        font-size: 14px;
        display: flex;
        justify-content: flex-end;

        img {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          vertical-align: middle;
          margin-left: 10px;
        }

        span {
          line-height: 30px;
        }

        span:first-child {
          color: rgb(101, 104, 115);
          margin-right: 10px;
          vertical-align: middle;
        }
      }
    }
  }
}

:deep(.el-input__inner) {
  font-family: element-icons, serif;
}
</style>
