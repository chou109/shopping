<template>
  <div class="favorite-shops">
    <!-- 页面头部 -->
    <div class="page-header">
      <span class="count">共关注 {{ favoriteList.length }} 家店铺</span>
    </div>

    <!-- 店铺列表 -->
    <div class="shops-container" v-loading="loading">
      <div v-if="favoriteList.length === 0" class="empty-state">
        <el-empty description="暂无关注的店铺">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-else class="shop-list">
        <article v-for="item in favoriteList" :key="item.favoriteId" class="shop-card">
          <router-link class="shop-card-link" :to="{ path: '/shop', query: { id: item.targetId } }"
            :aria-label="`查看店铺：${item.shopName || '未知店铺'}`">
            <div class="shop-logo">
              <img :src="getShopLogoUrl(item.shopLogo)" :alt="item.shopName" @error="handleLogoError" />
            </div>
            <div class="shop-info">
              <div class="shop-name">{{ item.shopName || '未知店铺' }}</div>
              <div class="shop-meta">
                <span class="followers">
                  <el-icon>
                    <User />
                  </el-icon>
                  {{ formatNumber(item.shopFollowers) }} 粉丝
                </span>
                <span class="rating" v-if="item.shopRating">
                  <el-icon>
                    <Star />
                  </el-icon>
                  {{ item.shopRating }}
                </span>
              </div>
              <div class="favorite-time">
                关注于 {{ formatTime(item.createTime) }}
              </div>
            </div>
          </router-link>
          <div class="shop-actions">
            <el-button type="danger" size="small" plain @click="handleRemoveFavorite(item)">
              取消关注
            </el-button>
            <el-button type="primary" size="small" @click="goShopDetail(item.targetId)">
              进入店铺
            </el-button>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Star } from '@element-plus/icons-vue'
import { getMyShopFavorites, removeFavorite } from '@/api/system/favorite'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const favoriteList = ref([])

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyShopFavorites()
    if (res.code === 200) {
      favoriteList.value = res.data || []
    }
  } catch (e) {
    console.error('加载收藏列表失败', e)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 取消关注
const handleRemoveFavorite = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消关注该店铺吗？', '取消关注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await removeFavorite(2, item.targetId) // 2表示店铺
    if (res.code === 200) {
      ElMessage.success('已取消关注')
      // 从列表中移除
      favoriteList.value = favoriteList.value.filter(f => f.favoriteId !== item.favoriteId)
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 获取店铺Logo URL
const getShopLogoUrl = (logo) => {
  if (!logo) return '/default-shop.png'
  if (logo.startsWith('http')) return logo
  return import.meta.env.VITE_APP_BASE_API + logo
}

// Logo加载失败处理
const handleLogoError = (e) => {
  e.target.src = '/default-shop.png'
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleDateString('zh-CN')
  } catch (e) {
    return time
  }
}

// 跳转店铺详情
const goShopDetail = (shopId) => {
  router.push({
    path: '/shop',
    query: { id: shopId }
  })
}

// 去购物
const goShopping = () => {
  router.push('/index')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorite-shops {
  padding: 0;
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  position: sticky;
  top: 70px;
  z-index: 10;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.page-header .count {
  font-size: 14px;
  color: #999;
}

.shops-container {
  min-height: 400px;
}

.empty-state {
  background: transparent;
  padding: 60px 20px;
  border-radius: 8px;
  text-align: center;
}

.shop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.shop-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.shop-card-link {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
  color: inherit;
  text-decoration: none;
  border-radius: 4px;
}

.shop-card-link:focus-visible {
  outline: 3px solid #ee0a24;
  outline-offset: 4px;
}

.shop-logo {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.shop-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-info {
  flex: 1;
  min-width: 0;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 4px;
}

.shop-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.shop-meta .el-icon {
  font-size: 14px;
}

.shop-meta .rating {
  color: #ff9500;
}

.favorite-time {
  font-size: 12px;
  color: #bbb;
}

.shop-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 600px) {
  .shop-card {
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px;
  }

  .shop-card-link {
    flex: 1 1 100%;
  }

  .shop-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
