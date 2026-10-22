<template>
  <section v-if="loading || loadError || hotProducts.length || phoneProducts.length" class="featured-sections">
    <p v-if="loading" class="featured-loading" role="status">精选商品加载中…</p>
    <el-alert v-if="loadError" :title="loadError" type="warning" :closable="false" show-icon>
      <template #default>
        <el-button link type="primary" :disabled="loading" @click="loadFeaturedProducts">重试</el-button>
      </template>
    </el-alert>
    <FeaturedProductGrid
      v-if="hotProducts.length"
      title="热门商品"
      :products="hotProducts"
      show-more
      @view-more="goToProducts"
      @select="openProduct"
    />
    <FeaturedProductGrid
      v-if="phoneProducts.length"
      title="手机数码"
      :products="phoneProducts"
      @select="openProduct"
    />
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listProductByCategory, listProductHot } from '@/api/system/product'
import { getEnabledCategories } from '@/api/system/category'
import { isBlindBoxProduct } from '@/utils/product'
import { findPhoneCategory, readCategoryList, readProductRows } from '@/utils/featuredProducts.mjs'
import FeaturedProductGrid from '@/views/index/FeaturedProductGrid.vue'

const router = useRouter()
const hotProducts = ref([])
const phoneProducts = ref([])
const loading = ref(false)
const loadError = ref('')
let loadRequestId = 0

const loadFeaturedProducts = async () => {
  if (loading.value) return
  const requestId = ++loadRequestId
  loading.value = true
  loadError.value = ''
  hotProducts.value = []
  phoneProducts.value = []

  try {
    const [hotResult, categoryResult] = await Promise.allSettled([
      listProductHot(8),
      getEnabledCategories()
    ])
    if (requestId !== loadRequestId) return

    let hasFailure = false
    if (hotResult.status === 'fulfilled') {
      try {
        hotProducts.value = readProductRows(hotResult.value)
          .filter(product => !isBlindBoxProduct(product)).slice(0, 8)
      } catch (error) {
        hasFailure = true
        console.warn('加载热门商品失败:', error)
      }
    } else {
      hasFailure = true
      console.warn('加载热门商品失败:', hotResult.reason)
    }

    let categories = []
    if (categoryResult.status === 'fulfilled') {
      try {
        categories = readCategoryList(categoryResult.value)
      } catch (error) {
        hasFailure = true
        console.warn('加载商品分类失败:', error)
      }
    } else {
      hasFailure = true
      console.warn('加载商品分类失败:', categoryResult.reason)
    }

    const phoneCategory = findPhoneCategory(categories)
    const categoryId = phoneCategory?.id || phoneCategory?.categoryId
    if (categoryId) {
      try {
        const response = await listProductByCategory(categoryId)
        if (requestId !== loadRequestId) return
        phoneProducts.value = readProductRows(response)
          .filter(product => !isBlindBoxProduct(product)).slice(0, 8)
      } catch (error) {
        hasFailure = true
        console.warn('加载手机数码商品失败:', error)
      }
    }

    if (requestId === loadRequestId && hasFailure) {
      loadError.value = '部分精选商品暂时无法加载，请重试'
    }
  } finally {
    if (requestId === loadRequestId) loading.value = false
  }
}

const openProduct = (product) => {
  if (!product?.id) return
  router.push({ path: '/product/detail', query: { id: product.id } })
}

const goToProducts = () => router.push('/products')

onMounted(loadFeaturedProducts)
onBeforeUnmount(() => {
  loadRequestId += 1
})
</script>

<style scoped>
.featured-sections {
  display: grid;
  gap: 24px;
  margin: 28px auto 40px;
  width: 100%;
}

.featured-loading {
  margin: 0;
  color: #777;
  text-align: center;
}
</style>
