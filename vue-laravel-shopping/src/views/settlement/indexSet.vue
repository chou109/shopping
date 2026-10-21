<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
import NavFooter from '@/views/NavFooter.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
import { useRouter } from 'vue-router';
const router = useRouter()
import { useRoute } from 'vue-router';
const route = useRoute()

const userId = ref()
const form = ref([])
const user=ref([])
const username=ref()
const load = function () {
    user.value=JSON.parse(localStorage.getItem("user"))
    // console.log(user.value)
    userId.value=user.value.id
    username.value=user.value.userName
    api({ url: '/address/' + userId.value }).then((res) => {
        form.value = res.data
        // console.log(res.data)
    })
}
const showDialog = ref(false)
const current_type = ref(0)
const check = function (index) {
    current_type.value = index
    console.log(index)
}
const del = function (id) {
    api({ url: '/address/' + id, method: "delete" }).then((res) => {
        load()
        ElMessage({
            message: '删除成功',
            type: 'success',
        })
    })
}
const handeEdit = function (row) {
    showDialog.value = true
    newAddress.value = JSON.parse(JSON.stringify(row))
    // console.log(row)
}
const onSubmit = function () {
    showDialog.value = false
    // console.log(newAddress.value)
    api.post('/address/' + userId.value, newAddress.value).then((res) => {
        // console.log("aaa")
        load()
    })

}
const add = function () {
    showDialog.value = true
    newAddress.value = {}
}
const newAddress = ref({

})
const ti=ref(0)
const goods=ref({})

const createOrder = function () {
    if (!form.value[current_type.value]) {
        ElMessage.error('请选择地址')
        return false
    }
    let time = Date.now();
    // console.log("time:"+time)
    ti.value = (time + "").substring(0, 10)
    // console.log("tiL:"+ti.value)
    let a = products.value.length;
    // console.log("a:"+a)
    for (let i = 0; i < a; i++) {
        goods.value = {}
        goods.value.activity_id = products.value[i].activity_id;
        goods.value.create_time =products.value[i].create_time;
        // goods.value.id = products.value[i].id;
        goods.value.product_id = products.value[i].product_id;
        goods.value.product_main_image = products.value[i].product_main_image;
        goods.value.product_name = products.value[i].product_name;
        goods.value.current_unit_price = products.value[i].product_unit_price;
        goods.value.total_price = products.value[i].product_total_price;
        goods.value.quantity = products.value[i].quantity;
        goods.value.order_no = ti.value
        goods.value.user_id = products.value[i].user_id;
        console.log(goods.value)
        api.post("/order/detail/"+userId.value,goods.value).then((res)=>{
            console.log(res)
            ElMessage({
            message: '创建成功',
            type: 'success',
        })
        })

        // this.axios.post("/api/order/detail/" + this.user.userInfo.id, this.goods,
        // ).then((res) => {
        //     this.$message.success("创建成功")
        // })
        // console.log(goods.value)
    }
    router.push({
        path:'/confirmorder',
        query: { order: JSON.stringify(form.value[current_type.value]),time:time,money:money.value }
        
    })


    // this.$router.push({
    //     name: 'confirmorder',
    //     // query: {product: this.multipleSelection }
    //     query: { order: JSON.stringify(this.form[this.current_type]), time: time, money: this.money }
    // })
}


const products = ref({})
const money = ref(0)
const number = ref(0)
onMounted(() => {
    window.scrollTo(0, 0);
    load()
    products.value = JSON.parse(route.query.product)
    console.log(products.value)
    // console.log(products.value)
    products.value.forEach(item => {
        money.value += parseInt(item.product_total_price)
        number.value += item.quantity
    });
})

</script>

