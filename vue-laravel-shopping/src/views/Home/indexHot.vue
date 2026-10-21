<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { useRouter } from 'vue-router';
const router = useRouter()

const form = ref([])
const load = function () {
    console.log(666)
    api({ url: "/product/hot", method: "get" }).then((res) => {
        form.value = res.data.data
        console.log(666)
        console.log(res.data)
    })
}
const cart = ref('src/assets/icon-cart-hover.png')
// const jumps=function(){
//     // router.push('/details')
//     console.log("dianjil ")
// }
onMounted(() => {
    load()

})
</script>

<template>

    <div>
        <!-- slice(0,2) -->
        <div style="display: flex; justify-content: space-between;">
            <span style="font-size: 30px;"><strong>热门商品</strong></span>
            <a href="javascript:;" style="font-size: 30px; color: black;"
                @click="$router.push('/whole')"><strong>商品列表></strong></a>
        </div>
        <div style="margin: 10px 0; ">
            <el-row :gutter="10">
                <el-col :span="6" v-for="item in form.slice(0, 8)" :key="item.id" style="margin-top: 10px;">
                    <el-card>
                        <RouterLink :to="{ name: 'details', query: { id: item.id } }">
                            <div>
                                <!-- @click="jump([item.id, item.name])" -->
                                <img :src="`src/assets/images` + item.main_image" alt=""
                                    style="width: 100%;height: 100%;">
                                <div style="text-align: center;">
                                    <div style="height: 45px;">
                                        <p style="font-size: 15px;"><strong>{{ item.name }}</strong></p>
                                    </div>
                                    <p style="color: red;margin-bottom: 15px;"><strong>{{ item.price }} </strong>
                                        <img :src="cart" style="height: 16px;margin-bottom: -4px;">
                                        <span class="icon-cart-hover" style="margin-bottom: -5px;"></span>
                                    </p>
                                    <hr>
                                    <p style="color:#b2b3b5; font-size: 10px;">{{ item.sub_title }}</p>
                                </div>
                            </div>
                        </RouterLink>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>

</template>

<style scoped>
hr {
    border-color: #d7d7d7;
}

.el-col {
    &:hover {
        transform: translate3d(0, -3px, 0);

    }
}
</style>