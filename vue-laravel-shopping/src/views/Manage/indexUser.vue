<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router';
const router = useRouter()
import { useRoute } from 'vue-router';
const route = useRoute()
const showDialog = ref(false)
const small = ref(false)
const background = ref(false)
const disabled = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(2)
const total = ref(0)
const form = ref({
    user_name: "",
    status: "",
    email: "",
    phone: "",
    nickname: "",
    type: "",
})
const handleSizeChange = (val) => {
    fetchMenuList();
}
const handleCurrentChange = (val) => {
    fetchMenuList();
}
const fetchMenuList = function () {
    api({ url: '/users/page', method: 'get', params: { page: currentPage.value, pageSize: pageSize.value } }).then((res) => {
        tableData.value = res.data.data
        total.value = parseInt(res.data.total)
        // console.log(res);
    })
}
const handeEdit = function (row) {
    showDialog.value = true
    form.value = JSON.parse(JSON.stringify(row))//修改时点确认才会修改
}
const del = function (id) {
    api.delete("/user/"+id).then((res)=>{
        ElMessage({
            message: '删除成功',
            type: 'success',
        })
        fetchMenuList()
    })
}
const save = function () {
    showDialog.value = false
    api.post("/user", form.value).then((res) => {
        console.log(res)
        ElMessage({
            message: '保存成功',
            type: 'success',
        })
        form.value={}
        fetchMenuList()
    })
}
onMounted(fetchMenuList);
</script>

<template>

    <div style="width: 1200px;">
        <div style="display: flex;justify-content: space-between;height: 100px;">
            <span>首页 / 用户列表</span>
            <span>欢迎您</span>

        </div>
        <hr style="margin-top: -50px;">
        <div>
            <el-table :data="tableData" row-key="id" default-expand-all style="width: 100%;line-height: 0px;">
                <el-table-column prop="id" label="ID" width="80px" align="center">
                </el-table-column>
                <el-table-column prop="status" label="启用标志" width="80px" align="center">
                    <template v-slot="scope">
                        <span v-if="scope.row.status === 1">启用</span>
                        <span v-else>禁用</span>
                    </template>
                </el-table-column>
                <el-table-column prop="user_name" label="用户名" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="email" label="邮箱" align="center">
                </el-table-column>
                <el-table-column prop="phone" label="手机号" align="center">
                </el-table-column>
                <el-table-column prop="nickname" label="昵称" align="center">
                </el-table-column>
                <el-table-column prop="type" label="角色" align="center">
                </el-table-column>
                <el-table-column prop="create_time" label="创建时间" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="address" label="操作" align="center">
                    <template v-slot="scope">
                        <span style="color: blue;">充值 </span>&nbsp;&nbsp;
                        <span style="color: blue;" @click="handeEdit(scope.row)"> 修改</span>
                        <br>
                        <span style="color: blue;" @click="del(scope.row.id)">删除</span>
                    </template>
                    <!-- </div> -->

                </el-table-column>
            </el-table>

        </div>
        <div class="demo-pagination-block" style="margin-top: 50px;">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :small="small"
                :page-sizes="[2, 4, 8, 16]" :disabled="disabled" :background="background"
                layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" />
        </div>
        <el-dialog v-model="showDialog" width="40%">
            <el-form :model="form" label-width="90px">
                <el-form-item label="用户名">
                    <el-input v-model.lazy="form.user_name" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="启用标志">
                    <el-input v-model.lazy="form.status" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="邮箱">
                    <el-input v-model.lazy="form.email" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="电话">
                    <el-input v-model.lazy="form.phone" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="昵称">
                    <el-input v-model.lazy="form.nickname" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="角色">
                    <el-input v-model.lazy="form.type" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" style="background-color: #ff6501;width: 200px;"
                        @click="save">确定</el-button>
                    <el-button style="width: 200px; margin-left: 50px;" @click="showDialog = false">取消</el-button>
                </el-form-item>
            </el-form>
            <!-- <div class="dialog-footer" style="text-align: center;margin-top: -50px;">
                <el-button @click="dialogFormVisible = false" style="width: 100px;">取 消</el-button>
                <el-button type="primary" @click="save" style="width: 100px;">确 定</el-button>
            </div> -->
        </el-dialog>

    </div>
</template>

<style scoped></style>