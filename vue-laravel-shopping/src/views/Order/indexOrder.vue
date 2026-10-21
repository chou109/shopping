<script setup>
import NavFooter from '@/views/NavFooter.vue'
import { ref, onMounted } from 'vue';
import NavServiceBar from '@/views/NavServiceBar.vue'
import api from "@/axios/request.js"; //引入api
import { useRouter } from 'vue-router';
const router = useRouter()
const goods = ref([])
const userId = ref()
const username = ref()
const small = ref(false)
const total = ref(0);
const pageSize = ref(8)
const currentPage = ref(1)
const background = ref(false)
const disabled = ref(false)
const ids=ref([])
const form=ref([])
const add=ref(0)
const num=ref([])
const user=ref([])
const handleSizeChange = (val) => {
    fetchMenuList();
}
const handleCurrentChange = (val) => {
    fetchMenuList();
}
const fetchMenuList = function () {
    window.scrollTo(0, 0);
    user.value=JSON.parse(localStorage.getItem("user"))
    // console.log(user.value)
    userId.value=user.value.id
    username.value=user.value.user_name
    api({ url: '/order/'+userId.value, method: 'get', params: { page: currentPage.value, pageSize: pageSize.value } }).then((res) => {
        console.log(res)    
        form.value = res.data.data || [];
            total.value = parseInt(res.data.total || 0)
            // console.log(form.value);
            ids.value = []
            ids.value = [...new Set(form.value.map(v => v.order_no))]
            goods.value=[]
            num.value=[]
            for (let i = 0; i < ids.value.length; i++) {
                    let addNew = [];
                    add.value = 0;
                    // console.log(form.value[i].orderNo)
                    for (let j = 0; j < form.value.length; j++) {
                        if (form.value[j].order_no === ids.value[i]) {
                            addNew.push(form.value[j])
                            add.value += parseInt(form.value[j].total_price);
                        }
                    }
                    add.value = parseFloat(add.value).toFixed(2);
                    num.value.push(add.value);
                    goods.value.push(addNew)
                }
                // console.log(goods.value)
    })
}
const dian = (index)=>{
    router.push({
        path:'/orderchild',
        query:{orderchild: JSON.stringify(goods.value[index]), num: JSON.stringify(num.value[index])}
    })
}
onMounted(fetchMenuList);
</script>
<template>
    <div class="order-header" style="padding-bottom: 70px;">
        <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
        <div class="container clearfix">
            <div class="title">
                <p style="font-size: 30px;">订单列表 <span>请认真填写收货地址</span></p>
            </div>
            <div class="username">
                <a href="javascript:;"> {{username}} </a>
            </div>
        </div>
    </div>

    <div class="order-container">
        <el-tabs>
            <div class="main-container">
                <div>
                    <!-- 订单列表 -->
                    <div class="order-item" v-for="(order, index) in goods" :key="index" @click="dian(index)">
                        <div class="head" style="display: flex;justify-content: space-between;color: #919da4;">
                            <div>
                                <span>下单时间:{{ order[0].create_time }}</span>
                                <span> |</span>
                                <span> 购买人ID:{{ order[0].user_id }}</span>
                                <span> |</span>
                                <span> 订单编号:{{ order[0].order_no }}</span>
                                <span> |</span>
                                <span> 在线支付</span>
                            </div>

                            <span>应付金额： <span style="font-size: 20px;color: black;"> {{ num[index] }}</span>元
                            </span>
                        </div>
                        <div class="body">
                            <div class="column goods">
                                <ul>

                                    <li v-for="item in goods[index]" :key="item.id">
                                        <a class="image" href="javascript:;">
                                            <img :src="`src/assets/images`+item.product_main_image" alt="" style="height: 80px;width: 80px;margin-left: -30px;">

                                            <!-- <img :src="require(`../assets/images` + item.productMainImage)" alt="" /> -->
                                        </a>
                                        <div class="info">
                                            <p class="name ellipsis-2">
                                                {{ item.product_name }}
                                            </p>
                                            <p style="color: #fe7500;">
                                                <span>{{ item.current_unit_price }}X{{ item.quantity }}</span>
                                            </p>
                                        </div>
                                        <div>
                                            {{ item.status_desc }}

                                        </div>
                                    </li>
                                </ul>
                            </div>

                        </div>
                    </div>
                    <div class="demo-pagination-block" style="margin-top: 50px;">
                        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :small="small"
                            :page-sizes="[8, 12, 16, 20]" :disabled="disabled" :background="background"
                            layout="total, sizes, prev, pager, next, jumper" :total="total"
                            @size-change="handleSizeChange" @current-change="handleCurrentChange" />
                    </div>

                </div>
            </div>

        </el-tabs>
    </div>
    <NavServiceBar></NavServiceBar>
    <NavFooter></NavFooter>
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
