<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
const child = ref([])
const form = ref([])
const load = function () {
    api({ url: "/gate/" + 0, method: "get" }).then((res) => {

        form.value = res.data
        // console.log(form.value)
        for (let i = 1; i <= form.value.length; i++) {
            api({ url: "/gate/" + i, method: "get" }).then((res) => {
                // child.value.push = res.data
                form.value[i - 1].child = res.data
                // console.log(child.value)
                // form.value.push(child.value)
                // form.value[0] = [...form.value, child.value];
            })
        }
        // for(let i=0;i<form.value.length;i++){
        // }
    })
}
const img = ref('src/assets/icon-arrow.png')
onMounted(() => {
    load()

})
</script>

<template>
    <div class="home-category">
        <ul class="menu">
            <li v-for="item in form" :key="item.id" style="display: flex;justify-content: space-between;">
                <RouterLink to="/" style="margin-left: -50px;">{{ item.name }}</RouterLink>
                <img :src="img" style="height: 17px; margin-right: 25px;margin-top: 18px;">
                <!-- 弹层layer位置 -->
                <div class="layer">
                    <ul>
                        <li v-for="i in item.child" :key="i.id">
                            <RouterLink>
                                <div>
                                    <p class="name ellipsis-2">
                                        {{ i.name }}
                                    </p>
                                </div>
                            </RouterLink>
                        </li>
                    </ul>
                </div>
            </li>
        </ul>
    </div>

</template>

<style scoped>
ul li::marker {
    display: none;
}

.home-category {

    width: 200px;
    height: 417px;
    background-color: rgba(121, 124, 138, 0.5);
    position: relative;
    z-index: 99;
    margin-bottom: -417px;

    .menu {
        li {
            padding-left: 40px;
            height: 55px;
            line-height: 55px;

            a {
                margin-right: 5px;
                color: #fff;

                &:first-child {
                    font-size: 16px;
                }
            }

            .layer {
                width: 200px;
                height: 417px;
                background-color: rgba(121, 124, 138, 0.5);
                position: absolute;
                left: 200px;
                top: 0;
                display: none;

                ul {
                    display: flex;
                    flex-wrap: wrap;
                    /* border: 1px solid #eee; */

                    li {
                        width: 200px;
                        height: 52px;
                    }
                }
            }

            &:hover {
                .layer {
                    display: inline;
                }
            }
        }
    }
}
</style>