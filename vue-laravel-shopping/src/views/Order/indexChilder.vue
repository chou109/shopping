<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import NavFooter from '@/views/NavFooter.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
const route = useRoute()
const Orderchild=ref([])
const dig=ref(false)

onMounted(()=>{
    window.scrollTo(0, 0);
    Orderchild.value=JSON.parse(route.query.number)
    if (Orderchild.value.status_desc === "未付款") {
        dig.value = true
    }
})

</script>

<template>

    <div>
        <div class="order-header" style="padding-bottom: 70px;">
            <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
            <div class="container clearfix">

                <div class="title">
                    <p style="font-size: 30px;">订单详情 <span>请谨防钓鱼链接或诈骗电话，了解更多></span></p>

                </div>
                <div class="username">
                    <a href="javascript:;"> username </a>

                </div>
            </div>
        </div>
        <div style="margin-left: 260px;font-size: 18px; ">
            <br>
            <div style="line-height: 30px;">
                <p><strong>订单号:</strong><span style="font-size: 15px;">{{ Orderchild.order_no }}</span></p>
                <p><strong>订单总金额:</strong><span
                        style="font-size: 15px;">{{ Orderchild.quantity * Orderchild.current_unit_price }}</span></p>
                <p><strong>订单状态:</strong><span style="font-size: 15px;"> {{ Orderchild.status_desc }}</span></p>
                <!-- <button v-show="this.dig" style="width: 80px;height: 30px;">付款 </button> -->
            </div>

            <hr>

            <div style="margin-top: 20px;">
                <div class="sub">
                    <img :src="`src/assets/images` + Orderchild.product_main_image" alt="">
                    <div style="margin-top: 20px;line-height: 28px;">
                        <p>商品名称:<span>{{ Orderchild.product_name }}</span></p>
                        <p>商品数:<span>{{ Orderchild.quantity }}</span></p>
                        <p>商品单价:<span>{{ Orderchild.current_unit_price }}</span></p>
                        <p>订单支付金额:<span>{{ Orderchild.quantity * Orderchild.current_unit_price }}</span></p>
                        <p>订单创建时间:<span>{{ Orderchild.create_time }}</span></p>
                        <p>订单状态:<span>{{ Orderchild.status_desc }}</span></p>
                    </div>
                </div>
                <br>
                <hr>
            </div>


        </div>
        <NavServiceBar></NavServiceBar>
        <NavFooter></NavFooter>
    </div>
</template>

<style scoped>
.sub {
    display: flex;
    justify-content: flex-start;

    img {
        height: 200px;
        width: 200px;
    }
}


.order-header {
    padding: 30px 0;
    border-bottom: 2px solid #FF6600;

    .header-logo {
        float: left;
    }

    .title,
    .username {
        display: inline-block;
        height: 55px;
        line-height: 55px;
    }

    .title {
        float: left;
        margin-left: 54px;

        span {
            font-size: 14px;
            margin-left: 17px;
            color: #999999;
            font-weight: 200;
        }
    }

    .username {
        float: right;

        a {
            color: #666666;
            font-size: 16px;
        }
    }
}
</style>