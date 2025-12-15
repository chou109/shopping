<script>
export default {
	name: 'SliderCaptcha',
	data() {
		return {
			targetLeft: 0,
			currentLeft: 0,
			width: 500,
			initOffsetX: 100,
			size: 30,
			baseHeight: 200,
			top: 85,
		}
	},
	mounted() {
		this.init()
	},
	methods: {
		init() {
			this.currentLeft = 0
			this.targetLeft = this.initOffsetX + (this.width - this.initOffsetX) * Math.random()
		},

		judge() {
			if (Math.abs(this.currentLeft - this.targetLeft) < 3) {
				this.$emit('pass')
			} else {
				this.init()
			}
		}
	}
}
</script>

<template>
	<div :style="{width: width + 'px'}" style="position: relative;">
		<img src="../../assets/images/banner1.png" alt="yz" :style="`width: ${width}px; height: ${baseHeight}px;`"/>
		<div :style="`position: absolute; top: ${top}px; left: ${targetLeft}px; height: ${size}px; width: ${size}px;`" class="target"></div>
		<div :style="`height: ${size}px; width: ${size}px; position: absolute; top: ${top}px; left: ${currentLeft}px; overflow: hidden; z-index: 9999;`">
			<img src="../../assets/images/banner1.png" alt="yz" :style="`width: ${width}px; height: ${baseHeight}px; position:relative; left: -${targetLeft}px; top: -${top}px;`"/>
		</div>
		<el-slider v-model="currentLeft" :max="width - size" @change="judge"></el-slider>
	</div>
</template>

<style scoped>
.target {
	background-color: rgba(0,0,0,.3);
}
</style>
