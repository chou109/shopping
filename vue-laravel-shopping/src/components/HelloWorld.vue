<script setup>
import { ref, onMounted } from 'vue';
import api from "@/axios/request.js"; //引入api
const texts = function () {
  api.get("/index").then((res) => {
    // console.log(res)
  })
}
const load = function () {
  let userName = "abc";
  let password = "123456"
  api.post("/login", {userName,password}).then((res) => {
    console.log(res)
  })
}

onMounted(() => {
  texts(),load()
})
</script>

<template>
  <div>
    niss
  </div>

</template>