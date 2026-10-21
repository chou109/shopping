<template>
  <div class="app-container category-management">
    <!-- 搜索区域卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="分类名称" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="请输入分类名称"
                clearable
                style="width: 100%"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="全部" :value="undefined" />
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item class="search-buttons">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <div class="toolbar-wrapper">
      <div class="action-buttons">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">{{ isExpandAll ? '折叠' : '展开' }}</el-button>
      </div>
    </div>

    <!-- 分类表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div v-if="categoryList.length === 0 && !loading" class="empty-table">
        <el-empty description="暂无分类数据" />
      </div>

      <el-table
        v-else-if="refreshTable"
        v-loading="loading"
        :data="categoryList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
        :row-class-name="tableRowClassName"
        stripe
        border
      >
        <el-table-column prop="name" label="分类名称" min-width="300" class-name="name-column">
          <template #default="scope">
            <span v-if="scope.row.children && scope.row.children.length > 0" class="folder-icon">
              <el-icon class="expanded-icon"><FolderOpened /></el-icon>
            </span>
            <span v-else class="file-icon">
              <el-icon><Document /></el-icon>
            </span>
            <span :class="['category-name', { 'disabled-text': scope.row.status === 0, 'deleted-text': scope.row.deleteFlag === 1 }]">{{ scope.row.name }}</span>
            <el-tag v-if="scope.row.status === 0" size="small" type="info" class="ml-2">已禁用</el-tag>
            <el-tag v-if="scope.row.deleteFlag === 1" size="small" type="warning" class="ml-2">已删除</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center" class-name="status-column">
          <template #default="scope">
            <div class="status-cell">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" align="center" width="280" class-name="action-column">
          <template #default="scope">
            <div class="action-cell">
              <el-button 
                type="primary" 
                icon="Plus" 
                size="small"
                @click="handleAdd(scope.row)"
                v-if="!scope.row.deleteFlag"
              >添加</el-button>
              <el-button 
                type="warning" 
                icon="Edit" 
                size="small"
                @click="handleUpdate(scope.row)"
                v-if="!scope.row.deleteFlag"
              >修改</el-button>
              <el-button 
                type="danger" 
                icon="Delete" 
                size="small"
                @click="handleDelete(scope.row)"
                v-if="scope.row.parentId != 0 && !hasChildren(scope.row) && !scope.row.deleteFlag"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改分类对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryOptions"
            :props="{
              value: 'id',
              label: 'name',
              children: 'children',
              disabled: 'disabled'
            }"
            :render-after-expand="false"
            placeholder="请选择上级分类（不选则为顶级分类）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            placeholder="请输入备注信息" 
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened, Folder, Document } from '@element-plus/icons-vue'
import { listCategory, getCategory, addCategory, updateCategory, delCategory, listCategoryFront } from "@/api/system/category";

// 状态管理
const loading = ref(false)
const open = ref(false)
const title = ref("")
const categoryOptions = ref([])
const isExpandAll = ref(false)
const refreshTable = ref(true)

// 数据
const categoryList = ref([])

