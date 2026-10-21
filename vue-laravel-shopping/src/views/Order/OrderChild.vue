<script setup>
import { ref, onMounted } from 'vue';
import NavFooter from '@/views/NavFooter.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
import { useRouter } from 'vue-router';
const router = useRouter()
import { useRoute } from 'vue-router';
const route = useRoute()

const Orderchild = ref([])
const num = ref(0)
const dig = ref(false)
const order_no = ref(0)
const status_desc = ref()
const payment=ref([])
const a=ref({})
const fuk = function () {
    payment.value = []
    for (var i = 0; i < Orderchild.value.length; i++) {
        a.value = {}
        a.value.product_id = Orderchild.value[i].product_id
        a.value.product_main_image = Orderchild.value[i].product_main_image
        a.value.product_name = Orderchild.value[i].product_name
        a.value.quantity = Orderchild.value[i].quantity
        a.value.product_total_price = Orderchild.value[i].total_price
        a.value.product_unit_price = Orderchild.value[i].current_unit_price
        a.value.user_id = Orderchild.value[i].user_id
        payment.value.push(this.a)
    }
    // console.log(this.payment)

    // console.log(Orderchild.value)
    router.push({
        path: '/set',
        query: { product: JSON.stringify(payment.value) }
    })
}
const dian = function (index) {
    if (Orderchild.value.length > 1) {
        router.push({
            path: '/childer',
            query: { number: JSON.stringify(Orderchild.value[index]) }
        })
    }

}
const user = ref([])
const userName = ref()
onMounted(() => {
    window.scrollTo(0, 0);
    user.value = JSON.parse(localStorage.getItem("user"))

    userName.value = user.value.user_name
    Orderchild.value = JSON.parse(route.query.orderchild)
    num.value = JSON.parse(route.query.num)
    order_no.value = Orderchild.value[0].order_no
    status_desc.value = Orderchild.value[0].status_desc
    if (status_desc.value === "未付款") {
        dig.value = true
    }
    // console.log(Orderchild.value)
    // console.log(num.value)
    // console.log(Orderchild.value[0].order_no)

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
                    <a href="javascript:;">{{ userName }} </a>
                </div>
            </div>
        </div>
        <div style="margin-left: 260px;font-size: 18px;">
            <br>
            <div style="line-height: 30px;margin-bottom: 10px;">
                <p><strong>订单号:</strong><span style="font-size: 15px;">{{ order_no }}</span></p>
                <p><strong>订单总金额:</strong><span style="font-size: 15px;">{{ num }}</span></p>
                <p><strong>订单状态:</strong><span style="font-size: 15px;"> {{ status_desc }}</span></p>
                <button v-show="dig" style="width: 80px;height: 30px;" @click="fuk()">付款 </button>
            </div>
            <hr>
            <div v-for="(item, index) in Orderchild" :key="item.id" style="margin-top: 20px;" @click="dian(index)">
                <div class="sub" style="display: flex;justify-content: flex-start;">
                    <img :src="`src/assets/images` + item.product_main_image" alt="" style="height: 200px;width: 200px;">
                    <div style="margin-top: 20px;line-height: 28px;">
                        <p>商品名称:<span>{{ item.product_name }}</span></p>
                        <p>商品数:<span>{{ item.quantity }}</span></p>
                        <p>商品单价:<span>{{ item.current_unit_price }}</span></p>
                        <p>订单支付金额:<span>{{ num }}</span></p>
                        <p>订单创建时间:<span>{{ item.create_time }}</span></p>
                        <p>订单状态:<span>{{ item.status_desc }}</span></p>
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
.el-pagination__editor.el-input .el-input__inner {
    height: 28px;
    color: white;
    background-color: #409efe;
}

.el-pagination .el-select .el-input .el-input__inner {
    background-color: #f5f5f5;
}

.order-header {
    padding: 30px 0;
    border-bottom: 2px solid #FF6600;
    background-color: white;

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

.order-container {
    padding: 10px 20px;
    width: 1500px;
    margin-left: 140px;

    .pagination-container {
        display: flex;
        justify-content: center;
    }

    .main-container {
        min-height: 500px;
    }
}

.order-item {
    margin-bottom: 20px;
    border: 1px solid #f5f5f5;
    background-color: white;

    .head {
        height: 50px;
        line-height: 50px;
        background: #fffaf7;
        padding: 0 20px;
        overflow: hidden;

        span {
            margin-right: 20px;
        }
    }

    .body {


        .column {
            border-left: 1px solid #f5f5f5;
            text-align: center;
            padding: 20px;

            &.goods {
                flex: 1;
                padding: 0;

                ul {
                    li {
                        padding: 10px;
                        display: flex;

                        .image {
                            width: 70px;
                            height: 70px;

                        }

                        .info {
                            width: 220px;
                            text-align: left;
                            padding: 0 10px;

                            p {
                                margin-bottom: 5px;

                                &.name {
                                    height: 38px;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
</style>