<template>
  <div class="app-container">
    <!-- 基础查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" 
      class="search-form compact-form">
      <el-row :gutter="20">
        <!-- 第一行：基础查询条件 -->
        <el-col :span="8">
          <el-form-item label="店铺名称" prop="shopName" label-width="70px" class="compact-item">
            <el-input
              v-model="queryParams.shopName"
              placeholder="请输入店铺名称"
              clearable
              @keyup.enter="handleQuery"
              class="compact-input"
            />
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item class="search-actions compact-item" label-width="0">
            <el-button type="primary" icon="Search" @click="handleQuery" size="default">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery" size="default">重置</el-button>
            <el-button type="text" @click="toggleAdvanced" size="default" style="margin-left: 10px;">
              {{ showAdvanced ? '收起' : '展开' }}
              <el-icon :style="{ transform: showAdvanced ? 'rotate(180deg)' : '', transition: 'transform 0.3s', marginLeft: '4px' }">
                <ArrowDown />
              </el-icon>
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 高级查询条件（折叠部分） -->
      <div v-if="showAdvanced" class="advanced-search"
        style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="店铺状态" prop="status" label-width="70px" class="compact-item">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 200px">
                <el-option label="全部" :value="null" />
                <el-option label="正常营业" :value="1" />
                <el-option label="已关闭" :value="2" />
                <el-option label="审核中" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="contactPhone" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.contactPhone"
                placeholder="请输入联系电话"
                clearable
                style="width: 200px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系地址" prop="contactAddress" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.contactAddress"
                placeholder="请输入联系地址"
                clearable
                style="width: 200px"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="销量范围" label-width="70px" class="compact-item">
              <div style="display: flex; align-items: center; width: 200px">
                <el-input-number 
                  v-model="queryParams.minSalesCount" 
                  placeholder="最低销量" 
                  :min="0" 
                  :controls="false" 
                  style="flex: 1" 
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number 
                  v-model="queryParams.maxSalesCount" 
                  placeholder="最高销量" 
                  :min="0" 
                  :controls="false" 
                  style="flex: 1" 
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="粉丝范围" label-width="70px" class="compact-item">
              <div style="display: flex; align-items: center; width: 200px">
                <el-input-number 
                  v-model="queryParams.minFollowerCount" 
                  placeholder="最少粉丝" 
                  :min="0" 
                  :controls="false" 
                  style="flex: 1" 
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number 
                  v-model="queryParams.maxFollowerCount" 
                  placeholder="最多粉丝" 
                  :min="0" 
                  :controls="false" 
                  style="flex: 1" 
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评分范围" label-width="70px" class="compact-item">
              <div style="display: flex; align-items: center; width: 200px">
                <el-input-number 
                  v-model="queryParams.minShopScore" 
                  placeholder="最低评分" 
                  :min="0" 
                  :max="5"
                  :step="0.1"
                  :precision="1"
                  :controls="false" 
                  style="flex: 1" 
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number 
                  v-model="queryParams.maxShopScore" 
                  placeholder="最高评分" 
                  :min="0" 
                  :max="5"
                  :step="0.1"
                  :precision="1"
                  :controls="false" 
                  style="flex: 1" 
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="删除状态" prop="deleteFlag" label-width="70px" class="compact-item">
              <el-select v-model="queryParams.deleteFlag" placeholder="全部" clearable style="width: 200px">
                <el-option label="全部" :value="null" />
                <el-option label="正常" :value="0" />
                <el-option label="已删除" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:store:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:store:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:store:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:store:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="storeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" width="80" />
      <el-table-column label="店铺名称" align="center" prop="shopName" min-width="150" />
      <el-table-column label="所有者用户ID" align="center" prop="userId" width="120" />
      <el-table-column label="店铺logo" align="center" prop="shopLogo" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.shopLogo" :width="50" :height="50" v-if="scope.row.shopLogo" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="店铺描述" align="center" prop="description" width="150">
        <template #default="scope">
          <div class="shop-description">
            {{ scope.row.description ? scope.row.description.substring(0, 20) + (scope.row.description.length > 20 ? '...' : '') : '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="120" />
      <el-table-column label="联系地址" align="center" prop="contactAddress" width="150">
        <template #default="scope">
          <div class="shop-address">
            {{ scope.row.contactAddress ? scope.row.contactAddress.substring(0, 15) + (scope.row.contactAddress.length > 15 ? '...' : '') : '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="店铺评分" align="center" prop="shopScore" width="100">
        <template #default="scope">
          <span style="color: #ff9900; font-weight: bold;">{{ scope.row.shopScore || '0.0' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="销售数量" align="center" prop="salesCount" width="100">
        <template #default="scope">
          <span style="color: #e4393c; font-weight: bold;">{{ scope.row.salesCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品数量" align="center" prop="productCount" width="100">
        <template #default="scope">
          <span style="color: #409eff; font-weight: bold;">{{ scope.row.productCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="粉丝数量" align="center" prop="followerCount" width="100">
        <template #default="scope">
          <span style="color: #67c23a; font-weight: bold;">{{ scope.row.followerCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="店铺状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag 
            :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'danger' : 'warning'" 
            size="small"
          >
            {{ scope.row.status === 1 ? '正常营业' : scope.row.status === 2 ? '已关闭' : scope.row.status === 3 ? '审核中' : '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:store:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:store:remove']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 1 ? '关闭店铺' : scope.row.status === 2 ? '开启店铺' : '审核中'" placement="top">
            <el-button 
              link 
              :type="scope.row.status === 1 ? 'warning' : scope.row.status === 2 ? 'success' : 'info'" 
              :icon="scope.row.status === 1 ? 'Switch' : 'Check'"
              @click="toggleStatus(scope.row)" 
              v-hasPermi="['system:store:edit']"
              :disabled="scope.row.status === 3"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改店铺信息对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="storeRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
        </el-form-item>

        <el-form-item v-if="form.id == null" label="所有者用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" :precision="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="所有者用户ID">
          <el-input :model-value="form.userId" disabled />
        </el-form-item>
        
        <!-- 店铺logo上传 -->
        <el-form-item label="店铺logo" prop="shopLogo">
          <image-upload v-model="form.shopLogo"/>
        </el-form-item>

        <el-form-item label="店铺横幅" prop="shopBanner">
          <image-upload v-model="form.shopBanner"/>
        </el-form-item>
              
        <el-form-item label="店铺描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入店铺描述" rows="3" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="联系地址" prop="contactAddress">
          <el-input v-model="form.contactAddress" placeholder="请输入联系地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup name="Store">
import { listStore, getStore, delStore, addStore, updateStore, updateStoreStatus } from "@/api/system/store";
import { ArrowDown, Plus } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const storeList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false); // 控制高级筛选显示
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    // 基础查询条件
    userId: null,
    shopName: null,
    // 高级查询条件
    contactPhone: null,
    contactAddress: null,
    status: null,
    deleteFlag: null,
    // 范围查询
    minSalesCount: null,
    maxSalesCount: null,
    minFollowerCount: null,
    maxFollowerCount: null,
    minShopScore: null,
    maxShopScore: null,
    // 时间范围
    createTime: null,
  },
  rules: {
    shopName: [
      { required: true, message: "店铺名称不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "店铺所有者用户ID不能为空", trigger: "change" }
    ],
    shopLogo: [
      { required: true, message: "店铺logo不能为空", trigger: "change" }
    ],
    contactPhone: [
      { required: true, message: "联系电话不能为空", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入11位正确的手机号码", trigger: "blur" }
    ],
    contactAddress: [
      { required: true, message: "联系地址不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询店铺信息列表 */
function getList() {
  loading.value = true;
  
  // 构建查询参数
  const params = { ...queryParams.value };
  
  // 处理时间范围
  if (params.createTime && params.createTime.length === 2) {
    params.beginCreateTime = params.createTime[0];
    params.endCreateTime = params.createTime[1];
    delete params.createTime;
  }
  
  // 移除空值参数
  Object.keys(params).forEach(key => {
    if (params[key] === '' || params[key] === null || params[key] === undefined) {
      delete params[key];
    }
  });
  
  listStore(params).then(response => {
    storeList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 切换高级筛选显示
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value;
};

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    createTime: null,
    updateTime: null,
    createUser: null,
    updateUser: null,
    deleteFlag: 0,
    userId: null,
    shopName: null,
    shopLogo: null,
    shopBanner: null,
    description: null,
    contactPhone: null,
    contactAddress: null,
    status: 1
  };
  proxy.resetForm("storeRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    shopName: null,
    contactPhone: null,
    contactAddress: null,
    status: null,
    deleteFlag: null,
    minSalesCount: null,
    maxSalesCount: null,
    minFollowerCount: null,
    maxFollowerCount: null,
    minShopScore: null,
    maxShopScore: null,
    createTime: null,
  };
  showAdvanced.value = false;
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加店铺信息";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value[0];
  getStore(_id).then(response => {
    form.value = response.data;
    // 确保必要字段有默认值
    if (form.value.status === undefined || form.value.status === null) {
      form.value.status = 1;
    }
    if (form.value.deleteFlag === undefined || form.value.deleteFlag === null) {
      form.value.deleteFlag = 0;
    }
    open.value = true;
    title.value = "修改店铺信息";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["storeRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        const storeData = getEditableStoreData();
        updateStore(storeData).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        const storeData = {
          ...getEditableStoreData(),
          userId: form.value.userId,
          status: 1,
          deleteFlag: 0
        };
        addStore(storeData).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function getEditableStoreData() {
  return {
    id: form.value.id,
    shopName: form.value.shopName,
    shopLogo: form.value.shopLogo,
    shopBanner: form.value.shopBanner,
    description: form.value.description,
    contactPhone: form.value.contactPhone,
    contactAddress: form.value.contactAddress
  };
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id ? [row.id] : ids.value;
  proxy.$modal.confirm('是否确认删除店铺信息编号为"' + _ids.join(',') + '"的数据项？').then(function() {
    return delStore(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  const params = { ...queryParams.value };
  // 处理时间范围
  if (params.createTime && params.createTime.length === 2) {
    params.beginCreateTime = params.createTime[0];
    params.endCreateTime = params.createTime[1];
    delete params.createTime;
  }
  
  proxy.download('system/store/export', params, `store_${new Date().getTime()}.xlsx`)
}

/** 切换店铺状态 */
function toggleStatus(row) {
  const newStatus = row.status === 1 ? 2 : 1;
  const statusText = newStatus === 1 ? '开启' : '关闭';
  
  proxy.$modal.confirm(`确认要${statusText}店铺"${row.shopName}"吗？`).then(() => {
    return updateStoreStatus(row.id, newStatus);
  }).then(response => {
    proxy.$modal.msgSuccess(`${statusText}成功`);
    getList();
  }).catch(() => {});
}

// 组件挂载时加载数据
onMounted(() => {
  getList();
});
</script>

<style scoped>
.search-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.compact-form {
  padding: 15px;
}

.advanced-search {
  border-top: 1px solid #e8e8e8;
}

.shop-description, .shop-address {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-container {
  padding: 20px;
}

.mb8 {
  margin-bottom: 20px;
}

/* 紧凑型form-item样式 */
.compact-item :deep(.el-form-item__label) {
  padding-right: 8px;
}

.compact-item :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}

.compact-input {
  width: 100%;
  min-width: 120px;
}

.search-actions {
  display: flex;
  align-items: center;
  white-space: nowrap;
  margin-left: 10px;
}

/* 确保整个表单行不换行 */
:deep(.el-form--inline .el-form-item) {
  display: inline-flex;
  margin-right: 0;
  flex-shrink: 0; /* 防止form-item被压缩 */
}

:deep(.el-col) {
  flex-shrink: 0; /* 防止列被压缩 */
}

/* 调整间距 */
:deep(.el-form-item) {
  margin-bottom: 15px;
  margin-right: 0;
}

/* 确保搜索区域整体布局 */
:deep(.el-row) {
  flex-wrap: nowrap;
}

:deep(.el-table .cell) {
  line-height: 1.4;
}

/* 上传样式 */
.avatar-uploader,
.banner-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader {
  width: 200px;
  height: 200px;
}

.banner-uploader {
  width: 100%;
  height: 150px;
}

.avatar-uploader:hover,
.banner-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  text-align: center;
  line-height: 200px;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
  line-height: 150px;
}

.banner-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.banner-tips {
  margin-top: 10px;
  color: #8c939d;
}

.banner-size-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-tips {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #8c939d;
  width: 100%;
}

.upload-preview {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.preview-text {
  font-size: 12px;
}

.upload-actions {
  margin-top: 10px;
}

.dialog-uploader {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

:deep(.el-upload__tip) {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}
</style>
