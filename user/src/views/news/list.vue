<template>
  <div class="main">
    <el-image style="width: 100%" :src="newsBgUrl" fit="fill" />
    <el-image style="width: 500px; margin: 25px 0" :src="newsUrl" fit="fill" />
    <div class="breadcrumb-box">

    </div>
    <div class="card-box" v-if="list.length">
    
    </div>
    <div >
      <img style="width: 200px;margin-top: 100px;" src="@/assets/images/nodata.png" alt="">
      <div style="font-size: 14px;color: #999999;margin-top: 10px;">暂无内容</div>
    </div>
   
  </div>
</template>

<script setup>
import newsBgUrl from "@/assets/images/news-bg.png";
import newsUrl from "@/assets/images/news.png";
import newsUrl1 from "@/assets/images/pic4.png";

const { proxy } = getCurrentInstance();
const list = ref([]);


</script>

<style lang="scss" scoped>

</style>