<template>
    <div>
        <div class="order-header" style="padding-bottom: 70px;">
            <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
            <div class="container clearfix">
                <div class="title">
                    <p style="font-size: 30px;">订单确认 <span>请认真填写收货地址</span></p>
                </div>
                <div class="username">
                    <a href="javascript:;"> {{username}} </a>
                </div>
            </div>
        </div>
        <div style="width: 1800px;margin: 0 auto;background-color:#f5f5f5; padding-top:  40px;">
            <div style="background-color: white;width: 1400px;margin-left: 200px;margin-top: 20px;">
                <div class="xtx-pay-checkout-page">
                    <div class="container">
                        <div class="wrapper">
                            <!-- 收货地址 -->
                            <h3 class="box-title" style="margin-bottom: -30px;font-size: 30px">收货地址</h3>
                            <div style="margin: 10px 0; padding-bottom: 40px;">
                                <el-row>
                                    <el-col :span="6" v-for="(item, index) in form" :key="item.id"
                                        style="margin-top: 10px; height: 162px;width: 250px;display: flex;justify-content: space-around;">
                                        <div style="height: 160px;width: 250px; background-color:white;color: #8b8c82;"
                                            :span="8" @click="check(index)"
                                            :class="current_type === index ? 'typeActive' : 'type'">
                                            <div style="padding: 15px;">
                                                <p style="font-size: 20px;color: black;">{{ item.receive_name }}</p>
                                                <p style="margin-top: 10px;">{{ item.receive_phone }}</p>
                                                <span>{{ item.province }} {{ item.city }} {{ item.area }} {{ item.street
                                                    }}</span>
                                                <br>
                                                <div
                                                    style="display: flex; justify-content:space-between;margin-top: 20px;">
                                                    <el-icon @click="del(item.id)">
                                                        <Delete style="height: 18px; color: blue;"></Delete>
                                                    </el-icon>
                                                    <!-- <i class="Delete" style="color: blue;" @click="del(item.id)"></i> -->
                                                    <el-icon @click="handeEdit(item)">
                                                        <EditPen style="height: 18px; color: blue;" />
                                                    </el-icon>
                                                    <!-- <i class="el-icon-edit" style="color: blue;"  ></i> -->
                                                </div>
                                            </div>
                                        </div>
                                    </el-col>
                                    <el-col :span="6" style="margin-top: 10px; margin-left: 45px;">
                                        <div style="height: 160px;width: 250px;border: 1px solid #8b8c82; background-color:white;color: #8b8c82;"
                                            :span="6">
                                            <el-icon
                                                style="color: #e2e2e2; font-size: 50px; margin-left: 90px;margin-top: 40px;  "
                                                @click="add">
                                                <CirclePlusFilled />
                                            </el-icon>
                                            <!-- <i class="el-icon-circle-plus"
                                        style="color: #e2e2e2; font-size: 50px; margin-left: 90px;margin-top: 40px;"
                                        @click="add"> </i> -->
                                            <p style="margin-left: 92px;">添加地址</p>
                                        </div>
                                    </el-col>
                                    <el-dialog title="地址" v-model="showDialog" width="34%">
                                        <el-form ref="newAddress.value" :model="newAddress.value" label-width="80px">
                                            <el-form-item label="姓名">
                                                <el-input v-model="newAddress.receive_name" placeholder="请输入姓名"
                                                    style="width: 450px;"></el-input>
                                            </el-form-item>
                                            <el-form-item label="电话">
                                                <el-input v-model="newAddress.receive_phone" placeholder="请输入电话"
                                                    style="width: 450px;"></el-input>
                                            </el-form-item>
                                            <el-form-item label="地址">
                                                <el-select v-model="newAddress.province" placeholder="省份"
                                                    style="width: 150px;">
                                                    <el-option label="北京" value="北京"></el-option>
                                                    <el-option label="湖北" value="湖北"></el-option>
                                                </el-select>
                                                <el-select v-model="newAddress.city" placeholder="城市"
                                                    style="width: 150px;">
                                                    <el-option label="北京" value="北京"></el-option>
                                                    <el-option label="湖北" value="湖北"></el-option>
                                                </el-select>
                                                <el-select v-model="newAddress.area" placeholder="区"
                                                    style="width: 150px;">
                                                    <el-option label="北京" value="北京"></el-option>
                                                    <el-option label="湖北" value="湖北"></el-option>
                                                </el-select>
                                            </el-form-item>
                                            <el-form-item label="详细地址">
                                                <el-input type="textarea" v-model="newAddress.street"
                                                    style="width: 450px;"></el-input>
                                            </el-form-item>
                                            <el-form-item label="邮编">
                                                <el-input v-model="newAddress.postal_code" placeholder="请输入邮编"
                                                    style="width: 450px;"></el-input>
                                            </el-form-item>
                                            <el-form-item>
                                                <el-button type="primary"
                                                    style="background-color: #ff6501;width: 200px;"
                                                    @click="onSubmit">确定</el-button>
                                                <el-button style="width: 200px; margin-left: 50px;"
                                                    @click="showDialog = false">取消</el-button>
                                            </el-form-item>
                                        </el-form>
                                    </el-dialog>
                                </el-row>
                            </div>
                            <h3 class="box-title">商品</h3>
                            <hr>
                            <div class="box-body" v-for="item in products" :key="item.id"
                                style="font-size: 17px; display: flex; justify-content: space-around;">
                                <div style="width: 700px; ">
                                    <img :src="`src/assets/images` + item.product_main_image" alt=""
                                        style="height: 50px;width: 50px;vertical-align: middle;">
                                    <span style="vertical-align: middle;">{{ item.product_name }} {{ item.product_subtitle
                                        }}</span>
                                </div>
                                <div style="width: 200px;">
                                    <span> {{ item.product_unit_price }}元x{{ item.quantity }}</span>
                                </div>
                                <div style="color: #ff6501;">
                                    <span>{{ item.product_total_price }}元</span>
                                </div>
                            </div>
                            <hr>
                            <!-- 配送时间 -->
                            <h3 class="box-title">配送方式 <span style="color: #ff6501; margin-left: 40px;">包邮</span></h3>
                            <div class="box-body">
                                <div class="total">
                                    <dl>
                                        <dt>商品件数：</dt>
                                        <dd>{{ number }}件</dd>
                                    </dl>
                                    <dl>
                                        <dt>商品总价：</dt>
                                        <dd>{{ money }}元</dd>
                                    </dl>
                                    <dl>
                                        <dt>优惠活动：</dt>
                                        <dd>0元</dd>
                                    </dl>
                                    <dl>
                                        <dt>运<i></i>费：</dt>
                                        <dd>0元</dd>
                                    </dl>
                                    <dl>
                                        <dt>应付总额：</dt>
                                        <dd class="price">{{ money }}元</dd>
                                    </dl>
                                </div>
                            </div>
                            <!-- 提交订单 -->
                            <div class="submit">
                                <el-button @click="createOrder"
                                    style="background-color: #ff6501; height: 55px;  color: white; margin-bottom: 50px;"
                                    size="large">去结算</el-button>
                            </div>
                        </div>
                    </div>
                </div>


            </div>
            <div style="height: 50px;">

            </div>
        </div>
        <NavServiceBar></NavServiceBar>
        <NavFooter></NavFooter>
    </div>