// 表单数据
const data = reactive({
  form: {
    id: undefined,
    parentId: 0,
    name: '',
    status: 1,
    remark: ''
  },
  queryParams: {
    name: '',
    status: undefined
  },
  rules: {
    name: [
      { required: true, message: "分类名称不能为空", trigger: "blur" },
      { min: 2, max: 50, message: "分类名称长度为2-50个字符", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 引用
const categoryRef = ref()
const queryRef = ref()

/** 检查是否有子节点 */
function hasChildren(row) {
  return row.children && row.children.length > 0
}

/** 获取分类样式类 */
function getCategoryClass(row) {
  const classes = ['category-cell']
  if (row.status === 0) classes.push('disabled-category')
  if (row.deleteFlag === 1) classes.push('deleted-category')
  return classes.join(' ')
}

/** 表格行样式 */
function tableRowClassName({ row }) {
  if (row.deleteFlag === 1) return 'deleted-row'
  if (row.status === 0) return 'disabled-row'
  return ''
}

/** 查询分类列表 - 优先使用树形接口 */
function getList() {
  loading.value = true
  
  // 先尝试使用树形接口
  listCategoryFront().then(response => {
    if (response.code === 200) {
      const data = response.data || []
      // 添加层级信息
      const addLevelInfo = (items, level = 1) => {
        return items.map(item => {
          const newItem = {
            ...item,
            level: level,
            status: item.status || 0,
            parentId: item.parentId || 0
          }
          if (item.children && item.children.length > 0) {
            newItem.children = addLevelInfo(item.children, level + 1)
          }
          return newItem
        })
      }
      
      categoryList.value = addLevelInfo(data)
      
      // 同时加载选项数据
      loadCategoryOptions()
    } else {
      ElMessage.warning('树形接口返回异常，尝试普通接口')
      fallbackToNormalApi()
    }
    loading.value = false
  }).catch(error => {
    console.warn('树形接口调用失败:', error)
    ElMessage.warning('树形接口调用失败，尝试普通接口')
    fallbackToNormalApi()
  })
}

/** 降级到普通接口 */
function fallbackToNormalApi() {
  listCategory(queryParams.value).then(response => {
    if (response.code === 200) {
      const data = response.data || []
      
      // 处理NULL值
      const processedData = data.map(item => ({
        ...item,
        status: item.status || 0,
        parentId: item.parentId || 0
      }))
      
      // 构建树形结构
      categoryList.value = buildTree(processedData)
    } else {
      ElMessage.error(response.msg || "加载失败")
      categoryList.value = []
    }
    loading.value = false
  }).catch(error => {
    console.error('普通接口也失败:', error)
    ElMessage.error("加载分类列表失败: " + (error.message || '未知错误'))
    categoryList.value = []
    loading.value = false
  })
}

/** 构建树形结构 */
function buildTree(data) {
  if (!data || !Array.isArray(data)) return []
  
  const result = []
  const map = {}
  
  // 创建映射，处理所有NULL值
  data.forEach(item => {
    map[item.id] = { 
      ...item, 
      children: [],
      status: item.status || 0,
      parentId: item.parentId || 0
    }
  })
  
  // 构建树
  data.forEach(item => {
    const node = map[item.id]
    const parentId = item.parentId || 0
    
    if (parentId === 0) {
      node.level = 1
      result.push(node)
    } else if (map[parentId]) {
      node.level = (map[parentId].level || 1) + 1
      if (!map[parentId].children) {
        map[parentId].children = []
      }
      map[parentId].children.push(node)
    } else {
      node.level = 1
      result.push(node)
    }
  })
  
  return result
}

/** 加载分类选项 */
function loadCategoryOptions() {
  return new Promise((resolve) => {
    listCategory().then(response => {
      if (response.code === 200) {
        const data = response.data || []
        // 处理NULL值
        const processedData = data.map(item => ({
          ...item,
          status: item.status || 0,
          parentId: item.parentId || 0
        }))
        // 构建树形选项
        const treeData = buildTree(processedData)
        // 添加顶级选项
        categoryOptions.value = [
          {
            id: 0,
            name: '顶级分类',
            children: treeData
          }
        ]
      }
      resolve()
    }).catch(() => {
      categoryOptions.value = []
      resolve()
    })
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    parentId: 0,
    name: '',
    status: 1,
    remark: ''
  }
  if (categoryRef.value) {
    categoryRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  queryParams.value.name = ''
  queryParams.value.status = undefined
  getList()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  loadCategoryOptions().then(() => {
    // 如果有传入row，说明是添加子分类
    if (row && row.id) {
      form.value.parentId = row.id
    }
    open.value = true
    title.value = "添加分类"
  })
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const categoryId = row.id
  
  // 获取分类详情
  getCategory(categoryId).then(response => {
    if (response.code === 200) {
      const data = response.data || {}
      // 处理NULL值
      Object.assign(form.value, {
        id: data.id,
        parentId: data.parentId || 0,
        name: data.name || '',
        status: data.status || 0,
        remark: data.remark || ''
      })
      
      // 加载分类选项（排除当前分类及其子分类）
      return loadCategoryOptionsExcluding(categoryId)
    } else {
      throw new Error(response.msg || "获取分类信息失败")
    }
  }).then(() => {
    open.value = true
    title.value = "修改分类"
  }).catch(error => {
    console.error('修改操作失败:', error)
    ElMessage.error(error.message || "获取分类信息失败")
  })
}

/** 加载分类选项（排除指定分类及其子分类） */
function loadCategoryOptionsExcluding(excludeId) {
  return listCategory().then(response => {
    if (response.code === 200) {
      const allCategories = response.data || []
      // 处理NULL值
      const processedData = allCategories.map(item => ({
        ...item,
        status: item.status || 0,
        parentId: item.parentId || 0
      }))
      // 过滤掉当前分类及其子分类
      const filteredData = filterCategoryAndChildren(processedData, excludeId)
      const treeData = buildTree(filteredData)
      
      // 添加顶级选项
      categoryOptions.value = [
        {
          id: 0,
          name: '顶级分类',
          children: treeData
        }
      ]
    }
  })
}

/** 过滤分类及其子分类 */
function filterCategoryAndChildren(categories, excludeId) {
  const result = []
  const excludeIds = getAllChildrenIds(categories, excludeId)
  
  categories.forEach(category => {
    if (!excludeIds.includes(category.id)) {
      result.push(category)
    }
  })
  
  return result
}

/** 获取所有子分类ID */
function getAllChildrenIds(categories, parentId) {
  const result = [parentId]
  const children = categories.filter(item => (item.parentId || 0) === parentId)
  
  children.forEach(child => {
    result.push(child.id)
    result.push(...getAllChildrenIds(categories, child.id))
  })
  
  return result
}

/** 状态切换 */
function handleStatusChange(row) {
  const newStatus = row.status
  const updateData = {
    id: row.id,
    status: newStatus
  }
  
  updateCategory(updateData).then(response => {
    if (response.code === 200) {
      ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
    } else {
      // 操作失败，回滚状态
      row.status = newStatus === 1 ? 0 : 1
      ElMessage.error(response.msg || '操作失败')
    }
  }).catch(error => {
    // 操作失败，回滚状态
    row.status = newStatus === 1 ? 0 : 1
    ElMessage.error('操作失败: ' + error.message)
  })
}

/** 提交按钮 */
function submitForm() {
  if (!categoryRef.value) return
  
  categoryRef.value.validate(valid => {
    if (valid) {
      // 检查不能设置自身为父分类
      if (form.value.id && form.value.parentId === form.value.id) {
        ElMessage.error("不能将自身设置为父分类")
        return
      }
      
      // 处理数据，确保没有undefined
      const submitData = {
        ...form.value,
        parentId: form.value.parentId || 0,
        status: form.value.status || 0
      }
      
      loading.value = true
      const savePromise = form.value.id 
        ? updateCategory(submitData)
        : addCategory(submitData)
      
      savePromise.then(response => {
        if (response.code === 200) {
          ElMessage.success(form.value.id ? "修改成功" : "新增成功")
          open.value = false
          getList()
        } else {
          ElMessage.error(response.msg || (form.value.id ? "修改失败" : "新增失败"))
        }
      }).catch(error => {
        console.error('保存分类失败:', error)
        ElMessage.error("保存失败: " + (error.message || '未知错误'))
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  // 检查是否有子分类
  if (row.children && row.children.length > 0) {
    ElMessage.error('该分类下存在子分类，请先删除子分类')
    return
  }
  
  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    loading.value = true
    return delCategory(row.id)
  }).then(response => {
    if (response.code === 200) {
      ElMessage.success("删除成功")
      getList()
    } else {
      ElMessage.error(response.msg || "删除失败")
    }
  }).catch((error) => {
    if (error !== 'cancel') {
      ElMessage.error(error.message || "删除失败")
    }
  }).finally(() => {
    loading.value = false
  })
}

// 页面加载时获取分类列表
onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 容器基础样式 */
.category-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding: 20px 24px 8px;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.search-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 工具栏 */
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 8px;
}

.table-card :deep(.el-table th) {
  padding: 14px 0;
}

.table-card :deep(.el-table td) {
  padding: 12px 0;
}

.table-card :deep(.el-table .cell) {
  padding: 0 16px;
  line-height: 1.6;
}

/* 分类名称列样式 */
.category-cell {
  display: inline-flex;
  align-items: center;
}

.category-content {
  display: inline-flex;
  align-items: center;
}

.category-name {
  margin-left: 8px;
  font-size: 14px;
  color: #303133;
}

.category-name.disabled-text {
  color: #c0c4cc;
}

.category-name.deleted-text {
  text-decoration: line-through;
  color: #909399;
}

/* 让展开箭头和内容对齐 */
:deep(.name-column .cell) {
  display: flex !important;
  align-items: center !important;
}

:deep(.el-table__expand-icon) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  vertical-align: middle !important;
  margin-right: 4px;
  flex-shrink: 0;
}

/* 状态列样式 */
.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 操作列样式 */
.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  height: 100%;
}

/* 分类样式 */
.disabled-category .category-name {
  color: #c0c4cc;
}

.deleted-category .category-name {
  text-decoration: line-through;
  color: #909399;
}

/* 图标样式 */
.folder-icon, .file-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  margin-right: 6px;
  color: #409eff;
}

.expanded-icon {
  color: #e6a23c;
}

/* 标签样式 */
.ml-2 {
  margin-left: 8px;
}

/* 空状态 */
.empty-table {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #fff;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

/* 特殊行样式 */
:deep(.el-table .deleted-row) {
  background-color: #fef0f0 !important;
}

:deep(.el-table .deleted-row:hover > td) {
  background-color: #fde2e2 !important;
}

:deep(.el-table .disabled-row) {
  background-color: #fafafa !important;
}

:deep(.el-table .disabled-row:hover > td) {
  background-color: #f5f7fa !important;
}

/* 表格行hover效果 */
:deep(.el-table .el-table__row:hover > td) {
  background-color: #f5f7fa;
}

/* 滚动条样式 */
:deep(.el-table .el-table__body-wrapper) {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f5f5f5;
}

:deep(.el-table .el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.el-table .el-table__body-wrapper::-webkit-scrollbar-track) {
  background: #f5f5f5;
}

:deep(.el-table .el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 3px;
}
</style>