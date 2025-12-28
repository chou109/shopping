<template>
    <div class="dashboard-container">
      <!-- 页面标题 -->
      <h1 class="page-title">首页概览</h1>
      
      <!-- 用户欢迎区域 -->
      <div class="welcome-section">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <div class="user-info">
              <el-avatar :size="64" :src="userInfo.avatar" class="user-avatar">
                {{ userInfo.name.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <h2 class="greeting">早上好，{{ userInfo.name }}，欢迎回来！</h2>
                <p class="user-role">{{ userInfo.position }} | {{ userInfo.dept }}</p>
              </div>
            </div>
            <div class="quick-actions">
              <el-button type="primary" @click="handleAction('message')">
                <el-icon><Message /></el-icon>
                消息中心
              </el-button>
              <el-button type="success" @click="handleAction('task')">
                <el-icon><List /></el-icon>
                待办任务
              </el-button>
              <el-button type="warning" @click="handleAction('schedule')">
                <el-icon><Calendar /></el-icon>
                日程安排
              </el-button>
              <el-button type="info" @click="handleAction('report')">
                <el-icon><Document /></el-icon>
                工作报告
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
  
      <!-- 统计数据卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" v-for="stat in statsData" :key="stat.title">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" :style="{ background: stat.color }">
                  <el-icon :size="28">
                    <component :is="stat.icon" />
                  </el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-title">{{ stat.title }}</div>
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-trend" :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
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
                  <el-select v-model="timeRange" size="small" style="width: 120px">
                    <el-option label="本周" value="week" />
                    <el-option label="本月" value="month" />
                    <el-option label="本季" value="quarter" />
                    <el-option label="本年" value="year" />
                  </el-select>
                </div>
              </template>
              <div ref="salesChart" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card shadow="hover">
              <template #header>
                <h3>访问来源分布</h3>
              </template>
              <div ref="sourceChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
  
      <!-- 项目与动态 -->
      <div class="content-section">
        <el-row :gutter="20">
          <el-col :xs="24" :lg="12">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <h3>项目进度</h3>
                  <el-button type="text" @click="gotoProjects">查看全部</el-button>
                </div>
              </template>
              <div class="projects-list">
                <div v-for="project in projects" :key="project.id" class="project-item">
                  <div class="project-info">
                    <div class="project-name">{{ project.name }}</div>
                    <div class="project-deadline">截止: {{ project.deadline }}</div>
                  </div>
                  <div class="project-progress">
                    <el-progress 
                      :percentage="project.progress" 
                      :color="project.color"
                      :show-text="false"
                    />
                    <span class="progress-text">{{ project.progress }}%</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card shadow="hover">
              <template #header>
                <h3>最新动态</h3>
              </template>
              <div class="activities-list">
                <div v-for="activity in activities" :key="activity.id" class="activity-item">
                  <div class="activity-icon" :class="`type-${activity.type}`">
                    <el-icon>
                      <component :is="activity.icon" />
                    </el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-text">
                      <span class="activity-user">{{ activity.user }}</span>
                      {{ activity.action }}
                      <span class="activity-target">{{ activity.target }}</span>
                    </div>
                    <div class="activity-time">{{ activity.time }}</div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
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
  import { ref, onMounted, onUnmounted, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import * as echarts from 'echarts'
  import {
    Message, List, Calendar, Document,
    User, ShoppingCart, Money, View,
    Top, Bottom, Plus, Edit, Delete,
    Search, Upload, Download, Setting
  } from '@element-plus/icons-vue'
  
  const router = useRouter()
  
  // 用户信息
  const userInfo = ref({
    name: '管理员',
    avatar: '',
    position: '系统管理员',
    dept: '技术部'
  })
  
  // 统计数据
  const statsData = ref([
    { title: '总用户数', value: '3,456', icon: User, color: 'linear-gradient(135deg, #1890ff, #36cfc9)', trend: 12.5 },
    { title: '订单数量', value: '2,189', icon: ShoppingCart, color: 'linear-gradient(135deg, #52c41a, #95de64)', trend: 8.3 },
    { title: '销售额', value: '¥128,560', icon: Money, color: 'linear-gradient(135deg, #faad14, #ffc53d)', trend: -3.2 },
    { title: '访问量', value: '56,789', icon: View, color: 'linear-gradient(135deg, #f5222d, #ff7875)', trend: 23.7 }
  ])
  
  // 时间范围
  const timeRange = ref('week')
  
  // 图表实例
  const salesChart = ref(null)
  const sourceChart = ref(null)
  let salesChartInstance = null
  let sourceChartInstance = null
  
  // 项目数据
  const projects = ref([
    { id: 1, name: '后台管理系统升级', progress: 85, color: '#1890ff', deadline: '2024-12-30' },
    { id: 2, name: '移动端开发', progress: 60, color: '#52c41a', deadline: '2024-12-15' },
    { id: 3, name: '数据迁移', progress: 45, color: '#faad14', deadline: '2024-12-10' },
    { id: 4, name: '安全审计', progress: 30, color: '#f5222d', deadline: '2024-12-05' }
  ])
  
  // 动态数据
  const activities = ref([
    { id: 1, user: '张三', action: '创建了新的项目', target: '客户关系管理系统', type: 'create', icon: Plus, time: '今天 08:30' },
    { id: 2, user: '李四', action: '提交了', target: '月度工作报告', type: 'update', icon: Edit, time: '今天 10:15' },
    { id: 3, user: '王五', action: '完成了', target: '系统升级任务', type: 'success', icon: List, time: '今天 14:20' },
    { id: 4, user: '赵六', action: '评论了', target: '产品设计方案', type: 'comment', icon: Message, time: '今天 16:45' },
    { id: 5, user: '孙七', action: '审批通过了', target: '采购申请', type: 'approve', icon: Document, time: '今天 18:30' }
  ])
  
  // 快捷操作
  const quickActions = ref([
    { name: '新建项目', icon: Plus, color: '#1890ff', action: 'createProject' },
    { name: '数据导入', icon: Upload, color: '#52c41a', action: 'importData' },
    { name: '数据导出', icon: Download, color: '#faad14', action: 'exportData' },
    { name: '系统设置', icon: Setting, color: '#722ed1', action: 'systemConfig' },
    { name: '用户管理', icon: User, color: '#f5222d', action: 'userManage' },
    { name: '权限配置', icon: Edit, color: '#13c2c2', action: 'permissionConfig' }
  ])
  
  // 初始化图表
  const initCharts = () => {
    // 销售图表
    if (salesChart.value) {
      salesChartInstance = echarts.init(salesChart.value)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
          axisLine: {
            lineStyle: {
              color: '#e8e8e8'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#e8e8e8'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        series: [{
          data: [120, 200, 150, 80, 70, 110, 130],
          type: 'bar',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' }
              ])
            }
          }
        }]
      }
      salesChartInstance.setOption(option)
    }
  
    // 来源图表
    if (sourceChart.value) {
      sourceChartInstance = echarts.init(sourceChart.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'right',
          top: 'center'
        },
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '20',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 1048, name: '直接访问' },
              { value: 735, name: '搜索引擎' },
              { value: 580, name: '社交媒体' },
              { value: 484, name: '邮件营销' }
            ],
            color: ['#1890ff', '#52c41a', '#faad14', '#f5222d']
          }
        ]
      }
      sourceChartInstance.setOption(option)
    }
  }
  
  // 处理操作
  const handleAction = (type) => {
    console.log('执行操作:', type)
    // 这里可以添加路由跳转或其他逻辑
  }
  
  // 处理快捷操作
  const handleQuickAction = (item) => {
    console.log('快捷操作:', item.action)
    // 这里可以添加相应的逻辑
  }
  
  // 跳转到项目页面
  const gotoProjects = () => {
    router.push('/project')
  }
  
  // 窗口大小调整
  const handleResize = () => {
    salesChartInstance?.resize()
    sourceChartInstance?.resize()
  }
  
  // 生命周期
  onMounted(() => {
    nextTick(() => {
      initCharts()
    })
    
    window.addEventListener('resize', handleResize)
  })
  
  onUnmounted(() => {
    salesChartInstance?.dispose()
    sourceChartInstance?.dispose()
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
      
      .welcome-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
        
        .user-info {
          display: flex;
          align-items: center;
          
          .user-avatar {
            margin-right: 20px;
            background: rgba(255, 255, 255, 0.2);
            font-size: 24px;
          }
          
          .user-details {
            color: white;
            
            .greeting {
              margin: 0 0 8px 0;
              font-size: 20px;
              font-weight: 500;
            }
            
            .user-role {
              margin: 0;
              opacity: 0.9;
              font-size: 14px;
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
          
          .el-icon {
            font-size: 28px;
          }
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
            
            &.trend-up {
              color: #52c41a;
            }
            
            &.trend-down {
              color: #f5222d;
            }
            
            .el-icon {
              margin-right: 4px;
            }
          }
        }
      }
    }
  }
  
  // 图表区域
  .charts-section {
    margin-bottom: 20px;
    
    .el-card {
      height: 100%;
    }
    
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
  
  // 内容区域
  .content-section {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
      }
    }
    
    .projects-list {
      .project-item {
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .project-info {
          margin-bottom: 12px;
          
          .project-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }
          
          .project-deadline {
            font-size: 12px;
            color: #999;
          }
        }
        
        .project-progress {
          display: flex;
          align-items: center;
          
          .el-progress {
            flex: 1;
            margin-right: 12px;
          }
          
          .progress-text {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            min-width: 40px;
            text-align: right;
          }
        }
      }
    }
    
    .activities-list {
      .activity-item {
        display: flex;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .activity-icon {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          
          &.type-create {
            background: #e6f7ff;
            color: #1890ff;
          }
          
          &.type-update {
            background: #f6ffed;
            color: #52c41a;
          }
          
          &.type-success {
            background: #fff7e6;
            color: #faad14;
          }
          
          &.type-comment {
            background: #f6ffed;
            color: #52c41a;
          }
          
          &.type-approve {
            background: #f0f5ff;
            color: #2f54eb;
          }
          
          .el-icon {
            font-size: 16px;
          }
        }
        
        .activity-content {
          flex: 1;
          
          .activity-text {
            font-size: 14px;
            color: #333;
            margin-bottom: 4px;
            
            .activity-user {
              font-weight: 500;
              color: #1890ff;
            }
            
            .activity-target {
              color: #722ed1;
              font-weight: 500;
            }
          }
          
          .activity-time {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
  
  // 快捷操作区域
  .quick-section {
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }
    
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
          
          .el-icon {
            font-size: 24px;
          }
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
        
        .el-button {
          width: calc(50% - 5px);
          margin: 5px 5px 0 0 !important;
          
          &:nth-child(2n) {
            margin-right: 0 !important;
          }
        }
      }
    }
    
    .stats-section {
      .el-col {
        margin-bottom: 16px;
      }
    }
    
    .quick-grid {
      grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)) !important;
      gap: 16px !important;
    }
  }
  </style>