<template>
	<div v-if="countDownText">
		<span :style="{ color: textColor }">{{ countDownText }}</span>
	</div>
</template>

<script>
export default {
	name: 'CountDown',
	props: ['targetTime'],
	data() {
		return {
			time: undefined,
			interval: -1,
			countDownText: undefined,
			textColor: 'red'
		}
	},
	created() {
		// 开始倒计时
		this.startCountdown()
	},
	beforeDestroy() {
		clearInterval(this.interval)
	},
	methods: {
		startCountdown() {
			this.time = this.targetTime

			this.interval = setInterval(() => {
				let d = parseInt(this.time / 60 / 60 / 24);
				let h = parseInt(this.time / 60 / 60 % 24);
				let m = parseInt(this.time / 60 % 60);
				let s = parseInt(this.time % 60);
				this.countDownText = d + '天' + h + '时' + m + '分' + s + '秒';

				if (this.time > 0) {
					this.time--
					this.textColor = 'black'
				} else {
					this.textColor = 'red'
					clearInterval(this.interval)
				}

			}, 1000)
		}
	}
}
</script>

<style scoped>
span {
	font-size: 24px;
}
</style>
