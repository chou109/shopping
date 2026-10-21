<template>
  <div class="user-footprint">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="page-actions">
        <el-button type="danger" plain :disabled="!footprints || footprints.length === 0" @click="handleClearAll">
          清空全部足迹
        </el-button>
      </div>
    </div>

    <!-- 分类筛选选项卡 -->
    <div class="filter-tabs">
      <el-radio-group v-model="activeFilter" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="shop">店铺</el-radio-button>
        <el-radio-button label="product">商品</el-radio-button>
      </el-radio-group>

      <!-- 统计信息 -->
      <div class="filter-stats">
        <span class="stat-item">共 {{ totalCount }} 条记录</span>
        <span class="stat-item" v-if="activeFilter === 'shop'">店铺: {{ totalCount }} 个</span>
        <span class="stat-item" v-if="activeFilter === 'product'">商品: {{ totalCount }} 个</span>
      </div>
    </div>

    <!-- 足迹列表 -->
    <div class="footprint-list" ref="footprintListRef" v-loading="loading">
      <!-- 先检查 footprints 是否存在且有数据 -->
      <div v-if="filteredFootprints && filteredFootprints.length > 0" class="footprint-container">
        <!-- 再检查 groupedFootprints 是否存在且有数据 -->
        <template v-if="groupedFootprints && Object.keys(groupedFootprints).length > 0">
          <div v-for="(group, date) in groupedFootprints" :key="date" class="footprint-group">
            <div class="group-header">
              <div class="group-date">{{ formatDate(date) }}</div>
              <div class="group-count">共 {{ group.length }} 条记录</div>
            </div>
            <div class="footprint-items">
              <div v-for="item in group" :key="item.id" class="footprint-item"
                :class="{ 'shop-item': item.targetType === 2, 'product-item': item.targetType === 1 }">
                <!-- 图片区域 -->
                <div class="item-image" role="button" tabindex="0" :aria-label="`查看足迹：${item.targetName || '商品或店铺'}`"
                  @click="handleItemClick(item)" @keydown.enter.prevent="handleItemClick(item)"
                  @keydown.space.prevent="handleItemClick(item)">
                  <img :src="getProductImageUrl(item.targetImage)" :alt="item.targetName || '浏览记录图片'" @error="handleImageError">
                  <!-- 类型标签 -->
                  <div class="type-badge" :class="getTypeBadgeClass(item.targetType)">
                    {{ getTypeText(item.targetType) }}
                  </div>
                </div>

                <!-- 信息区域 -->
                <div class="item-info">
                  <button type="button" class="item-title" :aria-label="`查看足迹：${item.targetName || '商品或店铺'}`" @click="handleItemClick(item)">
                    {{ item.targetName }}
                  </button>
                  <div class="item-meta">
                    <span class="item-time">{{ formatTime(item.lastBrowseTime) }}</span>
                    <span class="item-count">浏览 {{ item.browseCount }} 次</span>
                  </div>
                  <div v-if="item.price && item.targetType === 1" class="item-price">
                    ¥{{ formatPrice(item.price) }}
                  </div>
                  <div v-if="item.targetType === 2" class="shop-info">
                    <span class="shop-tag">店铺</span>
                    <span class="follower-count" v-if="item.followerCount">粉丝: {{ item.followerCount }}</span>
                  </div>
                </div>

                <!-- 操作区域 -->
                <div class="item-actions">
                  <el-button type="danger" text size="small" @click="handleDelete(item.id)">
                    删除
                  </el-button>
                  <el-button v-if="item.targetType === 1" type="primary" text size="small"
                    @click="handleAddToCart(item)">
                    加入购物车
                  </el-button>
                  <el-button v-if="item.targetType === 2" type="success" text size="small"
                    @click="handleFollowShop(item)">
                    关注店铺
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="no-data">
          <p>暂无分组数据</p>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty :description="getEmptyDescription()">
          <el-button type="primary" @click="handleEmptyAction">
            {{ getEmptyButtonText() }}
          </el-button>
        </el-empty>
      </div>
      
      <!-- 加载更多提示 -->
      <div v-if="hasMore && displayedFootprints.length > 0" class="load-more-tip">
        <span v-if="loadingMore">加载中...</span>
        <span v-else>下滑加载更多</span>
      </div>
      <div v-if="!hasMore && displayedFootprints.length > 0" class="no-more-tip">
        已加载全部
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted, inject, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { listFootprintByUser, deleteFootprint, clearFootprint } from '@/api/system/footprint'
import { addCart } from '@/api/system/cart'
import { addFavorite, checkFavorite } from '@/api/system/favorite'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const footprints = ref([])
const activeFilter = ref('all') // 当前激活的筛选类型：all, shop, product

// 懒加载相关
const footprintListRef = ref(null)
const pageSize = 10
const loadingMore = ref(false)
const pageNum = ref(1)
const totalCount = ref(0)
let footprintRequestId = 0

// 注入父组件的滚动容器引用
const scrollContainer = inject('scrollContainer', null)

