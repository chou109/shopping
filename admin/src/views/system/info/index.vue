<template>
  <div class="app-container order-management">
    <!-- 搜索区域卡片 -->
    <el-card class="search-card" shadow="hover" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="订单编号" prop="orderNo">
              <el-input
                v-model="queryParams.orderNo"
                placeholder="请输入订单编号"
                clearable
                @keyup.enter="handleQuery"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="购买人id" prop="userId">
              <el-input
                v-model="queryParams.userId"
                placeholder="请输入购买人id"
                clearable
                @keyup.enter="handleQuery"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%" @change="handleQuery">
                <el-option label="待付款" :value="10" />
                <el-option label="待发货" :value="20" />
                <el-option label="待收货" :value="30" />
                <el-option label="待评价" :value="40" />
                <el-option label="交易完成" :value="50" />
                <el-option label="已取消" :value="60" />
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
      
      <!-- 筛选条件标签展示 -->
      <div class="filter-tags" v-if="queryParams.status !== null">
        <span class="filter-label">当前筛选：</span>
        <el-tag 
          :type="getStatusTagType(queryParams.status)" 
          closable 
          @close="clearStatusFilter"
          effect="dark"
          size="large"
        >
          {{ getStatusText(queryParams.status) }}
        </el-tag>
      </div>
    </el-card>

    <!-- 操作按钮区域 -->
    <div class="toolbar-wrapper">
      <div class="action-buttons">
        <el-button type="danger" plain icon="Delete" :loading="deleting" :disabled="multiple || deleting" @click="handleDelete" v-hasPermi="['system:info:remove']" v-hasRole="['admin']">删除</el-button>
        <el-button type="primary" plain :loading="batchShipping" :disabled="!hasShippableOrders || batchShipping || shippingOrderNo !== null" @click="handleBatchShip" v-hasPermi="['system:info:edit', 'system:order:edit']">
          <el-icon><Van /></el-icon>一键发货
        </el-button>
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:info:export']">导出</el-button>
      </div>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </div>

    <!-- 数据表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <el-alert v-if="listError" :title="listError" type="error" :closable="false" show-icon>
        <template #default><el-button link type="primary" @click="getList">重试</el-button></template>
      </el-alert>
      <el-table v-else
        ref="infoTableRef"
        v-loading="loading" 
        :data="infoList" 
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
        :row-style="{ height: '60px' }"
        stripe
        border
      >
        <el-table-column v-if="isAdmin" type="selection" width="50" align="center" />
        <el-table-column label="ID" align="center" prop="id" width="70" />
        <el-table-column label="订单编号" align="center" prop="orderNo" min-width="160" show-overflow-tooltip />
        <el-table-column label="购买人id" align="center" prop="userId" width="90" />
        <el-table-column label="支付金额" align="center" prop="payment" width="110">
          <template #default="scope">
            <span class="price-text">¥{{ scope.row.payment || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付类型" align="center" prop="paymentTypeDesc" width="100">
          <template #default="scope">
            <el-tag :type="getPaymentTagType(scope.row.paymentType)" effect="light" round>
              {{ getPaymentText(scope.row.paymentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" align="center" prop="status" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" effect="dark" round>
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" align="left" min-width="200">
          <template #default="scope">
            <div class="receiver-info">
              <div class="receiver-name">
                <el-icon><User /></el-icon>
                <span>{{ scope.row.receiveName || '-' }}</span>
              </div>
              <div class="receiver-phone">
                <el-icon><Phone /></el-icon>
                <span>{{ scope.row.receivePhone || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货地址" align="left" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div class="address-text">
              <el-icon><Location /></el-icon>
              <span>{{ (scope.row.province || '') + (scope.row.city || '') + (scope.row.area || '') + (scope.row.street || '') || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" align="center" prop="paymentTime" width="160">
          <template #default="scope">
            <span class="time-text">{{ parseTime(scope.row.paymentTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="160">
          <template #default="scope">
            <div class="action-column">
              <el-button link type="primary" icon="View" @click="handleUpdate(scope.row)" v-hasPermi="['system:info:query']">查看</el-button>
              <el-button link type="danger" icon="Delete" :loading="deleting" :disabled="deleting" @click="handleDelete(scope.row)" v-hasPermi="['system:info:remove']" v-hasRole="['admin']">删除</el-button>
              <el-button 
                v-if="scope.row.status === 20"
                type="warning" 
                size="small"
                round
                :loading="shippingSubmitting && shippingOrderNo === scope.row.orderNo"
                :disabled="batchShipping || shippingOrderNo !== null"
                @click="handleShipOrder(scope.row)" 
                v-hasPermi="['system:info:edit', 'system:order:edit']"
              ><el-icon><Van /></el-icon>发货</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 分页组件 -->
    <div class="pagination-wrapper">
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 订单详情/编辑弹窗 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body class="order-dialog" @close="invalidateOrderDetailRequest">
      <el-form v-loading="detailLoading" ref="infoRef" :model="form" :rules="rules" label-width="100px" class="order-form" disabled>
        <!-- 订单基本信息 -->
        <div class="form-section">
          <div class="section-title">订单信息</div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="订单编号" prop="orderNo">
                <el-input v-model="form.orderNo" placeholder="请输入订单编号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="购买人id" prop="userId">
                <el-input v-model="form.userId" placeholder="请输入购买人id" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="订单状态" prop="status">
                <el-select v-model="form.status" placeholder="请选择订单状态" style="width: 100%">
                  <el-option label="待付款" :value="10" />
                  <el-option label="待发货" :value="20" />
                  <el-option label="待收货" :value="30" />
                  <el-option label="待评价" :value="40" />
                  <el-option label="交易完成" :value="50" />
                  <el-option label="已取消" :value="60" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.id">
              <el-form-item label="删除状态" prop="deleteFlag">
                <el-select v-model="form.deleteFlag" placeholder="请选择" style="width: 100%">
                  <el-option label="正常" :value="0" />
                  <el-option label="已删除" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 支付信息 -->
        <div class="form-section">
          <div class="section-title">支付信息</div>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="支付金额" prop="payment">
                <el-input-number v-model="form.payment" placeholder="请输入" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="邮费" prop="postage">
                <el-input-number v-model="form.postage" placeholder="请输入" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="支付类型" prop="paymentType">
                <el-select v-model="form.paymentType" :placeholder="form.paymentType == null ? '待支付' : '请选择'" style="width: 100%">
                  <el-option v-for="paymentType in PAYMENT_TYPES" :key="paymentType.value" :label="paymentType.label" :value="paymentType.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="支付时间" prop="paymentTime">
                <el-date-picker v-model="form.paymentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 收货信息 -->
        <div class="form-section">
          <div class="section-title">收货信息</div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="收货人" prop="receiveName">
                <el-input v-model="form.receiveName" placeholder="请输入收货人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="receivePhone">
                <el-input v-model="form.receivePhone" placeholder="请输入联系电话" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="省份" prop="province">
                <el-input v-model="form.province" placeholder="省" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" prop="city">
                <el-input v-model="form.city" placeholder="市" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区/县" prop="area">
                <el-input v-model="form.area" placeholder="区" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="18">
              <el-form-item label="详细地址" prop="street">
                <el-input v-model="form.street" placeholder="请输入详细地址" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="邮编" prop="postalCode">
                <el-input v-model="form.postalCode" placeholder="邮编" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Info">
import { listInfo, getInfo, delInfo, shipInfo } from "@/api/system/info";
import { User, Phone, Location, Van } from '@element-plus/icons-vue';
import useUserStore from '@/store/modules/user';
import { PAYMENT_TYPES, getPaymentTypeLabel, getPaymentTypeTag } from '@/utils/order-payment.mjs';
import { getObjectResponseData, normalizePagedResponse } from '@/utils/response-data.mjs';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const isAdmin = computed(() => userStore.roles?.includes('admin'));

const infoList = ref([]);
const infoTableRef = ref(null);
const open = ref(false);
const loading = ref(true);
const detailLoading = ref(false);
const listError = ref("");
const showSearch = ref(true);
const ids = ref([]);
const selectedRows = ref([]);  // 新增：保存选中的行数据
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const batchShipping = ref(false);
const shippingOrderNo = ref(null);
const shippingSubmitting = ref(false);
const deleting = ref(false);
let listRequestId = 0;
let orderDetailRequestId = 0;

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: null,
    userId: null,
    status: null,
    deleteFlag: null,
  },
  rules: {
    orderNo: [
      { required: true, message: "订单编号不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "购买人ID不能为空", trigger: "blur" }
    ],
    payment: [
      { required: true, message: "支付金额不能为空", trigger: "blur" }
    ],
    paymentType: [
      { required: true, message: "支付类型不能为空", trigger: "change" }
    ],
    status: [
      { required: true, message: "订单状态不能为空", trigger: "change" }
    ],
    receiveName: [
      { required: true, message: "收货人不能为空", trigger: "blur" }
    ],
    receivePhone: [
      { required: true, message: "联系电话不能为空", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入11位正确的手机号码", trigger: "blur" }
    ],
    street: [
      { required: true, message: "详细地址不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

// 计算属性：是否有可发货的订单被选中
const hasShippableOrders = computed(() => {
  return selectedRows.value.some(row => row.status === 20);
});

/** 查询订单列表 */
function getList() {
  const requestId = ++listRequestId;
  loading.value = true;
  listError.value = "";
  infoTableRef.value?.clearSelection();
  
  const params = { ...queryParams.value };

  return listInfo(params).then(response => {
    if (requestId !== listRequestId) return;
    const page = normalizePagedResponse(response);
    infoList.value = page.rows;
    total.value = page.total;
  }).catch(() => {
    if (requestId === listRequestId) {
      listError.value = "订单加载失败，请重试";
    }
  }).finally(() => {
    if (requestId === listRequestId) loading.value = false;
  });
}

// 取消按钮
function cancel() {
  invalidateOrderDetailRequest();
  open.value = false;
  reset();
}

function invalidateOrderDetailRequest() {
  orderDetailRequestId += 1;
  detailLoading.value = false;
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
    orderNo: null,
    payment: null,
    paymentType: null,
    paymentTypeDesc: null,
    postage: null,
    status: 0, // 默认待付款
    statusDesc: null,
    paymentTime: null,
    addressId: null,
    receiveName: null,
    receivePhone: null,
    province: null,
    city: null,
    area: null,
    street: null,
    postalCode: null,
    userId: null
  };
  proxy.resetForm("infoRef");
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
    orderNo: null,
    userId: null,
    status: null,
    deleteFlag: null,
  };
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  selectedRows.value = selection;  // 保存完整的行数据
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 清除状态筛选 */
function clearStatusFilter() {
  queryParams.value.status = null;
  handleQuery();
}

/** 查看订单详情 */
async function handleUpdate(row) {
  const requestId = ++orderDetailRequestId;
  reset();
  const _id = row.id || ids.value[0];
  if (!_id) return;
  open.value = true;
  title.value = "订单详情";
  detailLoading.value = true;
  try {
    const response = await getInfo(_id);
    if (requestId !== orderDetailRequestId) return;
    form.value = getObjectResponseData(response);
    // 确保必要字段有默认值
    if (form.value.status === undefined || form.value.status === null) {
      form.value.status = 0;
    }
    if (form.value.deleteFlag === undefined || form.value.deleteFlag === null) {
      form.value.deleteFlag = 0;
    }
  } catch (error) {
    if (requestId === orderDetailRequestId) {
      open.value = false;
      proxy.$modal.msgError("订单详情加载失败：" + (error?.message || error || "请稍后重试"));
    }
  } finally {
    if (requestId === orderDetailRequestId) detailLoading.value = false;
  }
}

/** 删除按钮操作 */
async function handleDelete(row) {
  if (deleting.value) return;
  const _ids = row.id ? [row.id] : ids.value;
  if (_ids.length === 0) return;
  deleting.value = true;
  try {
    await proxy.$modal.confirm('是否确认删除订单编号为"' + _ids.join(',') + '"的数据项？');
    await delInfo(_ids);
    await getList();
    proxy.$modal.msgSuccess("删除成功");
  } catch (error) {
    if (!isConfirmDismissed(error)) {
      proxy.$modal.msgError("删除失败：" + (error?.message || error || "请稍后重试"));
    }
  } finally {
    deleting.value = false;
  }
}

/** 导出按钮操作 */
function handleExport() {
  const params = { ...queryParams.value };
  // 处理时间范围
  if (params.paymentTimeRange && params.paymentTimeRange.length === 2) {
    params.beginPaymentTime = params.paymentTimeRange[0];
    params.endPaymentTime = params.paymentTimeRange[1];
    delete params.paymentTimeRange;
  }
  
  proxy.download('system/info/export', params, `info_${new Date().getTime()}.xlsx`)
}

function isConfirmDismissed(error) {
  return error === "cancel" || error === "close";
}

/** 单笔发货 */
async function handleShipOrder(row) {
  if (batchShipping.value || shippingOrderNo.value !== null) return;
  shippingOrderNo.value = row.orderNo;
  try {
    await proxy.$modal.confirm(`确认要将订单“${row.orderNo}”发货吗？`);
    shippingSubmitting.value = true;
    await shipInfo(row.orderNo);
    proxy.$modal.msgSuccess("发货成功！");
    await getList();
  } catch (error) {
    if (!isConfirmDismissed(error)) {
      proxy.$modal.msgError("发货失败：" + (error?.message || error || "请稍后重试"));
    }
  } finally {
    shippingSubmitting.value = false;
    shippingOrderNo.value = null;
  }
}

/** 批量发货操作 */
async function handleBatchShip() {
  if (batchShipping.value || shippingOrderNo.value !== null) return;
  // 筛选出待发货的订单
  const shippableOrders = selectedRows.value.filter(row => row.status === 20);
  
  if (shippableOrders.length === 0) {
    proxy.$modal.msgWarning("请选择待发货的订单");
    return;
  }
  
  const orderNos = shippableOrders.map(order => order.orderNo).join(', ');
  batchShipping.value = true;
  try {
    await proxy.$modal.confirm(`确认要批量发货以下 ${shippableOrders.length} 个订单吗？\n${orderNos}`);
    let successCount = 0;
    let failCount = 0;
    
    for (const order of shippableOrders) {
      try {
        await shipInfo(order.orderNo);
        successCount++;
      } catch (error) {
        failCount++;
      }
    }
    
    if (successCount > 0) {
      proxy.$modal.msgSuccess(`成功发货 ${successCount} 个订单` + (failCount > 0 ? `，失败 ${failCount} 个` : ''));
    } else {
      proxy.$modal.msgError("发货失败");
    }
    await getList();
  } catch (error) {
    if (!isConfirmDismissed(error)) {
      proxy.$modal.msgError("批量发货失败：" + (error?.message || error || "请稍后重试"));
    }
  } finally {
    batchShipping.value = false;
  }
}

/** 获取状态标签类型 */
function getStatusTagType(status) {
  switch (status) {
    case 10: return 'danger';   // 待付款
    case 20: return 'warning';  // 待发货
    case 30: return 'primary';  // 待收货
    case 40: return 'success';  // 待评价
    case 50: return 'info';     // 交易完成
    case 60: return '';         // 已取消
    default: return '';
  }
}

/** 获取状态文本 */
function getStatusText(status) {
  switch (status) {
    case 10: return '待付款';
    case 20: return '待发货';
    case 30: return '待收货';
    case 40: return '待评价';
    case 50: return '交易完成';
    case 60: return '已取消';
    default: return '未知';
  }
}

/** 获取支付类型标签类型 */
function getPaymentTagType(paymentType) {
  return getPaymentTypeTag(paymentType)
}

/** 获取支付类型文本 */
function getPaymentText(paymentType) {
  return getPaymentTypeLabel(paymentType)
}

// 组件挂载时加载数据
onMounted(() => {
  getList();
});
</script>

<style scoped>
/* 容器基础样式 */
.order-management {
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

/* 筛选条件标签 */
.filter-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 0;
  margin-top: 8px;
  border-top: 1px dashed #e4e7ed;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-tags :deep(.el-tag) {
  font-size: 14px;
  padding: 8px 16px;
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

/* 价格文字 */
.price-text {
  color: #e4393c;
  font-weight: 600;
  font-size: 14px;
}

/* 收货人信息 */
.receiver-info {
  line-height: 1.8;
}

.receiver-name,
.receiver-phone {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.receiver-name {
  font-weight: 500;
  color: #303133;
}

.receiver-phone {
  color: #909399;
}

/* 地址文字 */
.address-text {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.address-text .el-icon {
  margin-top: 3px;
  color: #909399;
  flex-shrink: 0;
}

/* 时间文字 */
.time-text {
  color: #909399;
  font-size: 13px;
}

/* 操作列 */
.action-column {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* 分页包裹 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 4px;
}

/* 弹窗样式 */
.order-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
  max-height: 65vh;
  overflow-y: auto;
}

.order-form .form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.order-form .form-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.form-section .section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 3px solid #409eff;
}

.order-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.order-form :deep(.el-form-item__label) {
  font-weight: 500;
}

/* 弹窗底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
}

/* 响应式调整 */
@media screen and (max-width: 1400px) {
  .table-card :deep(.el-table .cell) {
    padding: 0 12px;
  }
}
</style>
