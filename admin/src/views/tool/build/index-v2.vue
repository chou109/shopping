<script setup lang="ts">
import { ref } from "vue";
import dragComponent from '@/views/tool/build/components/drag-component.vue'
import dragContainer from "./components/drag-container.vue";
import useDragStore from './components/dragStore';
const dragStore = useDragStore();
/* 
dragstart
要被拖拽的元素开始拖拽时触发，这个事件对象是被拖拽元素
drag - 元素正在拖动时触发
dragend - 用户完成元素拖动后触发
dragenter - 拖拽元素进入目标元素时触发，这个事件对象是目标元素
dragover - 拖拽某元素在目标元素上移动时触发，这个事件对象是目标元素
dragleave - 拖拽某元素离开目标元素时触发，这个事件对象是目标元素
drop - 事件在可拖动元素或选取的文本放置在目标区域时触发。
触发顺序：
dragstart –> dragenter –> dragover –> drop –> dragend
*/
const dragingElement = ref<HTMLDivElement>()
function dragstart(e: DragEvent) {
  // 要被拖拽的元素开始拖拽时触发，这个事件对象是被拖拽元素
  const element = e.target as HTMLDivElement // 拖拽的元素
  // console.log("dragstart", element);
  dragingElement.value = element
}
function dragover(e: DragEvent) {
  // 阻止默认行为 - 使所有元素都可以接收
  e.preventDefault()
  // 拖拽过程 - 一直触发
  // const element = e.target // 在哪个元素上面
  // console.log("dragover", element);
}
function dragenter(e: DragEvent) {
  // 拖拽过程 - 触发一次
  const element = e.target // 在哪个元素上面
  // console.log("dragenter", element);
}

function drop(e: DragEvent) {
  // 拖拽结束
  const element = e.target as HTMLDivElement // 在那个元素上面松开拖拽
  // console.log("drop", element);
  console.log(dragStore.startDrag);
  console.log(dragStore.getDragingElement);
  
  
  element.appendChild(dragStore.getDragingElement())

}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>Header</el-header>
      <el-container>
        <div>

        </div>
        <el-aside width="200px">
          <drag-component>
            <el-input></el-input>
          </drag-component>
        </el-aside>
        <el-main>
          <drag-container></drag-container>
        </el-main>
        <el-aside width="200px">
        </el-aside>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="scss"></style>