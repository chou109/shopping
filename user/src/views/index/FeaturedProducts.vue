<template>
  <section v-if="hotProducts.length || phoneProducts.length" class="featured-sections">
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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listProductByCategory, listProductHot } from '@/api/system/product'
import { getEnabledCategories } from '@/api/system/category'
import { isBlindBoxProduct } from '@/utils/product'
import FeaturedProductGrid from '@/views/index/FeaturedProductGrid.vue'

const router = useRouter()
const hotProducts = ref([])
const phoneProducts = ref([])

const rowsFrom = (response) => {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.rows)) return response.rows
  if (Array.isArray(response?.data)) return response.data
  return []
}

const categoryListFrom = (response) => {
  const data = response?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data?.rows)) return data.rows
  return []
}

const findPhoneCategory = (categories) => {
  const queue = [...categories]
  while (queue.length) {
    const category = queue.shift()
    const name = category?.name || category?.categoryName || ''
    if (name.includes('手机') || name.includes('数码')) return category
    const children = category?.children || category?.childList || []
    if (Array.isArray(children)) queue.push(...children)
  }
  return null
}

const loadFeaturedProducts = async () => {
  const hotRequest = listProductHot(8).catch((error) => {
    console.warn('加载热门商品失败:', error)
    return null
  })
  const categoryRequest = getEnabledCategories().catch((error) => {
    console.warn('加载商品分类失败:', error)
    return null
  })

  const [hotResponse, categoryResponse] = await Promise.all([hotRequest, categoryRequest])
  hotProducts.value = rowsFrom(hotResponse).filter(product => !isBlindBoxProduct(product)).slice(0, 8)

  const phoneCategory = findPhoneCategory(categoryListFrom(categoryResponse))
  const categoryId = phoneCategory?.id || phoneCategory?.categoryId
  if (categoryId) {
    try {
      const response = await listProductByCategory(categoryId)
      phoneProducts.value = rowsFrom(response).filter(product => !isBlindBoxProduct(product)).slice(0, 8)
    } catch (error) {
      console.warn('加载手机数码商品失败:', error)
    }
  }
}

const openProduct = (product) => {
  if (!product?.id) return
  router.push({ path: '/product/detail', query: { id: product.id } })
}

const goToProducts = () => router.push('/products')

onMounted(loadFeaturedProducts)
</script>

<style scoped>
.featured-sections {
  display: grid;
  gap: 24px;
  margin: 28px auto 40px;
  width: 100%;
}
</style>