// 处理商品点击
const handleProductClick = (productId) => {
  router.push({
    path: '/product/detail',
    query: { id: productId }
  })
}

// 处理足迹项点击
const handleItemClick = (item) => {
  if (item.targetType === 1) { // 商品
    router.push({
      path: '/product/detail',
      query: { id: item.targetId }
    })
  } else if (item.targetType === 2) { // 店铺
    router.push({
      path: '/shop',
      query: { id: item.targetId }
    })
  }
}

// 筛选后的足迹数据
const filteredFootprints = computed(() => {
  return footprints.value
})

// 当前已从服务端分页加载的足迹
const displayedFootprints = computed(() => {
  return filteredFootprints.value
})

// 是否还有更多
const hasMore = computed(() => {
  return footprints.value.length < totalCount.value
})

// 加载更多
const loadMore = () => {
  if (loadingMore.value || loading.value || !hasMore.value) return
  loadFootprints(false)
}

// 滚动加载更多（监听父容器 .user-content 的滚动事件）
const handleScroll = () => {
  if (!scrollContainer || !scrollContainer.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer.value
  
  // 距离底部 200px 时加载更多
  if (scrollHeight - scrollTop - clientHeight < 200 && hasMore.value && !loadingMore.value) {
    loadMore()
  }
}

// 按日期分组（使用懒加载后的数据）
const groupedFootprints = computed(() => {
  if (!displayedFootprints.value || displayedFootprints.value.length === 0) {
    return {}
  }

  const groups = {}

  displayedFootprints.value.forEach(item => {
    if (!item || !item.lastBrowseTime) {
      return
    }

    try {
      const date = new Date(item.lastBrowseTime).toDateString()

      if (!groups[date]) {
        groups[date] = []
      }
      groups[date].push(item)
    } catch {
      // 无效日期不加入分组，保留其它可显示的记录。
    }
  })

  return groups
})

// 处理筛选变化
const handleFilterChange = (filterType) => {
  activeFilter.value = filterType
  loadFootprints(true)
}

// 获取类型徽章样式
const getTypeBadgeClass = (type) => {
  const classMap = {
    1: 'product-badge',
    2: 'shop-badge',
    3: 'activity-badge'
  }
  return classMap[type] || 'default-badge'
}

// 空状态描述
const getEmptyDescription = () => {
  const map = {
    all: '暂无浏览足迹',
    shop: '暂无店铺浏览记录',
    product: '暂无商品浏览记录'
  }
  return map[activeFilter.value] || '暂无数据'
}

// 空状态按钮文字
const getEmptyButtonText = () => {
  const map = {
    all: '去首页逛逛',
    shop: '浏览热门店铺',
    product: '发现热门商品'
  }
  return map[activeFilter.value] || '去首页逛逛'
}

// 空状态按钮动作
const handleEmptyAction = () => {
  if (activeFilter.value === 'shop') {
    router.push('/shops')
  } else if (activeFilter.value === 'product') {
    router.push('/products')
  } else {
    router.push('/')
  }
}

// 加入购物车
const handleAddToCart = async (item) => {
  if (item.targetType !== 1) return

  try {
    const cartData = {
      productId: item.targetId,
      productName: item.targetName,
      productMainImage: item.targetImage,
      quantity: 1,
      productUnitPrice: item.price || 0,
      selected: 1
    }

    const response = await addCart(cartData)
    if (response.code === 200) {
      ElMessage.success('已加入购物车')
    } else {
      ElMessage.error(response.msg || '加入购物车失败')
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 关注店铺
const handleFollowShop = async (item) => {
  if (item.targetType !== 2) return
  try {
    const status = await checkFavorite(2, item.targetId)
    if (status.code === 200 && (status.data === true || status.data?.isFavorite)) {
      ElMessage.info('已经关注过该店铺')
      return
    }
    const response = await addFavorite(2, item.targetId)
    if (response.code !== 200) throw new Error(response.msg || '关注失败')
    ElMessage.success('已关注店铺')
  } catch (error) {
    ElMessage.error(error.message || '关注店铺失败')
  }
}

// 其他辅助函数保持不变
const formatDate = (dateString) => {
  try {
    const date = new Date(dateString)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    if (date.toDateString() === today.toDateString()) {
      return '今天'
    } else if (date.toDateString() === yesterday.toDateString()) {
      return '昨天'
    } else {
      return `${date.getMonth() + 1}月${date.getDate()}日`
    }
  } catch (error) {
    console.error('格式化日期出错:', error)
    return dateString
  }
}

const formatTime = (timeString) => {
  try {
    const date = new Date(timeString)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (error) {
    console.error('格式化时间出错:', error)
    return timeString
  }
}

const formatPrice = (price) => {
  try {
    return Number(price).toFixed(2)
  } catch (error) {
    console.error('格式化价格出错:', error)
    return price
  }
}

const getTypeText = (type) => {
  const typeMap = {
    1: '商品',
    2: '店铺',
    3: '活动'
  }
  return typeMap[type] || '未知'
}

// 加载足迹数据
const loadFootprints = async (reset = true) => {
  const userId = store.state.user?.userId

  if (!userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (reset) {
    pageNum.value = 1
    totalCount.value = 0
    footprints.value = []
    loading.value = true
  } else {
    loadingMore.value = true
  }
  const requestId = ++footprintRequestId
  const requestedPageNum = pageNum.value
  try {
    const targetType = activeFilter.value === 'shop' ? 2 : activeFilter.value === 'product' ? 1 : undefined
    const response = await listFootprintByUser({
      pageNum: requestedPageNum,
      pageSize,
      targetType
    })
    if (requestId !== footprintRequestId) return
    if (response.code === 200) {
      const rows = response.rows || []
      footprints.value = reset ? rows : footprints.value.concat(rows)
      totalCount.value = Number(response.total) || 0
      pageNum.value += 1
    } else {
      ElMessage.error(response.msg || '获取足迹数据失败')
    }
  } catch (error) {
    if (requestId !== footprintRequestId) return
    console.error('加载足迹数据失败:', error)
    ElMessage.error('加载足迹数据失败')
  } finally {
    if (requestId === footprintRequestId) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

// 删除单个足迹
const handleDelete = async (footprintId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条足迹吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteFootprint(footprintId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载数据
      loadFootprints()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除足迹失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空全部足迹
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有足迹吗？此操作不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const userId = store.state.user?.userId
    if (!userId) {
      ElMessage.warning('用户未登录')
      return
    }

    const response = await clearFootprint()
    if (response.code === 200) {
      ElMessage.success('清空成功')
      footprints.value = []
      totalCount.value = 0
    } else {
      ElMessage.error(response.msg || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空足迹失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

onMounted(() => {
  loadFootprints()
  // 使用 nextTick 等待父容器准备完毕
  nextTick(() => {
    if (scrollContainer && scrollContainer.value) {
      scrollContainer.value.addEventListener('scroll', handleScroll)
    }
  })
})

onUnmounted(() => {
  if (scrollContainer && scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.user-footprint {
  padding: 0;
  background: transparent;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 70px;
  background: white;
  z-index: 10;
}

/* 筛选选项卡样式 */
.filter-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  position: sticky;
  top: 130px;
  z-index: 9;
}

.filter-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.stat-item {
  padding: 4px 8px;
  background: transparent;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.footprint-list {
  min-height: 400px;
}

.footprint-container {
  max-width: 100%;
}

/* 加载更多提示 */
.load-more-tip,
.no-more-tip {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.load-more-tip {
  color: #409eff;
}

.footprint-group {
  margin-bottom: 30px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0 10px;
}

.group-date {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-left: 3px solid #ff5500;
  padding-left: 10px;
}

.group-count {
  font-size: 14px;
  color: #999;
}

.footprint-items {
  background: transparent;
  border-radius: 8px;
  padding: 15px;
}

.footprint-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: transparent;
  border-radius: 6px;
  margin-bottom: 10px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.footprint-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.footprint-item:last-child {
  margin-bottom: 0;
}

/* 商品和店铺的不同样式 */
.product-item {
  border-left-color: #409eff;
}

.shop-item {
  border-left-color: #67c23a;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
  cursor: pointer;
  position: relative;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.item-image:hover img {
  transform: scale(1.05);
}

/* 类型徽章 */
.type-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  color: white;
  font-weight: bold;
}

.product-badge {
  background: rgba(64, 158, 255, 0.9);
}

.shop-badge {
  background: rgba(103, 194, 58, 0.9);
}

.activity-badge {
  background: rgba(255, 153, 0, 0.9);
}

.default-badge {
  background: rgba(102, 102, 102, 0.9);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  display: block;
  padding: 0;
  border: 0;
  background: none;
  text-align: left;
  font: inherit;
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-title:hover {
  color: #ff5500;
}

.item-title:focus-visible {
  outline: 2px solid #ff5500;
  outline-offset: 3px;
  border-radius: 2px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;
}

.item-price {
  font-size: 16px;
  color: #ff5500;
  font-weight: 600;
  margin-top: 5px;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.shop-tag {
  background: #67c23a;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.follower-count {
  font-size: 12px;
  color: #666;
}

.item-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .filter-tabs {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .filter-stats {
    align-self: stretch;
    justify-content: space-between;
  }

  .footprint-item {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }

  .item-image {
    width: 100%;
    height: 150px;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .item-info {
    width: 100%;
    margin-bottom: 10px;
  }

  .item-meta {
    flex-wrap: wrap;
    gap: 8px;
  }

  .item-actions {
    align-self: stretch;
    flex-direction: row;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .filter-stats {
    flex-direction: column;
    gap: 8px;
  }

  .stat-item {
    align-self: flex-start;
  }

  .item-actions {
    flex-direction: column;
  }

  .item-actions .el-button {
    width: 100%;
  }
}
</style>
