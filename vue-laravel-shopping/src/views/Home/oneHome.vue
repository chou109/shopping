<script setup>
import Banner from '@/views/Home/indexBanner.vue'
import Hot from '@/views/Home/indexHot.vue'
import Phone from '@/views/Home/indexPhone.vue'
import Cate from '@/views/Home/indexCate.vue'
import api from "@/axios/request.js"; //引入api
const ce = function () {
    api({ url: "/ce", method: "get" }).then((res) => {
        console.log(res)

    })

}
const ca = function () {
    api({ url: "/ca", method: "get" }).then((res) => {
        console.log(res)

    })
}
</script>


<template>
    <div style="width: 1000px;margin: 0 auto;">
        <!-- <button @click="ce">ce</button>
        <button @click="ca">ca</button> -->
        <div class="nav-header">
            <div class="container">
                <div class="logo">
                    <img src="../../assets/eshop.png" @click="toIndex()">
                </div>
                <div class="header-search">
                    <div class="wrapper" style="border: 2px solid black;">
                        <input type="text" name="keyword">
                        <a href="javascript:;">搜索</a>
                    </div>
                </div>
            </div>
        </div>
        <Cate></Cate>
        <Banner></Banner>
        <Hot></Hot>
        <Phone></Phone>
    </div>

</template>

<style>
.nav-header {
    .container {
        position: relative;
        height: 112px;
        display: flex;
        justify-content: space-between;
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
</style>