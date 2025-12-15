<template>
  <div class="main">
    <el-image style="width: 100%" :src="mainUrl" fit="fill" />
    <el-image style="width: 500px; margin: 25px 0" :src="newsUrl" fit="fill" />


  </div>
</template>

<script setup>
import { listCart, getCart, delCart, addCart, updateCart } from "@/api/system/cart";
import { listUser } from "@/api/system/user";
const { proxy } = getCurrentInstance();
const router = useRouter();

const list = ref([]);
const params = {
  pageNum: 1,
  pageSize: 5
};

onMounted(() => {
  getList();
});

const getList = () => {
  listCart(params.value).then((res) => {
    console.log("list.value", res);
  });
  listUser(params.value).then((res) => {
    console.log("listUser.value", res);
  });
};



const handleDetail = (id = "") => {
  router.push(`/news/detail?id=${id}`);
};
</script>

<style lang="scss" scoped>
.main {
  display: flex;
  flex-direction: column;
  align-items: center;

  .card-box {
    width: 1150px;
    display: flex;
    justify-content: space-between;
    flex-wrap: wrap;

    .card {
      width: 372px;
      height: 100px;
      border-radius: 10px;
      margin-bottom: 20px;
      cursor: pointer;

      &:hover {
        border: 1px solid #01c3c9;
        box-shadow: 0 0 4px #01c3c9;
      }

      .title {
        font-size: 14px;
        color: #3d3d3d;
        font-weight: bold;
        text-align: left;
        display: -webkit-box;
        overflow: hidden;
        -webkit-box-orient: vertical;
        line-clamp: 2;
        -webkit-line-clamp: 2;

        &:hover {
          color: #01c3c9;
        }
      }

      .footer {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #8a9090;
        margin-top: 10px;
      }
    }
  }

  .more {
    width: 150px;
    height: 34px;
    margin: 20px;
  }

  :deep(.el-card__body) {
    height: 100%;
    padding: 15px 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
}
</style>
