<script setup>
import { ref } from 'vue';
const imgs = ref(['src/assets/banner1.jpg', 'src/assets/banner2.jpg'])
</script>

<template>
    <div style="width: 1000px;margin: 0 auto; margin-bottom: 60px;">
        <div class="home-banner">
            <el-carousel arrow="never" height="417px">
                <el-carousel-item v-for="item in imgs" :key="item">
                    <img :src="item" alt="">
                </el-carousel-item>
            </el-carousel>
        </div>
    </div>
</template>

<style scoped>
.home-banner {
    width: 1000px;
    height: 417px;
    /* position: absolute; */
    left: 0;
    top: 0;
    z-index: 98;

    img {
        /* width: 100%; */
        height: 417px;
    }
}
</style>