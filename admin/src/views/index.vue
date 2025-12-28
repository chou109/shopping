<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <h1 class="page-title">{{ pageTitle }}</h1>
    
    <!-- 用户欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="hover" class="welcome-card">
        <div class="welcome-content">
          <div class="user-info">
            <el-avatar :size="64" :src="userInfo.avatar" class="user-avatar">
              {{ userInfo.name ? userInfo.name.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <h2 class="greeting">{{ getGreeting() }}，{{ userInfo.name }}，欢迎回来！</h2>
              <p class="user-role">{{ userInfo.position }} | {{ userInfo.dept }}</p>
              <p class="role-badge" v-if="statistics.role">
                <el-tag :type="statistics.role === 'admin' ? 'danger' : 'success'" size="small">
                  {{ statistics.role === 'admin' ? '超级管理员' : '商家' }}
                </el-tag>
                <span v-if="statistics.shopName" class="shop-name">{{ statistics.shopName }}</span>
              </p>
            </div>
          </div>
          <div class="quick-actions">
            <el-button type="primary" @click="handleAction('product')">
              <el-icon><ShoppingCart /></el-icon>
              商品管理
            </el-button>
            <el-button type="success" @click="handleAction('order')">
              <el-icon><List /></el-icon>
              订单管理
            </el-button>
            <el-button v-if="userStore.userId === 1" type="warning" @click="handleAction('shop')">
              <el-icon><Shop /></el-icon>
              店铺管理
            </el-button>
            <el-button v-if="userStore.userId === 1" type="info" @click="handleAction('user')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计数据卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in statsData" :key="stat.title">
          <el-card shadow="hover" class="stat-card" :loading="loading.stats">
            <div class="stat-content">
              <div class="stat-icon" :style="{ background: stat.color }">
                <el-icon :size="28">
                  <component :is="getIconComponent(stat.icon)" />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">{{ stat.title }}</div>
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-trend" :class="stat.trend > 0 ? 'trend-up' : 'trend-down'" v-if="stat.trend !== 0">
                  <el-icon v-if="stat.trend > 0"><Top /></el-icon>
                  <el-icon v-else><Bottom /></el-icon>
                  {{ Math.abs(stat.trend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <el-card shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>销售趋势</h3>
                <el-select v-model="timeRange" size="small" style="width: 120px" @change="loadSalesTrend">
                  <el-option label="本周" value="week" />
                  <el-option label="本月" value="month" />
                  <el-option label="本季" value="quarter" />
                  <el-option label="本年" value="year" />
                </el-select>
              </div>
            </template>
            <div ref="salesChart" class="chart-container" v-loading="loading.salesTrend"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card shadow="hover">
            <template #header>
              <h3>订单状态分布</h3>
            </template>
            <div ref="orderStatusChart" class="chart-container" v-loading="loading.orderStatus"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热门商品与最新订单 -->
    <div class="content-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>热门商品</h3>
                <el-button type="primary" link @click="gotoProducts">查看全部</el-button>
              </div>
            </template>
            <div class="products-list" v-loading="loading.hotProducts">
              <el-empty v-if="hotProducts.length === 0" description="暂无热门商品" />
              <div v-else v-for="product in hotProducts" :key="product.id" class="product-item">
                <div class="product-image">
                  <el-image :src="getProductImage(product.mainImage)" fit="cover" style="width: 60px; height: 60px; border-radius: 8px;">
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-price">¥{{ product.price }}</div>
                </div>
                <div class="product-stats">
                  <el-tag type="success" size="small">库存: {{ product.stock }}</el-tag>
                  <el-tag v-if="product.hotFlag === 1" type="danger" size="small">热门</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>最新订单</h3>
                <el-button type="primary" link @click="gotoOrders">查看全部</el-button>
              </div>
            </template>
            <div class="orders-list" v-loading="loading.recentOrders">
              <el-empty v-if="recentOrders.length === 0" description="暂无订单" />
              <div v-else v-for="order in recentOrders" :key="order.id" class="order-item">
                <div class="order-info">
                  <div class="order-no">{{ order.orderNo }}</div>
                  <div class="order-time">{{ formatTime(order.createTime) }}</div>
                </div>
                <div class="order-amount">¥{{ order.payment }}</div>
                <div class="order-status">
                  <el-tag :type="getOrderStatusType(order.status)" size="small">
                    {{ order.statusDesc || getOrderStatusText(order.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分类统计 -->
    <div class="category-section">
      <el-card shadow="hover">
        <template #header>
          <h3>商品分类分布</h3>
        </template>
        <div ref="categoryChart" class="chart-container" v-loading="loading.categoryStats"></div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-section">
      <el-card shadow="hover">
        <template #header>
          <h3>快捷操作</h3>
        </template>
        <div class="quick-grid">
          <div v-for="item in quickActions" :key="item.name" class="quick-item" @click="handleQuickAction(item)">
            <div class="quick-icon" :style="{ background: item.color }">
              <el-icon :size="24">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="quick-name">{{ item.name }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  ShoppingCart, List, Shop, User, Top, Bottom,
  Plus, Edit, Setting, Picture, TrendCharts,
  Money, Star, Document, Upload, Download, Search
} from '@element-plus/icons-vue'
import {
  getStatistics,
  getStatsCards,
  getSalesTrend,
  getHotProducts,
  getRecentOrders,
  getCategoryStats,
  getOrderStatusDistribution
} from '@/api/system/dashboard'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = ref({
  stats: false,
  salesTrend: false,
  hotProducts: false,
  recentOrders: false,
  categoryStats: false,
  orderStatus: false
})

// 用户信息
const userInfo = computed(() => {
  return {
    name: userStore.name || '用户',
    avatar: userStore.avatar || '',
    position: userStore.roles?.includes('admin') ? '系统管理员' : '商家用户',
    dept: '运营部门'
  }
})

// 页面标题
const pageTitle = computed(() => {
  return statistics.value.role === 'admin' ? '管理员控制台' : '商家工作台'
})

// 统计数据
const statistics = ref({})
const statsData = ref([])

// 时间范围
const timeRange = ref('week')

// 图表实例
const salesChart = ref(null)
const orderStatusChart = ref(null)
const categoryChart = ref(null)
let salesChartInstance = null
let orderStatusChartInstance = null
let categoryChartInstance = null

// 数据
const hotProducts = ref([])
const recentOrders = ref([])
const categoryStats = ref([])
const orderStatusData = ref([])
const salesTrendData = ref({ dates: [], amounts: [], orderCounts: [] })

// 快捷操作
const quickActions = computed(() => {
  const baseActions = [
    { name: '新增商品', icon: Plus, color: '#1890ff', action: 'addProduct' },
    { name: '商品管理', icon: ShoppingCart, color: '#52c41a', action: 'productList' },
    { name: '订单管理', icon: List, color: '#faad14', action: 'orderList' },
    { name: '店铺设置', icon: Setting, color: '#722ed1', action: 'shopSetting' }
  ]
  
  if (statistics.value.role === 'admin') {
    baseActions.push(
      { name: '用户管理', icon: User, color: '#f5222d', action: 'userManage' },
      { name: '数据导出', icon: Download, color: '#13c2c2', action: 'exportData' }
    )
  }
  
  return baseActions
})

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
}

// 获取图标组件
const getIconComponent = (iconName) => {
  const iconMap = {
    'ShoppingCart': ShoppingCart,
    'List': List,
    'Money': Money,
    'Shop': Shop,
    'User': User,
    'TrendCharts': TrendCharts,
    'Star': Star
  }
  return iconMap[iconName] || ShoppingCart
}

// 获取商品图片
const getProductImage = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return import.meta.env.VITE_APP_BASE_API + imagePath
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const typeMap = {
    10: 'warning',  // 待付款
    20: 'primary',  // 已付款
    30: '',         // 待发货
    40: 'info',     // 已发货
    50: 'success',  // 已完成
    60: 'danger',   // 退款中
    70: 'info'      // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const textMap = {
    10: '待付款',
    20: '已付款',
    30: '待发货',
    40: '已发货',
    50: '已完成',
    60: '退款中',
    70: '已取消'
  }
  return textMap[status] || '未知'
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载统计卡片
const loadStatsCards = async () => {
  loading.value.stats = true
  try {
    const res = await getStatsCards()
    if (res.code === 200) {
      statsData.value = res.data || []
    }
  } catch (error) {
    console.error('获取统计卡片失败:', error)
  } finally {
    loading.value.stats = false
  }
}

// 加载销售趋势
const loadSalesTrend = async () => {
  loading.value.salesTrend = true
  try {
    const res = await getSalesTrend(timeRange.value)
    if (res.code === 200) {
      salesTrendData.value = res.data || { dates: [], amounts: [], orderCounts: [] }
      renderSalesChart()
    }
  } catch (error) {
    console.error('获取销售趋势失败:', error)
  } finally {
    loading.value.salesTrend = false
  }
}

// 加载热门商品
const loadHotProducts = async () => {
  loading.value.hotProducts = true
  try {
    const res = await getHotProducts(5)
    if (res.code === 200) {
      hotProducts.value = res.data || []
    }
  } catch (error) {
    console.error('获取热门商品失败:', error)
  } finally {
    loading.value.hotProducts = false
  }
}

// 加载最新订单
const loadRecentOrders = async () => {
  loading.value.recentOrders = true
  try {
    const res = await getRecentOrders(5)
    if (res.code === 200) {
      recentOrders.value = res.data || []
    }
  } catch (error) {
    console.error('获取最新订单失败:', error)
  } finally {
    loading.value.recentOrders = false
  }
}

// 加载分类统计
const loadCategoryStats = async () => {
  loading.value.categoryStats = true
  try {
    const res = await getCategoryStats()
    if (res.code === 200) {
      categoryStats.value = res.data || []
      renderCategoryChart()
    }
  } catch (error) {
    console.error('获取分类统计失败:', error)
  } finally {
    loading.value.categoryStats = false
  }
}

// 加载订单状态分布
const loadOrderStatus = async () => {
  loading.value.orderStatus = true
  try {
    const res = await getOrderStatusDistribution()
    if (res.code === 200) {
      orderStatusData.value = res.data || []
      renderOrderStatusChart()
    }
  } catch (error) {
    console.error('获取订单状态分布失败:', error)
  } finally {
    loading.value.orderStatus = false
  }
}

// 渲染销售图表
const renderSalesChart = () => {
  if (!salesChart.value) return
  
  if (!salesChartInstance) {
    salesChartInstance = echarts.init(salesChart.value)
  }
  
  const data = salesTrendData.value
  const dates = data.dates || []
  const amounts = data.amounts || []
  const orderCounts = data.orderCounts || []
  
  // 格式化日期显示
  const formattedDates = dates.map(d => {
    if (!d) return ''
    const date = new Date(d)
    return (date.getMonth() + 1) + '/' + date.getDate()
  })
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['销售额', '订单数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: formattedDates,
      axisLine: { lineStyle: { color: '#e8e8e8' } }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        axisLine: { lineStyle: { color: '#1890ff' } },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      {
        type: 'value',
        name: '订单数',
        axisLine: { lineStyle: { color: '#52c41a' } }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: amounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: orderCounts,
        smooth: true,
        itemStyle: { color: '#52c41a' }
      }
    ]
  }
  
  salesChartInstance.setOption(option)
}

// 渲染订单状态图表
const renderOrderStatusChart = () => {
  if (!orderStatusChart.value) return
  
  if (!orderStatusChartInstance) {
    orderStatusChartInstance = echarts.init(orderStatusChart.value)
  }
  
  const data = orderStatusData.value.map(item => ({
    name: item.name,
    value: item.value,
    itemStyle: { color: item.color }
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'right',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: '16', fontWeight: 'bold' }
        },
        data: data
      }
    ]
  }
  
  orderStatusChartInstance.setOption(option)
}

// 渲染分类图表
const renderCategoryChart = () => {
  if (!categoryChart.value) return
  
  if (!categoryChartInstance) {
    categoryChartInstance = echarts.init(categoryChart.value)
  }
  
  const names = categoryStats.value.map(item => item.categoryName)
  const values = categoryStats.value.map(item => item.productCount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: names.reverse(),
      axisLabel: {
        width: 80,
        overflow: 'truncate'
      }
    },
    series: [
      {
        type: 'bar',
        data: values.reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1890ff' },
            { offset: 1, color: '#36cfc9' }
          ])
        }
      }
    ]
  }
  
  categoryChartInstance.setOption(option)
}

