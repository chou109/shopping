<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
import { useRouter } from 'vue-router';
const router = useRouter()
const small = ref(false)
const total = ref(0);
const pageSize = ref(8)
const currentPage = ref(1)
const background = ref(false)
const disabled = ref(false)
const form = ref([])
const cart = ref('src/assets/icon-cart-hover.png')
const handleSizeChange = (val) => {
    fetchMenuList();
}
const handleCurrentChange = (val) => {
    fetchMenuList();
}
const fetchMenuList = function () {
    // pageNum: currentPage.value, pageSize: pageSize.value
    // let page=currentPage.value
    // let pageSize=pageSize.value
    api({ url: '/page/product/whole', method: 'get', params: { page: currentPage.value, pageSize: pageSize.value } }).then((res) => {
        // if (res && res.data) {
            form.value = res.data.data
            total.value = parseInt(res.data.total || 0)
            console.log(form.value);
        // }
    })
}

onMounted(fetchMenuList);
</script>

<template>
    <div class="footer" style="width: 1400px;margin: 0 auto;">
        <div>
            <p style="font-size: 30px;">商品列表</p>
        </div>

        <div style="margin: 10px 0; ">
            <el-row :gutter="10">
                <el-col :span="6" v-for="item in form" :key="item.id" style="margin-top: 10px;">
                    <RouterLink :to="{name:'details',query:{id:item.id}}" >
                    <el-card>
                        <div >
                            <img :src="`src/assets/images` + item.main_image" alt="" style="width: 100%;height: 100%;">
                            <div style="text-align: center;">
                                <div style="height: 40px;">
                                    <p style="font-size: 15px;"><strong>{{ item.name }}</strong></p>
                                </div>
                                <p style="color: red;margin-bottom: 15px;"><strong>{{ item.price }} </strong>
                                    <img :src="cart" style="height: 16px;margin-bottom: -4px;">
                                </p>
                                <hr>
                                <p style="color:#b2b3b5; font-size: 10px;">{{ item.sub_title }}</p>
                            </div>
                        </div>
                    </el-card>
                </RouterLink>
                </el-col>
            </el-row>
            <div class="demo-pagination-block" style="margin-top: 50px;" >
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" 
                :small="small"   :page-sizes="[8, 12, 16, 20]" :disabled="disabled" :background="background"
                    layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </div>

    </div>

</template>

<style scoped>
.demo-pagination-block+.demo-pagination-block {
    margin-top: 10px;
}

.demo-pagination-block .demonstration {
    margin-bottom: 16px;
}

hr {
    border-color: #d7d7d7;
}

.el-col {
    &:hover {
        transform: translate3d(0, -3px, 0);

    }
}
</style>