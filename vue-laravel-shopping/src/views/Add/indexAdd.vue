<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
import NavFooter from '@/views/NavFooter.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
const user=ref([])

const userId = ref()
const userName = ref()
const form = ref([])
const load = function () {
    user.value=JSON.parse(localStorage.getItem("user"))
    // console.log(user.value)
    userId.value=user.value.id
    userName.value=user.value.user_name
    api({ url: '/address/' + userId.value }).then((res) => {
        form.value = res.data
        // console.log(res.data)
    })
    
}
const showDialog=ref(false)
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
    showDialog.value=true
    newAddress.value=JSON.parse(JSON.stringify(row))
    // console.log(row)
}
const onSubmit=function(){
    showDialog.value=false
    // console.log(newAddress.value)
    console.log(newAddress.value)
    api.post('/address/'+userId.value,newAddress.value).then((res)=>{
        console.log(res)
         load()
    })
   
}
const add=function(){
    newAddress.value={}
    showDialog.value=true
    
}
const newAddress = ref({

})
onMounted(() => {
    load()
    

})
</script>

<template>
    <div style="width: 1800px;margin: 0 auto;">
        <div class="order-header" style="height: 100px;">
            <a href="/#/index"> <img style="float:left;width:200px" src="/src/assets/eshop.png"></a>
            <div class="container clearfix">
                <div class="title">
                    <p style="font-size: 30px;">地址填写 <span></span></p>
                </div>
                <div class="username">
                    <a href="javascript:;"> {{userName}} </a>
                </div>
            </div>
        </div>
        <div style="width: 1800px;margin: 0 auto;background-color:#f5f5f5; padding-top:  40px;">
            <div style="background-color: white;width: 1400px;margin-left: 200px;margin-top: 20px;">
                <div class="container">
                    <!-- 收货地址 -->
                    <h3 class="box-title" style="font-size: 25px; padding-top: 40px; margin-left: 50px;">收货地址</h3>
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
                                        <div style="display: flex; justify-content:space-between;margin-top: 20px;">
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
                            <el-col :span="6" style="margin-top: 10px; margin-left: 50px;">
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
                            <el-dialog title="地址"   v-model="showDialog" width="34%">
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
                                    <el-select v-model="newAddress.province" placeholder="省份" style="width: 150px;">
                                        <el-option label="北京" value="北京"></el-option>
                                        <el-option label="湖北" value="湖北"></el-option>
                                    </el-select>
                                    <el-select v-model="newAddress.city" placeholder="城市" style="width: 150px;">
                                        <el-option label="北京" value="北京"></el-option>
                                        <el-option label="湖北" value="湖北"></el-option>
                                    </el-select>
                                    <el-select v-model="newAddress.area" placeholder="区" style="width: 150px;">
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
                                    <el-button type="primary" style="background-color: #ff6501;width: 200px;"
                                        @click="onSubmit">确定</el-button>
                                    <el-button style="width: 200px; margin-left: 50px;"
                                        @click="showDialog = false">取消</el-button>
                                </el-form-item>
                            </el-form>
                            </el-dialog>
                        </el-row>
                    </div>
                </div>

            </div>
            <div style="height: 20px;">

            </div>

        </div>
        <NavServiceBar></NavServiceBar>
        <NavFooter></NavFooter>
    </div>
</template>

<style>
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
        margin-left: 44px;

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

/* //选中变色 */
.typeActive {
    border: 1px solid #FF6600;
    color: #fff;
}

.type {
    border: 1px solid #8b8c82;
    color: #fff;
}
</style>