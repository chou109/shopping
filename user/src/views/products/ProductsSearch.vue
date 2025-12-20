<template>
  <div class="search-container">
    <div class="search-header">
      <h2>搜索结果</h2>
      <p v-if="keyword">搜索关键词: "{{ keyword }}"</p>
    </div>
    
    <div v-loading="loading" class="product-list">
      <div v-if="products.length > 0">
        <div class="search-stats">
          <span>找到 {{ total }} 个相关商品</span>
        </div>
        
        <div class="product-grid">
          <div class="product-item" v-for="item in products" :key="item.id" 
               @click="$router.push({ path: '/product/detail', query: { id: item.id } })">
            <div class="product-card">
              <div class="product-image">
                <img :src="getImageUrl(item.mainImage)" :alt="item.name" @error="handleImageError" />
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ item.name }}</h3>
                <div class="product-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
                <div class="product-desc">{{ item.subTitle }}</div>
                <div class="product-category">分类: {{ item.categoryName }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页组件 -->
        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
      <div v-else class="empty-state">
        <el-empty :description="`没有找到与'${keyword}'相关的商品`" />
        <div class="suggestions">
          <p>建议：</p>
          <ul>
            <li>检查输入的关键词是否正确</li>
            <li>尝试其他相关关键词</li>
            <li>浏览分类目录</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from "@/utils/request.js"

const route = useRoute()
const keyword = ref('')
const products = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const loadSearchResults = () => {
  loading.value = true
  api({
    url: '/api/products/search',
    method: 'get',
    params: {
      keyword: keyword.value,
      page: currentPage.value,
      size: pageSize.value
    }
  })
    .then((res) => {
      products.value = res.data?.rows || res.data || []
      total.value = res.data?.total || 0
    })
    .catch(() => {
      products.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadSearchResults()
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return `/api/${imagePath}`
}

const handleImageError = (event) => {
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNlZWVlZWUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSIjYWFhIj5ObyBJbWFnZTwvdGV4dD48L3N2Zz4='
}

onMounted(() => {
  keyword.value = route.query.keyword || ''
  if (keyword.value) {
    loadSearchResults()
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  keyword.value = newKeyword || ''
  currentPage.value = 1
  if (keyword.value) {
    loadSearchResults()
  }
})
</script>

<style scoped>
.search-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 30px;
  text-align: center;
}

.search-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.search-header p {
  color: #666;
  font-size: 16px;
}

.search-stats {
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.search-stats span {
  font-size: 14px;
  color: #666;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.product-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-item:hover {
  transform: translateY(-5px);
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

.product-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 8px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; /* 标准写法 */
  -webkit-box-orient: vertical;
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
  margin-bottom: 8px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; /* 标准写法 */
  -webkit-box-orient: vertical;
}

.product-category {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.empty-state {
  text-align: center;
  padding: 50px 20px;
}

.suggestions {
  margin-top: 30px;
  text-align: left;
  max-width: 500px;
  margin: 30px auto 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.suggestions p {
  font-weight: bold;
  margin-bottom: 10px;
}

.suggestions ul {
  list-style-type: none;
  padding-left: 0;
}

.suggestions li {
  padding: 5px 0;
  color: #666;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .search-container {
    padding: 15px;
  }
  
  .product-image {
    height: 160px;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .search-container {
    padding: 12px;
  }
  
  .search-header h2 {
    font-size: 20px;
  }
  
  .search-header p {
    font-size: 14px;
  }
  
  .product-image {
    height: 200px;
  }
}
</style>