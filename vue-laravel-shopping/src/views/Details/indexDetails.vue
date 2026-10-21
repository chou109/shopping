<script setup>
import { ref, onMounted, onUnmounted, compile } from 'vue';
import api from "@/axios/request.js"; //引入api
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'
const router = useRouter()
const route = useRoute()
const form = ref([])
const id = ref()
const subImg = ([])
const userId = ref()
const chang = ref()
const user = ref([])
const username = ref([])
const load = function () {
    user.value = JSON.parse(localStorage.getItem("user"))
    console.log(user.value)
    if (user.value) {
        userId.value = user.value.id
        username.value = user.value.userName
    }


    api({ url: '/product/' + id.value, method: 'get' }).then((res) => {
        console.log(res)
        form.value = JSON.parse(JSON.stringify(res.data.data))
        console.log(form.value)
        subImg.value = form.value[0].sub_images.split(",")
        // console.log(subImg.value)
        chang.value = subImg.value.length
        // console.log(chang.value)
    })
}
const payment = ref([])
const mai = function () {
    if (!user.value) {
        router.push({ path: '/' })
        ElMessage.error('请先登录')
        return
    }
    goods.value.product_id = form.value[0].product_id
    goods.value.user_id = userId.value
    goods.value.product_main_image = form.value[0].main_image
    goods.value.product_unit_price = form.value[0].price
    goods.value.product_name = form.value[0].name
    goods.value.product_subtitle = form.value[0].sub_title
    goods.value.quantity = 1
    goods.value.product_total_price = form.value[0].price
    payment.value.push(goods.value)
    router.push({
        path: '/set',
        // query: {product: this.multipleSelection }
        query: { product: JSON.stringify(payment.value) }
    })
}
const goods = ref({})
const shopping = function () {
    if (!user.value) {
        router.push({ path: '/' })
        ElMessage.error('请先登录')
    } else {
        goods.value.product_id = form.value[0].product_id
        goods.value.user_id = userId.value
        goods.value.product_main_image = form.value[0].main_image
        goods.value.product_unit_price = form.value[0].price
        goods.value.product_name = form.value[0].name
        goods.value.product_subtitle = form.value[0].sub_title
        goods.value.create_user = userId.value
        goods.value.update_user = userId.value
        goods.value.quantity = 1
        goods.value.product_total_price = form.value[0].price
        console.log(goods.value)

        api.post("/cart/add", goods.value).then((res) => {
            console.log(res)
            ElMessage({
                message: '添加成功',
                type: 'success',
            })
            // console.log(res)
        })
    }
}

const show = ref(false)
const dataLoad = ref(false)
onMounted(() => {
    // console.log(route.query.id)
    id.value = route.query.id
    load()

    window.addEventListener("scroll", showSearch)
    window.scrollTo(0, 0);
    dataLoad.value = true
})

function showSearch() {
    // console.log(window.scrollY)
    show.value = window.scrollY > 180
}
</script>