// 处理操作
const handleAction = (type) => {
  const routes = {
    'product': '/shopping/product',
    'order': '/order/info',
    'shop': '/store/store',
  }
  if (routes[type]) {
    router.push(routes[type])
  }
}

// 处理快捷操作
const handleQuickAction = (item) => {
  const actions = {
    'addProduct': () => router.push('/system/product'),
    'productList': () => router.push('/system/product'),
    'orderList': () => router.push('/system/info'),
    'shopSetting': () => router.push('/system/store'),
    'userManage': () => router.push('/system/user'),
    'exportData': () => console.log('导出数据')
  }
  if (actions[item.action]) {
    actions[item.action]()
  }
}

// 跳转
const gotoProducts = () => router.push('/system/product')
const gotoOrders = () => router.push('/system/info')

// 窗口大小调整
const handleResize = () => {
  salesChartInstance?.resize()
  orderStatusChartInstance?.resize()
  categoryChartInstance?.resize()
}

// 加载所有数据
const loadAllData = async () => {
  await loadStatistics()
  loadStatsCards()
  loadSalesTrend()
  loadHotProducts()
  loadRecentOrders()
  loadCategoryStats()
  loadOrderStatus()
}

// 生命周期
onMounted(() => {
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  salesChartInstance?.dispose()
  orderStatusChartInstance?.dispose()
  categoryChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang='scss' scoped>
.dashboard-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background-color: #f0f2f5;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

// 欢迎区域样式
.welcome-section {
  margin-bottom: 20px;
  
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    :deep(.el-card__body) {
      padding: 0;
    }
    
    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30px;
      
      .user-info {
        display: flex;
        align-items: center;
        
        .user-avatar {
          margin-right: 20px;
          background: rgba(255, 255, 255, 0.2);
          font-size: 24px;
          color: white;
        }
        
        .user-details {
          color: white;
          
          .greeting {
            margin: 0 0 8px 0;
            font-size: 20px;
            font-weight: 500;
          }
          
          .user-role {
            margin: 0 0 8px 0;
            opacity: 0.9;
            font-size: 14px;
          }
          
          .role-badge {
            display: flex;
            align-items: center;
            gap: 10px;
            
            .shop-name {
              font-size: 14px;
              opacity: 0.9;
            }
          }
        }
      }
      
      .quick-actions {
        .el-button {
          margin-left: 10px;
          background: rgba(255, 255, 255, 0.1);
          border: 1px solid rgba(255, 255, 255, 0.2);
          color: white;
          
          &:hover {
            background: rgba(255, 255, 255, 0.2);
          }
        }
      }
    }
  }
}

