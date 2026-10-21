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
const time = ref()
const money = ref()
const showDialog = ref(false)
const ti = ref()
const userId = ref()
const good = ref(false)
const settlement = function () {
    showDialog.value = true
}
const myMoney = ref()
const ruleForm = ref({
    id: "", order_id: "", order_no: "",
    balance: '',
    user_id: ""
})
const dataForm = ref()
const yu = ref()
const yes = function () {
    showDialog.value = false
    if (good.value) {
        ElMessage.error('您已经付款过了')
        return false
    }
    if (myMoney.value >= money.value) {
        good.value = true
        // console.log("ss"+userId.value)
        api.get("/user/" + userId.value).then((res) => {
            dataForm.value = res.data
            myMoney.value = dataForm.value[0].balance
            yu.value = myMoney.value - money.value
            api.post("/user/balance" + userId.value, { yu: yu.value }).then((res) => {
                console.log(res)
            })
            localStorage.setItem("balance", yu)
            dataForm.value[0].balance = yu
            console.log(dataForm.value)
            api.post("/user/shopping", dataForm.value[0]).then((res) => {
                console.log(res)
                ElMessage({
                    message: '购买成功',
                    type: 'success',
                })
                good.value = true
                for (var i = 0; i < form.value.length; i++) {
                    form.value[i].status = 20
                    form.value[i].statusDesc = "已付款"
                    api.post("/order", form.value[i]).then((res) => {
                        ElMessage({
                            message: '支付成功',
                            type: 'success',
                        })
                    })
                }
            })
            ruleForm.value.balance = money.value * -1
            ruleForm.value.order_no = ti.value
            ruleForm.value.user_id = userId.value
            ruleForm.value.order_id = userId.value
            api.post("/recharge/info", ruleForm.value).then((res) => {
                // console.log(res)
            })
            api.post("/sss/balance/" + userId.value, { yu: yu.value }).then((res) => {
                ElMessage({
                    message: '购买成功',
                    type: 'success',
                })
                centerDialogVisible.value = true
            })
        })
        // for (var i = 0; i < form.value.length; i++) {
        // form.value[i].status = 20
        // form.value[i].status_desc = "已付款"
        api.post("/order", form.value[0]).then((res) => {
            console.log(res)
            ElMessage({
                message: '支付成功',
                type: 'success',
            })
        })
        // }
        // this.router.push('/order')

    } else {
        ElMessage.error('余额不足')
        return false;
    }
}
const no = function () {
    showDialog.value = false
}
const centerDialogVisible = ref(false)
const receiveName = ref()
const receivePhone = ref()
const province = ref()
const city = ref()
const area = ref()
const street = ref()
const order = ref()
const form = ref()
const user = ref([])
const userName = ref()
onMounted(() => {
    window.scrollTo(0, 0);
    user.value = JSON.parse(localStorage.getItem("user"))
    userId.value = user.value.id
    userName.value = user.value.user_name
    api.get("/user/" + userId.value).then((res) => {
        myMoney.value = res.data[0].balance
        console.log(myMoney.value)
    })

    time.value = JSON.parse(route.query.time)
    ti.value = Number((time.value + "").substring(0, 10))
    // console.log(ti.value )
    api.post("/order/child/sss", { order_on: ti.value }).then((res) => {
        // console.log(res)
        form.value = res.data
    })
    order.value = JSON.parse(route.query.order)
    money.value = JSON.parse(route.query.money)
    receiveName.value = order.value.receive_name
    receivePhone.value = order.value.receive_phone
    province.value = order.value.province
    city.value = order.value.city
    area.value = order.value.area
    street.value = order.value.street
    // sessionStorage.setItem('order', JSON.parse(route.query.order));

    // 在组件中从 sessionStorage 获取数据
    //  order.value = sessionStorage.getItem('order');
})
</script>

