<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from "@/utils/request.js"
import { getCategory } from '@/api/system/category'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const route = useRoute()
const keyword = ref('')
const categoryId = ref('')
const categoryName = ref('')
const products = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 获取分类名称
const loadCategoryName = async () => {
  if (!categoryId.value) {
    categoryName.value = ''
    return
  }
  
  try {
    const response = await getCategory(categoryId.value)
    console.log('分类详情响应:', response)
    
    if (response.code === 200 && response.data) {
      categoryName.value = response.data.name || `ID: ${categoryId.value}`
    } else {
      categoryName.value = `ID: ${categoryId.value}`
    }
  } catch (error) {
    console.error('获取分类名称失败:', error)
    categoryName.value = `ID: ${categoryId.value}`
  }
}

const loadSearchResults = () => {
  loading.value = true
  
  if (categoryId.value) {
    loadCategoryName()
  } else {
    categoryName.value = ''
  }
  
  if (categoryId.value) {
    api({
      url: `/system/product/category/${categoryId.value}`,
      method: 'get'
    })
      .then((res) => {
        console.log('分类查询响应:', res)
        
        if (res.code === 200) {
          products.value = res.data || []
          total.value = products.value.length
        } else {
          products.value = []
          total.value = 0
        }
      })
      .catch(() => {
        products.value = []
        total.value = 0
      })
      .finally(() => {
        loading.value = false
      })
  }
  else if (keyword.value) {
    api({
      url: '/system/product/search',
      method: 'get',
      params: {
        keyword: keyword.value
      }
    })
      .then((res) => {
        console.log('关键词搜索响应:', res)
        
        if (res.code === 200) {
          products.value = res.data || []
          total.value = products.value.length
        } else {
          products.value = []
          total.value = 0
        }
      })
      .catch(() => {
        products.value = []
        total.value = 0
      })
      .finally(() => {
        loading.value = false
      })
  } else {
    loading.value = false
    products.value = []
    total.value = 0
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadSearchResults()
}

// 计算显示标题
const pageTitle = computed(() => {
  if (keyword.value) {
    return `搜索关键词: "${keyword.value}"`
  } else if (categoryName.value) {
    return `分类: ${categoryName.value}`
  } else if (categoryId.value) {
    return `分类ID: ${categoryId.value}`
  } else {
    return '请输入搜索条件'
  }
})

// 计算空状态描述
const emptyDescription = computed(() => {
  if (keyword.value) {
    return `没有找到与'${keyword.value}'相关的商品`
  } else if (categoryName.value) {
    return `分类【${categoryName.value}】下暂无商品`
  } else if (categoryId.value) {
    return `分类ID ${categoryId.value} 下暂无商品`
  } else {
    return '请输入搜索条件'
  }
})

onMounted(() => {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId || ''
  
  console.log('页面加载，参数:', {
    keyword: keyword.value,
    categoryId: categoryId.value
  })
  
  if (keyword.value || categoryId.value) {
    loadSearchResults()
  }
})

watch(() => route.query, (newQuery) => {
  keyword.value = newQuery.keyword || ''
  categoryId.value = newQuery.categoryId || ''
  currentPage.value = 1
  categoryName.value = ''
  
  console.log('路由参数变化:', newQuery)
  
  if (keyword.value || categoryId.value) {
    loadSearchResults()
  } else {
    products.value = []
    total.value = 0
  }
})
</script>

<template>
  <div class="search-container">
    <!-- 添加的面包屑导航 -->
    <div class="breadcrumb-nav">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="keyword">搜索"{{ keyword }}"</el-breadcrumb-item>
        <el-breadcrumb-item v-else>商品列表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="search-header">
      <h2>搜索结果</h2>
      <p>{{ pageTitle }}</p>
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
                <img :src="getProductImageUrl(item.mainImage)" :alt="item.name" @error="handleImageError" />
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ item.name }}</h3>
                <div class="product-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
                <div class="product-desc">{{ item.subTitle }}</div>
                <div class="product-category">分类: {{ item.categoryName || item.category?.name || '未分类' }}</div>
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
        <el-empty :description="emptyDescription" />
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

<style scoped>
/* 添加面包屑导航样式 */
.breadcrumb-nav {
  margin-bottom: 20px;
  padding: 0 10px;
}

.breadcrumb-nav .el-breadcrumb {
  font-size: 14px;
}

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
  line-clamp: 2;
  -webkit-line-clamp: 2;
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
  line-clamp: 2;
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