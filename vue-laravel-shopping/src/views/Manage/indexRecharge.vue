<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router';
const router = useRouter()
import { useRoute } from 'vue-router';
// import { reactive } from 'vue'
// import type { FormInstance } from 'element-plus'
// const ruleFormRef = ref<FormInstance>()
// const 
const route = useRoute()
const small = ref(false)
const background = ref(false)
const disabled = ref(false)
const tableData = ref([])
const users = ref([])
const postUse = ref([])
const currentPage = ref(1)
const pageSize = ref(2)
const total = ref(0)
const ruleForm = ref({
    id: "", order_id: "", order_no: "",
    balance: '',
    user_id: ""
})

const handleSizeChange = (val) => {
    fetchMenuList();
}
const handleCurrentChange = (val) => {
    fetchMenuList();
}
const fetchMenuList = function () {
    api({ url: '/recharge', method: 'get', params: { page: currentPage.value, pageSize: pageSize.value } }).then((res) => {
        // console.log(res)
        tableData.value = res.data.data
        total.value = parseInt(res.data.total)
        // console.log(res);
    })
}
const user = function () {
    api.get("/user").then((res) => {
        // console.log(res)
        users.value = res.data
    })
}

const submitForm = function () {
    // console.log(ruleForm.value.userId+" "+ruleForm.value.balance)
    if (!ruleForm.value.user_id) {
        ElMessage.error('请选择用户')
        return
    } else if (!ruleForm.value.balance || parseInt(ruleForm.value.balance) < 0) {
        ElMessage.error('请输入正确的金额')
    } else {
        api.post("/recharge/info", ruleForm.value).then((res) => {
        })


        // for (let i = 0; i < users.value.length; i++) {
        //     if (users.value[i].id === ruleForm.value.userId) {

        //         postUse.value = users.value[i]
        //     }
        // }
        // postUse.value.balance += ruleForm.value.balance
        // console.log(ruleForm.value.user_id+" "+ruleForm.value.balance)
        let money=ref(1)
        api.get("/user/"+ruleForm.value.user_id).then((res)=>{
            money.value=parseInt(res.data[0].balance)+parseInt(ruleForm.value.balance)
            console.log( money.value)
            // console.log(parseInt(res.data[0].balance)+" "+parseInt(ruleForm.value.balance))
            api.post("/users/re/"+ruleForm.value.user_id, {balance: money.value}).then((res) => {
            console.log(res)
            ElMessage({
                message: '充值成功',
                type: 'success',
            })
            fetchMenuList()
            ruleForm.value ={}
        })
        })
        
        // ruleForm.value ={}
        

    }
}
onMounted(() => {
    fetchMenuList()
    user()
})


</script>

<template>

    <div style="width: 1200px;">
        <div style="display: flex;justify-content: space-between;height: 50px;">
            <span>首页 / 充值列表</span>
            <span>欢迎您</span>

        </div>
        <hr style="margin-bottom: 20px;">
        <div style="text-align: left; line-height: 60px;">
            <el-form :model="ruleForm" ref="ruleFormRef" label-width="100px" class="demo-ruleForm">

                <el-form-item label="用户选择" prop="name">
                    <el-select v-model="ruleForm.user_id" placeholder="请选择活动区域" style="width: 220px;">
                        <el-option :label="item.user_name" :value="item.id" v-for="item in users" :key="item.id">
                            {{ item.user_name }}</el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="金额输入" prop="balance">
                    <el-input v-model.number="ruleForm.balance" style="width: 220px;" type="number"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="submitForm()">充值</el-button>
                </el-form-item>
            </el-form>
            <!-- <script></script> -->
        </div>
        <div>
            <el-table :data="tableData" row-key="id" default-expand-all style="width: 100%;line-height: 0px;">
                <el-table-column prop="id" label="ID" width="80px" align="center">
                </el-table-column>
                <el-table-column prop="create_time" label="创建时间" width="300px" align="center">

                </el-table-column>
                <el-table-column prop="user_id" label="用户id" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="balance" label="金额" align="center">
                </el-table-column>
                <el-table-column prop="order_id" label="订单id" align="center">
                    <template v-slot="scope">
                        <span v-if="!scope.row.order_id">充值</span>
                        <span v-if="scope.row.order_id">{{ scope.row.order_id }}</span>
                    </template>
                </el-table-column>
            </el-table>

        </div>
        <div class="demo-pagination-block" style="margin-top: 50px;">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :small="small"
                :page-sizes="[2, 4, 8, 16]" :disabled="disabled" :background="background"
                layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" />
        </div>

    </div>
</template>

<style scoped>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
    -webkit-appearance: none;
}

input[type="number"] {
    -moz-appearance: textfield;
}

inpit {
    border: none
}
</style>