<template>

    <div>
        <div class="order-header" style="padding-bottom: 70px;">
            <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
            <div class="container clearfix">

                <div class="title">
                    <p style="font-size: 30px;">订单支付<span>请谨防钓鱼链接或诈骗电话，了解更多</span></p>

                </div>
                <div class="username">
                    <a href="javascript:;"> {{ userName }} </a>

                </div>
            </div>
        </div>
        <div style="height: 30px; background-color: #f5f5f5;"></div>
        <div style="display: flex;justify-content: flex-start;margin-top: 50px; margin-left: 35px;margin-bottom: 30px;">
            <i class="el-icon-success"
                style="font-size: 120px ;color: #80c68a;height: 130px;width: 150px;vertical-align: middle;"></i>
            <div style="vertical-align: middle;">
                <span style="font-size: 30px;"><strong>订单提交成功！去付款咯~</strong></span><br><br><br>
                <span style="font-size: 16px;">请在 <span style="color: #e27b54;">30分</span> 内完成支付，超时后将会取消订单<br>

                    收货人信息：{{ receiveName }} {{ receivePhone }} {{ province }} {{ city }} {{
                        area }} {{ street }}

                </span>
            </div>
            <div style=" margin-left: 350px;margin-top: 15px;font-size: 16px;">
                <span>应付总额：<span style="font-size: 30px;color: #ff7800;">{{ money }}</span></span><br>
                <br>
                <span>详细订单^</span>
            </div>
        </div>
        <hr style="border: 2px solid #f5f5f5;">

        <div
            style="display: flex;justify-content: flex-start;margin-top: 50px;margin-left: 180px;font-size: 17px;margin-bottom: 100px;">
            <div>
                <p>订单号：</p><br>
                <p>收货信息：</p><br>
                <p>商品名称：</p><br>
            </div>
            <div style="margin-left: 50px;">
                <p style="margin-top: 3px;">{{ ti }}</p><br>
                <p>
                    {{ receiveName }} {{ receivePhone }} {{ province }} {{ city }} {{
                        area }} {{ street }}
                </p><br>
                <div v-for="item in form" :key="item.id">
                    <p><img :src="`src/assets/images` + item.product_main_image" alt=""
                            style="height: 35px;width: 25px;vertical-align: middle;">{{ item.product_name
                            }}</p><br>
                </div>

            </div>
        </div>
        <div style="height: 30px;background-color: #f5f5f5;">
        </div><br>
        <div style="margin-left: 50px;padding-bottom: 50px">
            <span style="font-size: 20px;">请选以下支付方式</span><br><br>
            <hr><br>
            <span style="font-size: 15px;">支付平台</span><br><br>
            <span style="border: 2px solid #e3e4e7;" @click="settlement()"><img
                    src="/src/assets/icon-wechat.png"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <span style="border: 2px solid #e3e4e7;" @click="settlement()"><img src="/src/assets/icon-ali.png"></span>

        </div>
        <div>
            <el-dialog title="支付确认" v-model="showDialog" width="30%">
                <span>您确定是否支付？</span>
                <br><br><br>
                <div style="display: flex;justify-content: center;">
                    <button
                        style="height: 40px;width: 160px;margin-right: 20px;background-color: #ff6501;border: 0px;color: white;"
                        @click="yes()">支付</button>
                    <button style="height: 40px;width: 160px;background-color: #b0b0b0;border: 0px;color: white;"
                        @click="no()">再想想</button>
                </div>
            </el-dialog>
        </div>
        <div style="height: 50px;background-color: #f5f5f5;"></div>
        <NavServiceBar></NavServiceBar>
        <NavFooter></NavFooter>
    </div>
    <el-dialog v-model="centerDialogVisible" title="提示" width="500" center>
        <span>

        </span>
        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="$router.push('/index')">返回主页</el-button><br><br></br>
                <el-button type="primary" @click="$router.push('/order')">
                    查看订单
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<style scoped></style>