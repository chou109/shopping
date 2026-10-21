<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router';
import NavFooter from '@/views/NavFooter.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
const router = useRouter()
const userId = ref()
const cartList = ref([])
const user = ref()
const username = ref()
const load = function () {
    user.value = JSON.parse(localStorage.getItem("user"))
    userId.value = user.value.id
    username.value = user.value.userName
    // console.log(user.value)
    api({ url: '/cart/' + userId.value }).then((res) => {
        console.log(res)
        cartList.value = res.data
        console.log(cartList.value)
        res.data.forEach(item => {
            whole.value += item.quantity
            // console.log(whole.value)
        })
    })
}
const changeQuantity = function (row) {
    // whole.value=0
    // api({ url: '/cart/' + userId.value }).then((res) => {
    //     cartList.value = res.data.data
    //     // console.log(res.data.data)
    //     res.data.data.forEach(item => {
    //         whole.value += item.quantity
    //         // console.log(whole.value)
    //     })
    // })
    whole.value = 0
    // console.log(cartList.value)
    cartList.value.forEach(item => {
        whole.value += item.quantity
    })
    row.product_total_price = row.product_unit_price * row.quantity
    api.post('/cart', row).then((res) => {
        console.log(res)
        total.value = 0
        selected.value = 0
        multipleSelection.value.forEach(item => {
            total.value += item.quantity * item.product_unit_price
            selected.value += item.quantity
        });
    })
}
const selectionChange = function (row) {
    total.value = 0
    selected.value = 0
    multipleSelection.value = row
    // console.log(row)
    multipleSelection.value.forEach(item => {
        total.value += item.quantity * item.product_unit_price
        selected.value += item.quantity
    });
}
const go = function () {
    if (multipleSelection.value.length) {
        router.push({
            path: "/set",
            query: { product: JSON.stringify(multipleSelection.value) }
        })
    } else {
        ElMessage.error('请选择商品')
    }

}
const total = ref(0)
const selected = ref(0)
const whole = ref(0)
const multipleSelection = ref([])

const del = function (id) {
    api.delete('/cart/' + id).then((res) => {
        ElMessage({
            message: '删除成功',
            type: 'success',
        })
        load()
    })
}
onMounted(() => {

    load()
})
</script>

<template>
    <div class="order-header" style="padding-bottom: 70px;">
        <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
        <div class="container clearfix">
            <div class="title">
                <p style="font-size: 30px;">我的购物车 <span>温馨提示:产品是否购买成功，以最终下单为准哦，请尽快结算></span></p>
            </div>
            <div class="username">
                <a href="javascript:;">{{ username }} </a>
            </div>
        </div>
    </div>
    <div style="width: 1800px;margin: 0 auto;background-color:#f5f5f5; padding-top:  20px;">
        <el-table :data="cartList" style="font-size: 15px; width: 1300px;margin-left: 250px;"
            @selection-change="selectionChange" row-key="id">
            <el-table-column type="selection" width="100px" :reserve-selection="true">
            </el-table-column>
            <el-table-column prop="product_main_image" label="商品图片" width="200px" align="center">
                <template v-slot="scope">
                    <el-image :src="`src/assets/images` + scope.row.product_main_image" alt=""
                        style="height: 100px;width: 100px;margin-left: -10px;" />
                </template>
            </el-table-column>
            <el-table-column prop="product_name" label="商品名称" width="200px" align="center"></el-table-column>
            <el-table-column prop="product_unit_price" label="单价" align="center"></el-table-column>
            <el-table-column prop="quantity" label="数量" align="center">
                <template v-slot="scope">
                    <el-input-number v-model="scope.row.quantity" :min="1" @change="changeQuantity(scope.row)" />
                </template>
            </el-table-column>
            <el-table-column prop="productTotalPrice" label="小计" align="center">
                <template v-slot="scope">
                    {{ scope.row.product_unit_price * scope.row.quantity }}
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
                <template v-slot="scope">
                    <img src="/src/assets/icon-close.png" alt="" style="height: 15px;width: 15px;"
                        @click=" del(scope.row.id)" />
                    <!--  @confirm=" del(scope.row.id)" -->
                </template>
            </el-table-column>
        </el-table>
        <div class="action">
            <div>
                继续购物 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;共 {{ whole }}<span style="color: #ff6501;"> </span>
                件商品，已选择<span style="color: #ff6501;"> {{ selected }}</span> 件
            </div>
            <div>
                <span style="color: #ff6501;margin-right: 40px;">合计:{{ total }} <span
                        style="font-size: 30px;"></span>元</span>
                <el-button size="large" style="background-color: #ff6501; color: white;width: 170px;height: 45px;"
                    @click="go">去结算</el-button>
            </div>
        </div>
        <NavServiceBar></NavServiceBar>
        <NavFooter></NavFooter>
    </div>
</template>

<style scoped>
.action {
    display: flex;
    background: #fff;
    margin-top: 20px;
    height: 80px;
    align-items: center;
    font-size: 16px;
    justify-content: space-between;
    padding: 0 30px;
    background-color: #f5f5f5;
    padding-bottom: 20px;
    width: 1300px;
    margin-left: 220px;
}
</style>