<template>

    <div style="width: 1800px;margin: 0 auto;margin-top: 50px;" v-if="dataLoad">
        <div class="home-header-fixed" v-show="show">
            <div class="xtx-goods-page" style="width: 1100px;margin: 0 auto;height: 10px;">
                <div
                    style="display: flex; justify-content: space-between;line-height: 15px;margin-top: 20px;margin-bottom: 20px;">
                    <p style=" font-size: 15px; ">{{ form[0].name }}</p>
                    <p style="color:#999 ;">概述 | 参数 | 用户评价</p>
                </div>
            </div>
        </div>
        <div class="xtx-goods-page" style="width: 1100px;margin: 0 auto;">
            <hr width="1800px" style="margin-left: -350px;">
            <!-- 商品信息 -->
            <div
                style="display: flex; justify-content: space-between;line-height: 15px;margin-top: 20px;margin-bottom: 20px;">
                <p style=" font-size: 15px; "><strong> {{ form[0].name }} </strong></p>
                <p style="color:#999 ;">概述 | 参数 | 用户评价</p>
            </div>
            <div class="info-container">
                <div class="goods-info">
                    <!-- 图片预览区 -->
                    <div style=" margin-right: 70px; height: 450px;width: 450px;">
                        <el-carousel style="height: 450px;width: 450px;">
                            <el-carousel-item v-for="(item, index) in chang" :key="index"
                                style="height: 450px;width: 450px;">
                                <img :src="`/src/assets/images` + subImg.value[index]" alt=""
                                    style="width:450px; height:450px ; ">
                            </el-carousel-item>
                        </el-carousel>
                    </div>
                    <div>
                        <!-- 商品信息区 -->
                        <p style="font-size: 22px;"><strong> {{ form[0].name }} </strong> </p>
                        <p style="color: #999;margin-top: 10px;"> {{ form[0].sub_title }} </p>
                        <p style="margin-top: 10px;font-size: 15px; color: #ff6501;margin-top: 30px;">
                            <span>店铺自营</span><br>
                            <span> {{ form[0].price }} 元</span>
                        </p>
                        <hr style="margin-bottom: 20px; margin-top: 10px;border-color: #f0f0f0;">
                        <div class="g-service">
                            <dl style="padding-top: 10px;color: #ff6501; font-size: 17px;">
                                <dd>库存剩余：{{ form[0].stock }} </dd>
                            </dl>
                        </div>
                        <div class="g-service" style="margin-top: 30px;margin-bottom: 20px;">
                            <dl>
                                <dd> {{ form[0].name }} </dd>
                            </dl>
                            <dl>
                                <dd style="color: #ff6501;font-size: 20px;margin-top: -10px;">
                                    总价：{{ form[0].price }}
                                </dd>
                            </dl>
                        </div>
                        <!-- 按钮组件 -->
                        <div>
                            <el-button style="margin-right: 30px;background-color: #ff6600;color: white;"
                                @click="shopping" ref="btn">
                                加入购物车
                            </el-button>
                            <el-button style="border-color: #ff6600;color:#ff6600 ;background-color: white;"
                                @click="mai">
                                立即购买
                            </el-button>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div style="margin-top: 70px;">
            <div style="height: 300px;width: 100%;background-color: #f3f3f3;">
                <p style="margin-left: 200px; font-size: 23px;padding-top: 30px;"><strong>价格说明</strong></p>
                <div style="width:1400px;height: 170px;background-color: white;margin-left: 200px;margin-top: 28px;">
                    <div
                        style="padding-top:40px;font-size: 16px;margin-left: 100px;margin-right: 100px;line-height: 30px;">
                        <strong>划线价:</strong><span
                            style="color:#a7a7a7 ;">商品展示的划横线价格为参考价，该价格可能是品牌专柜标价、商品吊牌价或由品牌供应商提供的正品零售价(如厂商指导价、建议零售价
                            等)或该商品在小米商城/小米有品/天猫小米官方旗舰店曾经展示过的销售价;由于地区、时间的差异性和市场行情波动，品牌专柜标价、商品吊牌价
                            等可能会与您购物时展示的不一致，该价格仅供您参考。</span>
                    </div>
                </div>
            </div>
        </div>

    </div>

</template>

<style>
.plus {
    height: 100px;
    width: 100px;

    /* background-color: aqua; */
    img {
        height: 100px;
        width: 100px;
    }
}

.el-carousel__container {
    height: 450px;
}

.responsive-image {
    width: 100%;
    position: absolute;
    left: 0;
    top: 0;
    /* 或者指定具体的宽度值 */
    /* height: 100%; */
    /* 高度自动，保持图片比例 */
    /* 或者使用max-width，这样图片可以响应式缩放而不超过其原始大小 */
    /* max-width: 100%; */
    /* display: block; */
    /* 防止图片下方出现空白 */
}

.home-header-fixed {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background-color: #fff;
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.2);
    padding: 20px 0;
}




button {
    width: 240px;
}

.xtx-goods-page {
    .goods-info {
        min-height: 600px;
        background: #fff;
        display: flex;


    }

    .alignleft {
        display: inline;
        float: left;
    }

    .g-service {
        background: #f5f5f5;
        width: 500px;
        height: 90px;
        padding: 15px 10px 0 10px;
        margin-top: 10px;

        dl {
            padding-bottom: 20px;
        }
    }
}
</style>