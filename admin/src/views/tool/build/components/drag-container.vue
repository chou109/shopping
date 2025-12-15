<script setup lang="ts">
const props = defineProps({
    destroy: {
        type: Boolean,
        default: false
    },
    copy: {
        type: Boolean,
        default: false
    },
    draw: {
        type: Boolean,
        default: false
    }
})
import useDragStore from './dragStore';
const dragStore = useDragStore();
const dragover = (e: DragEvent) => {
    // 阻止默认行为 - 使所有元素都可以接收
    e.preventDefault()
    // 拖拽过程 - 一直触发
    // const element = e.target // 在哪个元素上面
    // console.log("dragover", element);


}
function drop(e: DragEvent) {
    // 拖拽结束
    const element = e.target as HTMLDivElement // 在那个元素上面松开拖拽
    
    if (props.destroy) {
        dragStore.remove()
    } else {
        if (props.copy) {
            const item = dragStore.moveFromCopy(element)
            item.ondragstart = e => dragStore.startDrag(item)
        } else {
            const item = dragStore.move(element)
            item.ondragstart = e => dragStore.startDrag(item)
            if (props.draw) {
                item.style.position = 'absolute'
                item.style.top = e.offsetY + 'px'
                item.style.left = e.offsetX + 'px'
            }
        }
    }
}
</script>
<template>
    <div @drop="drop" @dragover="dragover" style="width: 100%;height: 100%;" :class="{ draw }">
        <slot></slot>
    </div>
</template>
<style lang="scss" scoped>
.draw {
    position: relative;
}
</style>