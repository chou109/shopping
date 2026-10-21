<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { useRouter } from 'vue-router';
const router = useRouter()
const form = ref([])
const load = function () {
    api({ url: "/product/phone", method: "get" }).then((res) => {
        form.value = res.data.data
        // console.log(res.data.data)
    })
}
const jump = (id) => {
  router.push({
    path: '/details',
    query: {
        id
    }
  })
}

const cart = ref('src/assets/icon-cart-hover.png')
onMounted(() => {
    load()

})
</script>

<template>
    <div style="margin-top: 40px; margin-bottom: 100px;">
        <div style="display: flex; justify-content: space-between;">
            <span style="font-size: 30px;"><strong>手机数码 </strong></span>
        </div>
        <div style="margin: 10px 0; ">
            <el-row :gutter="10">
                <el-col :span="6" v-for="item in form.slice(0, 8)" :key="item.id" style="margin-top: 10px;">

                    <el-card>
                        <div @click="jump(item.id)">
                            <img :src="`src/assets/images` + item.main_image" alt=""
                                style="width: 100%;height: 100%;">
                            <div style="text-align: center;">
                                <div style="height: 40px;">
                                    <p style="font-size: 15px;"><strong>{{ item.name }}</strong></p>
                                </div>

                                <p style="color: red;margin-bottom: 15px;"><strong>{{ item.price }}</strong>
                                    <!-- <img src="../assets/icon-cart-hover.png" style="height: 16px;margin-bottom: -4px;"> -->
                                    <img :src="cart" style="height: 16px;margin-bottom: -4px;">
                                </p>

                                <hr>
                                <p style="color:#b2b3b5; font-size: 10px;">{{ item.sub_title }}</p>
                            </div>
                        </div>

                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>

</template>

<style scoped>
hr {
    border-color: #fff;
}

.el-col {
    &:hover {
        transform: translate3d(0, -3px, 0);

    }
}
</style>