</template>

<style>
.typeActive {
    border: 1px solid #FF6600;
    color: #fff;
}

.type {
    border: 1px solid #8b8c82;
    color: #fff;
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
        margin-left: 34px;

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

.xtx-pay-checkout-page {
    margin-top: 20px;

    .wrapper {
        background: #fff;
        padding: 0 20px;

        .box-title {
            font-size: 16px;
            font-weight: normal;
            padding-left: 10px;
            line-height: 70px;
            border-bottom: 1px solid #f5f5f5;
        }

        .box-body {
            padding: 20px 0;
        }
    }
}

.address {
    border: 1px solid #f5f5f5;
    display: flex;
    align-items: center;

    .action {
        width: 420px;
        text-align: center;

        .btn {
            width: 140px;
            height: 46px;
            line-height: 44px;
            font-size: 14px;

            &:first-child {
                margin-right: 10px;
            }
        }
    }
}

.total {
    dl {
        display: flex;
        justify-content: flex-end;
        line-height: 30px;
        font-size: 15px;

        dd {
            width: 80px;
            text-align: right;
            padding-right: 10px;
            color: #FF6600;

            &.price {
                font-size: 20px;

            }
        }
    }
}

.submit {
    text-align: right;
    padding: 60spx;
    border-top: 1px solid #f5f5f5;
}
</style>