// 统计数据区域
.stats-section {
  margin-bottom: 20px;
  
  .stat-card {
    height: 100%;
    
    .stat-content {
      display: flex;
      align-items: center;
      padding: 10px 0;
      
      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        color: white;
      }
      
      .stat-info {
        flex: 1;
        
        .stat-title {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }
        
        .stat-trend {
          display: flex;
          align-items: center;
          font-size: 12px;
          
          &.trend-up { color: #52c41a; }
          &.trend-down { color: #f5222d; }
          
          .el-icon { margin-right: 4px; }
        }
      }
    }
  }
}

// 图表区域
.charts-section, .category-section {
  margin-bottom: 20px;
  
  .el-card { height: 100%; }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .chart-container {
    height: 350px;
  }
}

h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

// 内容区域
.content-section {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .products-list {
    .product-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child { border-bottom: none; }
      
      .product-image {
        margin-right: 12px;
        
        .image-placeholder {
          width: 60px;
          height: 60px;
          background: #f5f5f5;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 8px;
          color: #999;
        }
      }
      
      .product-info {
        flex: 1;
        
        .product-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: 200px;
        }
        
        .product-price {
          font-size: 16px;
          color: #f5222d;
          font-weight: 600;
        }
      }
      
      .product-stats {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .orders-list {
    .order-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child { border-bottom: none; }
      
      .order-info {
        flex: 1;
        
        .order-no {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 4px;
        }
        
        .order-time {
          font-size: 12px;
          color: #999;
        }
      }
      
      .order-amount {
        font-size: 16px;
        color: #f5222d;
        font-weight: 600;
        margin-right: 16px;
      }
    }
  }
}

// 快捷操作区域
.quick-section {
  margin-bottom: 20px;
  
  .quick-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 20px;
    
    .quick-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateY(-4px);
        
        .quick-icon {
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
        }
      }
      
      .quick-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        color: white;
        transition: box-shadow 0.2s;
      }
      
      .quick-name {
        font-size: 14px;
        color: #333;
        text-align: center;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .welcome-content {
    flex-direction: column !important;
    align-items: flex-start !important;
    
    .quick-actions {
      margin-top: 16px;
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      
      .el-button {
        margin: 5px 5px 0 0 !important;
        flex: 1 1 calc(50% - 10px);
      }
    }
  }
  
  .stats-section .el-col {
    margin-bottom: 16px;
  }
  
  .charts-section .el-col {
    margin-bottom: 16px;
  }
  
  .content-section .el-col {
    margin-bottom: 16px;
  }
  
  .quick-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)) !important;
    gap: 16px !important;
  }
}
</style>
