<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from "@/axios/request.js"; //引入api
import { ElMessage } from 'element-plus'
const router = useRouter()
const user = ref({
    username: "admin",
    password: "123456"
})

const login = function () {
    let userName = user.value.username
    let password = user.value.password
    api.post("/login", { userName, password }).then((res) => {
        // console.log(res)
        if (res.data.code == 200) {
            localStorage.setItem("user", JSON.stringify(res.data.data[0]))
            // localStorage.setItem("Authorization", JSON.stringify(res.data.data.Authorization))
            localStorage.setItem("Authorization", JSON.stringify(res.data.Authorization))
            router.push({
                path: '/home',
            })
            console.log(res)
            console.log(res.data.data[0].balance)
        }else{
            ElMessage.error('用户名或密码错误')
        }


    })
    // console.log(user.value.username+" "+user.value.password)

}


onMounted(()=>{
    localStorage.removeItem("user")//删除电脑储存
    // localStorage.removeItem("Authorization")//删除电脑储存
})

</script>

<template>
    <div class="wrapper">
        <div
            style="margin: 200px auto; background-color: #fff;width: 350px;height: 300px;padding: 20px;border-radius: 10px;">
            <div style="margin: 20px 0;text-align: center;font-size: 24px;"><b>登录</b></div>
            <el-form :model="user">
                <el-form-item prop="username">
                    <el-input prefix-icon="Avatar" size="medium" v-model="user.username" placeholder="请输入账号"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input prefix-icon="Key" size="medium" show-password v-model="user.password"
                        placeholder="请输入密码"></el-input>
                </el-form-item>

                <el-form-item style="text-align: right;">
                    <el-button type="primary" autocomplete="off" @click="login()" style="width: 120px;">登录</el-button>
                    <!-- <el-button type="warning" autocomplete="off" style="width: 120px;">注册</el-button> -->
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<style scoped>
.wrapper {
    height: 100vh;
    background-image: linear-gradient(to bottom right, #FC466B, #3f5efb);
    /* 超出部分隐藏 */
    overflow: hidden;
}
</style>