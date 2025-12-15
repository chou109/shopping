<script setup lang="ts">
import useDragStore from './dragStore';
const props = defineProps({
    copy: {
        type: Boolean,
        default: false
    },
})
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
    触发顺序：dragstart –> dragenter –> dragover –> drop –> dragend
*/
function dragstart(e: DragEvent) {
    // 要被拖拽的元素开始拖拽时触发，这个事件对象是被拖拽元素
    const element = e.target as HTMLDivElement // 拖拽的元素
    if (props.copy)
        dragStore.startDragFromCopy(element)
    else
        dragStore.startDrag(element)
}
</script>
<template>
    <div @dragstart="dragstart" draggable="true">
        <slot></slot>
    </div>
</template>