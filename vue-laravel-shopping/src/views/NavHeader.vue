<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from "@/axios/request.js"; //引入api
const router = useRouter()
const logout = function () {
    localStorage.removeItem("user")//删除电脑储存
    localStorage.removeItem("Authorization")//删除电脑储存
    router.push({
        path: "/",
    })
}
const user = ref([])
const type = ref()
const userName = ref()
onMounted(() => {
    user.value = JSON.parse(localStorage.getItem("user"))

    if (user.value) {
        console.log(user.value)
        type.value = user.value.type
        userName.value = user.value.user_name
        console.log(userName.value)
    } else {
        console.log("wu")
    }

})
</script>

<template>
    <div class="header" style="width: 2000px;margin: 0 auto;">
        <div class="nav-topbar">
            <div class="container">
                <div class="topbar-menu">
                    <a v-if="userName">{{ userName }}</a>
                </div>
                <div class="topbar-user">
                    <a href="javascript:;" v-if="!userName" @click="$router.push('/')">登录</a>
                    <a href="javascript:;" v-if="userName" @click="logout">退出</a>
                    <!-- <a href="javascript:;" v-if="userName" @click="$router.push('/index')">主页</a> -->
                    <a href="javascript:;" v-if="userName" @click="$router.push('/order')">我的订单</a>
                    <a href="javascript:;" v-if="userName" @click="$router.push('/add')">地址管理</a>
                    <!-- <a href="javascript:;" v-if="userName">充值管理</a> -->
                    <a href="javascript:;" v-if="userName" class="my-cart" @click="$router.push('/cart')">
                        <span class="icon-cart"></span>购物车</a>
                    <!-- v-if="type === 'staff'"  -->
                    <a href="javascript:;" style="margin-left: 20px;" v-if="type === 'staff'"
                        @click="$router.push('/manage')">后端管理</a>
                </div>
            </div>
        </div>
        <!-- <div class="nav-header">
            <div class="container">
                <div class="logo">
                    <img src="../assets/eshop.png" @click="toIndex()">
                </div>
                <div class="header-search">
                    <div class="wrapper" style="border: 2px solid black;">
                        <input type="text" name="keyword">
                        <a href="javascript:;">搜索</a>
                    </div>
                </div>
            </div>
        </div> -->
    </div>

</template>

<style>
.header {
    .nav-topbar {
        height: 40px;
        line-height: 39px;
        background-color: #f0f0f0;
        color: #B0B0B0;

        .container {
            /* @include flex(); */
            display: flex;
            justify-content: space-around;

            a {
                display: inline-block;
                color: #000000;
                margin-right: 20px;
            }

            .my-cart {
                width: 110px;
                background-color: #FF6600;
                text-align: center;
                color: #ffffff;
                margin-right: 0;
                height: 40px;
            }
        }
    }

    .logo {
        position: relative;
        left: -70px;
    }

    .nav-header {
        .container {
            position: relative;
            height: 112px;
            display: flex;
            justify-content: space-around;
            margin-top: 20px;

            .header-menu {
                .item-menu {

                    &:hover {
                        color: red;

                        .children {
                            height: 220px;
                            opacity: 1;
                        }
                    }

                    .children {
                        .product {
                            a {
                                display: inline-block;
                            }


                            .pro-img {
                                height: 137px;
                            }

                            .pro-name {
                                font-weight: bold;
                                margin-top: 19px;
                                margin-bottom: 8px;
                                color: red;
                            }

                            .pro-price {
                                color: red;
                            }

                            &:before {
                                content: ' ';
                                position: absolute;
                                top: 28px;
                                right: 0;
                                border-left: 1px solid #fff;
                                height: 100px;
                                width: 1px;
                            }

                            &:last-child:before {
                                display: none;
                            }
                        }
                    }
                }
            }

            .header-search {
                width: 319px;

                .wrapper {
                    height: 50px;
                    /* border: 1px solid #E0E0E0; */
                    display: flex;
                    align-items: center;

                    input {
                        border: none;
                        box-sizing: border-box;
                        border-right: 1px solid #E0E0E0;
                        width: 264px;
                        /* height: 50px; */
                        padding-left: 14px;
                    }

                    a {
                        /* @include bgImg(18px, 18px, '/imgs/icon-search.png'); */
                        margin-left: 17px;
                    }
                }
            }
        }
    }
}
</style>