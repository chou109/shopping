<template>
  <div>
    <!--  头像 -->
    <div v-if="!chatOpen" class="avatar" :style="{
            ...buttonPosition
          }"
         @mousedown="handleMouseDown"
         ref="dragButton"
    >
      <img :style="{
        height: buttonHeight + 'px',
        width: buttonWidth + 'px',
        userSelect: 'none',
        pointerEvents: 'none',
        }"
           src="@/assets/images/pic5.png" alt=""
      />
    </div>

    <!--  聊天窗  -->
    <el-dialog v-model="chatOpen" :width="documentWidth > 600 ? '600px' : '100%' " class="dragButtonDialog">
      <Chat @close="minimize" @minimize="minimize"></Chat>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Chat from '@/components/Chat/index.vue'

export default {
  name: 'DragButton',
  components: {
    Chat
  },
  props: {
    // 通过position来设置初始定位
    position: {
      type: Object,
      default: () => {
        return {
          top: 'auto',
          left: 'auto',
          button: '20vh',
          right: '0'
        }
      }
    }
  },
  data() {
    return {
      chatOpen: false,     //  菜单展开状态

      mouseDownState: false,   //  鼠标点击状态

      buttonHeight: 50,
      buttonWidth: 50,
      buttonPosition: {
        top: 'auto',
        left: 'auto',
        button: '20vh',
        right: '0'
      },
      currentMouseX: 0,
      currentMouseY: 0,

      documentWidth: document.documentElement.clientWidth
    }
  },
  computed: {
    ...mapGetters(['userId', 'avatar', 'name'])
  },
  methods: {
    // 最小化
    minimize() {
      this.chatOpen = false
    },

    //  鼠标按下
    handleMouseDown(event) {
      /* 此处判断  pc 或 移动端 得到 event 事件 */
      let touch
      if (event.touches) {
        touch = event.touches[0]
      } else {
        touch = event
      }
      //得到鼠标的位置
      let { clientX, clientY } = touch

      // 保存当前位置
      this.currentMouseX = clientX
      this.currentMouseY = clientY

      this.mouseDownState = true
      //如果用户没有登录的 需要跳转到登录页面
      setTimeout(() => {
        if (!this.mouseDownState) {
          console.log(this.userId)

          if (this.userId !== '') {
            this.chatOpen = true
          } else {
            this.$router.push('/login')
          }
          this.chatOpen = true
        }
      }, 100)

      document.documentElement.addEventListener('mousemove', this.handleMouseMove)
      document.documentElement.addEventListener('mouseup', this.handleMouseUp)
    },

    //  鼠标拖拽
    handleMouseMove(event) {
      // 如果没有鼠标点击状态  则不执行
      if (!this.mouseDownState) return
      // 阻止默认事件
      event.preventDefault()

      let touch
      if (event.touches) {
        touch = event.touches[0]
      } else {
        touch = event
      }
      let { clientX, clientY } = touch

      // 当前位置增加偏移量
      this.buttonPosition.left = `${this.$refs.dragButton.offsetLeft + (clientX - this.currentMouseX)}px`
      this.buttonPosition.top = `${this.$refs.dragButton.offsetTop + (clientY - this.currentMouseY)}px`
      this.buttonPosition.bottom = 'auto'
      this.buttonPosition.right = 'auto'

      this.currentMouseX = clientX
      this.currentMouseY = clientY
    },

    //    鼠标抬起
    handleMouseUp(event) {

      if (!this.$refs.dragButton) return

// 如果没有鼠标点击状态  则不执行
      if (!this.mouseDownState) return

      let { clientWidth: pageWidth } = document.documentElement
      let { clientWidth: dragButtonWidth } = this.$refs.dragButton

      if (this.currentMouseX <= ((pageWidth - dragButtonWidth) / 2)) {
        this.buttonPosition.left = '0px'
        this.buttonPosition.right = 'auto'
      } else {
        this.buttonPosition.right = '0px'
        this.buttonPosition.left = 'auto'
      }

      this.mouseDownState = false

      document.documentElement.removeEventListener('mousemove', this.handleMouseMove)
      document.documentElement.removeEventListener('mouseup', this.handleMouseUp)
    },

    setDocumentWidth() {
      this.documentWidth = document.documentElement.offsetWidth
    },
  },
  mounted() {
    window.addEventListener('resize', this.setDocumentWidth)

    this.buttonPosition = { ...this.buttonPosition, ...this.position }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.setDocumentWidth)
  }

}
</script>

<style scoped lang="scss">
.avatar {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9999;

  border-radius: 50%;
}


::v-deep .dragButtonDialog {
  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    margin: 0;
  }
}
</style>
