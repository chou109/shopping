<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
const route = useRoute()
import { useRouter } from 'vue-router';
const router = useRouter()
const activeIndex = route.path
const user=ref([])
const type=ref()
// const cuo=function(){
//     router.push({
//         path:'/sls'
//     })
// }
onMounted(()=>{
    user.value=JSON.parse(localStorage.getItem("user"))
    type.value=user.value.type
    console.log(type.value)
    
    if(type.value==="user"){
        console.log("yse")
        // cuo()
        router.push('/404')
    }
})
</script>

<template>
    <div class="common-layout">
        <el-container>
            <el-aside width="200px" style="height: 100%; overflow-y: hidden;">
                <el-row class="tac">
                    <el-col style="height: 700px;">
                        <h5 class="mb-2"
                            style="height: 50px;text-align: center;line-height: 50px;background-color: #2b2f3a;color: white;font-size: 25px;">
                            后台管理</h5>
                        <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" background-color="#304057"
                            text-color="#fff" style="height: 100%; "  router>
                            <el-sub-menu index="1">
                                <template #title>
                                    <el-icon>
                                        <location />
                                    </el-icon>
                                    <span>用户管理</span>
                                </template>
                                <el-menu-item-group>
                                    <el-menu-item index="user">用户列表</el-menu-item>
                                </el-menu-item-group>
                            </el-sub-menu>
                            <el-sub-menu index="2">
                                <template #title>
                                    <el-icon>
                                        <location />
                                    </el-icon>
                                    <span>商品管理</span>
                                </template>
                                <el-menu-item-group>
                                    <el-menu-item index="product">商品列表</el-menu-item>
                                </el-menu-item-group>
                            </el-sub-menu>
                            <el-sub-menu index="3">
                                <template #title>
                                    <el-icon>
                                        <location />
                                    </el-icon>
                                    <span>充值管理</span>
                                </template>
                                <el-menu-item-group>
                                    <el-menu-item index="recharge">充值列表</el-menu-item>
                                </el-menu-item-group>
                            </el-sub-menu>
                        </el-menu>
                    </el-col>
                </el-row>
            </el-aside>
            <el-main>
                <router-view></router-view>
            </el-main>
        </el-container>
    </div>
</template>


<style scoped></style>