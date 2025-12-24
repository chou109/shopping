<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listProductByCategory } from '@/api/system/product'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const props = defineProps({
  categoryId: {
    type: Number,
    required: true
  },
  categoryName: {
    type: String,
    required: true
  }
})

const router = useRouter()
const products = ref([])
const loading = ref(false)
const name=ref("")

const loadProducts = async () => {
  if (!props.categoryId) return
  
  loading.value = true
  try {
    name.value=props.categoryName
    const response = await listProductByCategory(props.categoryId)
    console.log(response)
    
    if (response.code === 200) {
      products.value = response.data || []
    } else {
      console.error(`API错误: ${response.msg}`)
      products.value = []
    }
  } catch (error) {
    console.error(`获取${props.categoryName}商品失败:`, error)
    products.value = []
  } finally {
    loading.value = false
  }
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 获取商品描述
const getDescription = (item) => {
  if (item.subTitle) return item.subTitle
  if (item.description) {
    return item.description.length > 30 
      ? item.description.substring(0, 30) + '...'
      : item.description
  }
  return '暂无描述'
}

onMounted(() => {
  loadProducts()
})
</script>

<template>
<div class="product-section">
    <div class="section-header">
      <div class="section-title-wrapper">
        <h2 class="section-title">{{ name }}</h2>
        <el-link type="primary" :underline="false" @click="$router.push('/products')" class="view-more">
          查看更多 >
        </el-link>
      </div>
    </div>
    
    <div v-loading="loading" class="product-list">
      <div v-if="products.length > 0" class="product-grid">
        <div class="product-item" v-for="item in products.slice(0, 8)" :key="item.id" 
             @click="$router.push({ path: '/product/detail', query: { id: item.id } })">
          <div class="product-card">
            <div class="product-image">
              <img 
                :src="getProductImageUrl(item.mainImage)" 
                :alt="item.name"
                @error="handleImageError"
              />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ item.name }}</h3>
              <div class="product-price">¥{{ formatPrice(item.price) }}</div>
              <div class="product-desc">{{ getDescription(item) }}</div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无商品" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-section {
  width: 100%;
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 30px;
  text-align: center;
}

.section-title-wrapper {
  display: inline-block;
  position: relative;
  padding: 0 80px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
  line-height: 1.2;
  display: inline-block;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  border-radius: 2px;
}

.view-more {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-more:hover {
  color: #ff5500 !important;
  transform: translateY(-50%) translateX(5px);
}

.product-list {
  width: 100%;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-item:hover {
  transform: translateY(-8px);
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.product-item:hover .product-card {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #e0e0e0;
}

.product-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  display: -moz-box;
  display: box;
  line-clamp: 2;
  box-orient: vertical;
}

.product-price {
  color: #e4393c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.product-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-top: auto;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  display: -moz-box;
  display: box;
  line-clamp: 2;
  box-orient: vertical;
}

.empty-state {
  text-align: center;
  padding: 50px 20px;
  background: #f9f9f9;
  border-radius: 12px;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
}

/* 响应式调整 */
@media (max-width: 1500px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 15px;
  }
  
  .product-image {
    height: 160px;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .section-header {
    margin-bottom: 25px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .section-title-wrapper {
    padding: 0 70px;
  }
  
  .view-more {
    font-size: 13px;
  }
  
  .product-image {
    height: 140px;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-size: 14px;
    height: 38px;
    margin-bottom: 10px;
  }
  
  .product-price {
    font-size: 17px;
    margin-bottom: 8px;
  }
  
  .product-desc {
    font-size: 12px;
    padding-top: 10px;
    height: 36px;
  }
}

@media (max-width: 600px) {
  .section-title-wrapper {
    padding: 0 60px;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .section-title-wrapper {
    padding: 0 50px;
  }
  
  .view-more {
    font-size: 12px;
    display: none; /* 在小屏幕上隐藏"查看更多" */
  }
  
  .product-image {
    height: 120px;
  }
  
  .product-info {
    padding: 12px;
  }
  
  .product-name {
    font-size: 13px;
    height: 36px;
    margin-bottom: 8px;
  }
  
  .product-price {
    font-size: 16px;
    margin-bottom: 6px;
  }
  
  .product-desc {
    font-size: 11px;
    padding-top: 8px;
    height: 34px;
  }
}

@media (max-width: 360px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .product-image {
    height: 150px;
  }
  
  .section-title-wrapper {
    padding: 0 20px;
  }
  
  .view-more {
    display: none;
  }
}
</style>