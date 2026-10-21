<template>
  <div class="user-reviews">
    <div class="page-header">
      <h2>我的评价</h2>
      <p class="subtitle">查看您对商品的所有评价</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="loading-icon">
        <Loading />
      </el-icon>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <div v-else-if="reviews.length === 0" class="empty-container">
      <el-empty description="暂无评价记录">
        <el-button type="primary" @click="goShopping">去购物</el-button>
      </el-empty>
    </div>

    <!-- 评价列表 -->
    <div v-else class="reviews-list">
      <div v-for="review in reviews" :key="review.commentId" class="review-card">
        <!-- 商品信息 -->
        <router-link class="product-info" :to="{ path: '/product/detail', query: { id: review.productId } }" :aria-label="`查看商品：${review.productName || '商品'}`">
          <img :src="getImageUrl(review.productImage)" alt="商品图片" class="product-image" />
          <div class="product-detail">
            <div class="product-name">{{ review.productName || '商品' }}</div>
            <div class="product-price">¥{{ review.productPrice || '-' }}</div>
          </div>
        </router-link>

        <!-- 评价内容 -->
        <div class="review-content">
          <div class="review-header">
            <div class="rating">
              <span v-for="n in 5" :key="n" class="star" :class="{ active: n <= review.score }">★</span>
              <span class="score-text">{{ getScoreText(review.score) }}</span>
            </div>
            <div class="review-time">{{ formatTime(review.createTime) }}</div>
          </div>

          <div class="review-text">{{ review.content }}</div>

          <!-- 评价图片 -->
          <div v-if="review.images && review.images.length > 0" class="review-images">
            <el-image v-for="(img, index) in parseImages(review.images)" :key="index" :src="getImageUrl(img)"
              :preview-src-list="parseImages(review.images).map(i => getImageUrl(i))" :initial-index="index" fit="cover"
              class="review-image" />
          </div>

          <!-- 商家回复 -->
          <div v-if="review.replyContent" class="merchant-reply">
            <div class="reply-label">商家回复：</div>
            <div class="reply-text">{{ review.replyContent }}</div>
          </div>

          <!-- 操作按钮 -->
          <div class="review-actions">
            <span class="like-count">
              <el-icon>
                <Pointer />
              </el-icon>
              {{ review.likeCount || 0 }} 人觉得有用
            </span>
            <el-popconfirm title="确定要删除这条评价吗？" @confirm="handleDelete(review.commentId)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { DEFAULT_PRODUCT_IMAGE } from '@/utils/image'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyComments, deleteComment } from '@/api/comment'
import { ElMessage } from 'element-plus'
import { Loading, Pointer } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const reviews = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})

// 获取评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await getMyComments(queryParams.value)
    reviews.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取评价失败:', error)
    ElMessage.error('获取评价失败')
  } finally {
    loading.value = false
  }
}

// 解析图片
const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    return JSON.parse(images)
  } catch {
    return images.split(',').filter(Boolean)
  }
}

// 获取图片URL
const getImageUrl = (url) => {
  if (!url) return DEFAULT_PRODUCT_IMAGE
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 评分文字
const getScoreText = (score) => {
  const texts = ['', '非常差', '差', '一般', '好', '非常好']
  return texts[score] || ''
}

// 去购物
const goShopping = () => {
  router.push('/index')
}

// 删除评价
const handleDelete = async (commentId) => {
  try {
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    fetchReviews()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 分页
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  fetchReviews()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  fetchReviews()
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.user-reviews {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.page-header .subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #999;
}

.loading-icon {
  font-size: 32px;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.empty-container {
  padding: 80px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.review-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.product-info {
  display: flex;
  color: inherit;
  text-decoration: none;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
  cursor: pointer;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
}

.product-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-name:hover {
  color: #409EFF;
}

.product-info:focus-visible {
  outline: 2px solid #409EFF;
  outline-offset: 3px;
  border-radius: 4px;
}

.product-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.review-content {
  /* 评价内容区域 */
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star {
  color: #ddd;
  font-size: 16px;
}

.star.active {
  color: #ffb800;
}

.score-text {
  font-size: 13px;
  color: #ff9500;
  margin-left: 8px;
}

.review-time {
  font-size: 13px;
  color: #999;
}

.review-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}

.merchant-reply {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.reply-label {
  font-size: 13px;
  color: #ff6b00;
  font-weight: 500;
  margin-bottom: 6px;
}

.reply-text {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.review-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.like-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
</style>
