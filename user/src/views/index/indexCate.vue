<template>
  <div class="category-container">
    <div class="category-sidebar" v-loading="loading">
      <div class="category-title" @click="toggleMobileMenu">
        <span>商品分类</span>
        <span class="mobile-toggle" v-if="isMobile">
          <i :class="menuExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
        </span>
      </div>
      
      <div class="category-content" :class="{ 'expanded': menuExpanded }">
        <ul class="category-menu">
          <li v-for="category in categories" :key="category.id" class="category-item">
            <div class="category-main" @click="gotoCategory(category.id)">
              <span class="category-name">{{ category.name }}</span>
              <span class="category-icon" v-if="!isMobile && category.children?.length">›</span>
            </div>
            
            <!-- 桌面端显示子菜单 -->
            <div v-if="category.children?.length && !isMobile" class="category-submenu">
              <div class="submenu-content">
                <div v-for="subCategory in category.children" 
                     :key="subCategory.id" 
                     class="subcategory-item"
                     @click="gotoCategory(subCategory.id)">
                  {{ subCategory.name }}
                </div>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { listCategory } from '@/api/system/category';

const router = useRouter()
const categories = ref([])
const loading = ref(false)
const isMobile = ref(false)
const menuExpanded = ref(false)

// 检测是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth < 992
}

const loadCategories = async function() {
  loading.value = true
  
  try {
    // 使用封装好的API
    const response = await listCategory({
      parentId: 0,
      status: 1
    })
    
    // 根据实际API响应结构调整
    categories.value = response?.rows || response || []
    
    // 如果API没有数据，使用默认数据
    if (categories.value.length === 0) {
      categories.value = getDefaultCategories()
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    // API失败时使用默认数据
    categories.value = getDefaultCategories()
  } finally {
    loading.value = false
  }
}

// 获取默认分类数据
const getDefaultCategories = () => {
  return [
    { id: 1, name: '手机数码', children: [] },
    { id: 2, name: '电脑办公', children: [] },
    { id: 3, name: '家用电器', children: [] },
    { id: 4, name: '服装服饰', children: [] },
    { id: 5, name: '美妆护肤', children: [] }
  ]
}

const toggleMobileMenu = () => {
  if (isMobile.value) {
    menuExpanded.value = !menuExpanded.value
  }
}

const gotoCategory = function(categoryId) {
  router.push({ 
    path: '/products', 
    query: { categoryId } 
  })
}

onMounted(() => {
  loadCategories()
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
/* 保持原有样式不变 */
.category-container {
  width: 100%;
}

.category-sidebar {
  width: 100%;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
}

.category-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mobile-toggle {
  cursor: pointer;
  font-size: 14px;
  color: white;
}

.category-content {
  max-height: 500px;
  overflow-y: auto;
}

.category-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  position: relative;
  border-bottom: 1px solid #f5f5f5;
}

.category-item:last-child {
  border-bottom: none;
}

.category-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  padding: 0 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-main:hover {
  background-color: #f5f5f5;
}

.category-name {
  font-size: 14px;
  color: #333;
}

.category-icon {
  font-size: 16px;
  color: #999;
  transform: rotate(90deg);
}

/* 桌面端子菜单 */
.category-submenu {
  position: absolute;
  left: 100%;
  top: 0;
  width: 300px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: none;
  padding: 15px;
}

.category-item:hover .category-submenu {
  display: block;
}

.submenu-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.subcategory-item {
  padding: 8px 12px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.subcategory-item:hover {
  background-color: #f0f9ff;
  color: #1890ff;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .category-submenu {
    width: 250px;
  }
}

@media (max-width: 992px) {
  .category-sidebar {
    border-radius: 6px;
  }
  
  .category-title {
    height: 45px;
    line-height: 45px;
    padding: 0 15px;
    font-size: 15px;
    cursor: pointer;
  }
  
  .mobile-toggle {
    display: block;
  }
  
  .category-main {
    height: 45px;
    padding: 0 15px;
  }
  
  .category-name {
    font-size: 13px;
  }
  
  .category-icon {
    display: none;
  }
  
  .category-submenu {
    display: none !important;
  }
  
  /* 移动端展开/收起功能 */
  .category-content {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease;
  }
  
  .category-content.expanded {
    max-height: 500px;
  }
}

@media (max-width: 768px) {
  .category-title {
    height: 42px;
    line-height: 42px;
    padding: 0 12px;
    font-size: 14px;
  }
  
  .category-main {
    height: 42px;
    padding: 0 12px;
  }
  
  .category-name {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .category-sidebar {
    border-radius: 4px;
  }
  
  .category-title {
    height: 40px;
    line-height: 40px;
    padding: 0 10px;
    font-size: 14px;
  }
  
  .category-main {
    height: 40px;
    padding: 0 10px;
  }
  
  .category-name {
    font-size: 13px;
  }
}
</style>