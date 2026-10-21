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

		handlePointerLeave(e) {
			this.drawFlag = false
		},
		getCanvasPoint(e) {
			const rect = this.$refs.canvas.getBoundingClientRect()
			return {
				x: (e.clientX - rect.left) * (this.width / rect.width),
				y: (e.clientY - rect.top) * (this.height / rect.height)
			}
		},
		handlePointerDown(e) {
			this.drawFlag = true
			const point = this.getCanvasPoint(e)
			this.lastX = point.x
			this.lastY = point.y
			e.currentTarget.setPointerCapture(e.pointerId)
		},
		handlePointerMove(e) {
			if (this.drawFlag) {
				const point = this.getCanvasPoint(e)
				this.dragLine(point.x, point.y)
				this.lastX = point.x
				this.lastY = point.y
			}
		},
		handlePointerUp(e) {
			if (this.drawFlag) {
				const point = this.getCanvasPoint(e)
				this.dragLine(point.x, point.y)
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
		<canvas ref="canvas" :height="height" :width="width" class="signature-canvas"
				aria-label="签名区域，请使用鼠标、触控或触控笔签名"
				@pointerdown="handlePointerDown"
				@pointermove="handlePointerMove"
				@pointerup="handlePointerUp"
				@pointercancel="handlePointerLeave"
				@pointerleave="handlePointerLeave"></canvas>
		<button type="button" class="signature-action" @click="handleClear">重签</button>
		<button type="button" class="signature-action" @click="handleConfirm">确定</button>
	</div>
</template>

<style scoped>
.signature-canvas {
	border: 1px solid;
	touch-action: none;
}

.signature-action {
	margin-right: 2em;
	padding: 4px 8px;
	border: 0;
	background: transparent;
	color: inherit;
	font: inherit;
	cursor: pointer;
}

.signature-action:focus-visible {
	outline: 2px solid #409EFF;
	outline-offset: 2px;
}
</style>

