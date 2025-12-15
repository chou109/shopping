<script>
import ImageUpload from '@/components/ImageUpload/index.vue'

export default {
	name: 'CountDown',
	components: {
		ImageUpload
	},
	props: {
		height: {
			type: Number,
			default: 200
		},
		width: {
			type: Number,
			default: 420
		}
	},
	data() {
		return {
			lastX: 0,
			lastY: 0,

			drawFlag: false,

			context: undefined,
		}
	},
	mounted() {
		this.init()
	},
	methods: {
		init() {
			const canvas = this.$refs.canvas
			if (canvas) {
				this.context = canvas.getContext('2d')
			}
		},

		dragLine(endX, endY) {
			this.context.beginPath();
			this.context.moveTo(this.lastX, this.lastY);
			this.context.lineTo(endX, endY);
			this.context.stroke();
		},

		handleMouseLeave(e) {
			this.drawFlag = false
		},
		handleMouseDown(e) {
			this.drawFlag = true
			const {offsetX, offsetY} = e
			this.lastX = offsetX
			this.lastY = offsetY
		},
		handleMouseMove(e) {
			if (this.drawFlag) {
				const {offsetX, offsetY} = e
				this.dragLine(offsetX, offsetY)
				this.lastX = offsetX
				this.lastY = offsetY
			}
		},
		handleMouseUp(e) {
			if (this.drawFlag) {
				this.dragLine(e.offsetX, e.offsetY)
				this.drawFlag = false
			}
		},

		handleClear() {
			this.context.clearRect(0, 0, this.width, this.height);
		},

		handleConfirm() {
			this.$emit('confirm', {canvas: this.$refs.canvas, context: this.context})
		}
	}
}
</script>

<template>
	<div :style="{height: height + 'px', width: width + 'px'}">
		<canvas ref="canvas" :height="height" :width="width" style="border: 1px solid"
				@mousedown="handleMouseDown"
				@mousemove="handleMouseMove"
				@mouseup="handleMouseUp"
				@mouseleave="handleMouseLeave"></canvas>
		<span @click="handleClear" style="padding-right: 2em">重签</span>
		<span @click="handleConfirm">确定</span>
	</div>
</template>

