<script setup>
import { ref, onMounted } from 'vue';
const qiday = ref('src/assets/icon-7day.png')
const swday = ref('src/assets/icon-15day.png')
const setting = ref('src/assets/icon-setting.png')
const post = ref('src/assets/icon-post.png')
</script>

<template>
    <div class="service" style="width: 1600px;margin: 0 auto;">
        <div class="container">
            <ul>
                <li><img :src="setting" alt="">预约维修服务</li>
                <li><img :src="qiday" alt="">7天无理由退货</li>
                <li><img :src="swday" alt="">15天免费换货</li>
                <li><img :src="post" alt="">满150元包邮</li>
            </ul>
        </div>
    </div>
</template>

<style scoped>
.service {
    padding: 33px 0;
    color: #666666;
    font-size: 16px;
    padding-left: 200px;

    li {
        display: inline-block;
        width: 20.9%;
        text-align: center;
        border-right: 1px solid #E5E5E5;

        img {
            display: inline-block;
            width: 20px;
            height: 20px;
            vertical-align: middle;
            margin-right: 8px;
            margin-top: -5px;
        }
    }

}
</style>