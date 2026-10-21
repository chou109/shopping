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
    userName: "",
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
    api({ url: '/page/product/whole', method: 'get', params: { page: currentPage.value, pageSize: pageSize.value } }).then((res) => {
        tableData.value = res.data.data
        total.value = parseInt(res.data.total)
        // console.log(res);
    })
}
const handeEdit = function (row) {
    showDialog.value = true
    form.value = JSON.parse(JSON.stringify(row))
}
const del = function (id) {
    api.delete("/product/"+id).then((res)=>{
        ElMessage({
            message: '删除成功',
            type: 'success',
        })
        fetchMenuList()
    })
}
const save = function () {
    showDialog.value = false
    api.post("/product", form.value).then((res) => {
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
            <span>首页 / 产品列表</span>
            <span>欢迎您，{{ }}</span>

        </div>
        <hr style="margin-top: -50px;">
        <div style="height: 50px;line-height: 60px;text-align: left;">
            <button style="height: 30px;width: 60px;background-color: #3da0fd;border: 0px;color: white;">+新增</button>
        </div>
        <div>
            
            <el-table :data="tableData" row-key="id" default-expand-all style="width: 100%;line-height: 0px;">
                <el-table-column prop="id" label="主键" width="80px" align="center">
                </el-table-column>
                <el-table-column prop="category_id" label="品类id" width="80px" align="center">

                </el-table-column>
                <el-table-column prop="product_id" label="商品id" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="name" label="商品名称" align="center">
                </el-table-column>
                <el-table-column prop="sub_title" label="简要描述" align="center">
                </el-table-column>
                <el-table-column prop="main_image" label="商品图片地址" align="center">
                    <template v-slot="scope">
                        <el-image style="width: 100px; height: 100px" :src="`src/assets/images` + scope.row.main_image" fit="fill"></el-image>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="商品状态" align="center">
                </el-table-column>
                <el-table-column prop="price" label="商品单价" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="stock" label="库存数" width="150px" align="center">
                </el-table-column>
                <el-table-column prop="address" label="操作" align="center">
                    <template v-slot="scope">
                        <span style="color: blue;" @click="handeEdit(scope.row)"> <i class="el-icon-edit"></i>修改</span>
                        <br>
                        <span style="color: blue;" @click="del(scope.row.id)"><i class="el-icon-delete"></i>删除</span>
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
            <el-form :model="form" label-width="90px" style="width: 100%; text-align: left;">
                <el-form-item label="商品名称">
                    <el-input v-model.lazy="form.name" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="简要描述">
                    <el-input v-model.lazy="form.sub_title" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="商品图片地址" >
                    <el-input v-model.lazy="form.main_image" autocomplete="off">
                    <!-- <el-image style="width: 100px; height: 100px" src="../assets/images/phone-1.jpg"></el-image> -->
                    </el-input>
                </el-form-item>
                <el-form-item label="商品状态">
                    <el-input v-model.lazy="form.status" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="商品单价">
                    <el-input v-model.lazy="form.price" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="库存数">
                    <el-input v-model.lazy="form.stock" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" style="background-color: #ff6501;width: 200px;"
                        @click="save">确定</el-button>
                    <el-button style="width: 200px; margin-left: 50px;" @click="showDialog = false">取消</el-button>
                </el-form-item>
            </el-form>

        </el-dialog>

    </div>
</template>

<style scoped>
.el-form-item__label{
    padding-right: 0px;
    text-align: left;
}
element.style{
    width: 200px;
}
</style>