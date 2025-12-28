<template>
  <div class="app-container">
    <!-- 基础查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" 
      class="search-form compact-form">
      <el-row :gutter="20">
        <!-- 第一行：基础查询条件 -->
        <el-col :span="6">
          <el-form-item label="订单编号" prop="orderNo" label-width="70px" class="compact-item">
            <el-input
              v-model="queryParams.orderNo"
              placeholder="请输入订单编号"
              clearable
              @keyup.enter="handleQuery"
              class="compact-input"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="购买人ID" prop="userId" label-width="70px" class="compact-item">
            <el-input
              v-model="queryParams.userId"
              placeholder="请输入购买人ID"
              clearable
              @keyup.enter="handleQuery"
              class="compact-input"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
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
            <el-form-item label="订单状态" prop="status" label-width="70px" class="compact-item">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 180px">
                <el-option label="全部" :value="null" />
                <el-option label="待付款" :value="0" />
                <el-option label="已付款" :value="1" />
                <el-option label="已发货" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已取消" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付类型" prop="paymentType" label-width="70px" class="compact-item">
              <el-select v-model="queryParams.paymentType" placeholder="全部类型" clearable style="width: 180px">
                <el-option label="全部" :value="null" />
                <el-option label="在线支付" :value="1" />
                <el-option label="货到付款" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收货人" prop="receiveName" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.receiveName"
                placeholder="请输入收货人"
                clearable
                style="width: 180px"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="联系电话" prop="receivePhone" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.receivePhone"
                placeholder="请输入联系电话"
                clearable
                style="width: 180px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付金额范围" label-width="70px" class="compact-item">
              <div style="display: flex; align-items: center; width: 180px">
                <el-input-number 
                  v-model="queryParams.minPayment" 
                  placeholder="最低金额" 
                  :min="0" 
                  :precision="2"
                  :controls="false" 
                  style="flex: 1" 
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number 
                  v-model="queryParams.maxPayment" 
                  placeholder="最高金额" 
                  :min="0" 
                  :precision="2"
                  :controls="false" 
                  style="flex: 1" 
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付时间" prop="paymentTimeRange" label-width="70px" class="compact-item">
              <el-date-picker
                v-model="queryParams.paymentTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 180px"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="省份" prop="province" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.province"
                placeholder="请输入省份"
                clearable
                style="width: 180px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city" label-width="70px" class="compact-item">
              <el-input
                v-model="queryParams.city"
                placeholder="请输入城市"
                clearable
                style="width: 180px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="删除状态" prop="deleteFlag" label-width="70px" class="compact-item">
              <el-select v-model="queryParams.deleteFlag" placeholder="全部" clearable style="width: 180px">
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
          v-hasPermi="['system:info:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:info:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:info:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:info:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="infoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" width="80" />
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="150" />
      <el-table-column label="购买人ID" align="center" prop="userId" width="100" />
      <el-table-column label="支付金额" align="center" prop="payment" width="120">
        <template #default="scope">
          <span style="color: #e4393c; font-weight: bold;">¥{{ scope.row.payment || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付类型" align="center" prop="paymentType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.paymentType === 1 ? 'success' : 'warning'" size="small">
            {{ scope.row.paymentType === 1 ? '在线支付' : scope.row.paymentType === 2 ? '货到付款' : scope.row.paymentTypeDesc || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="邮费" align="center" prop="postage" width="80">
        <template #default="scope">
          <span>¥{{ scope.row.postage || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收货人" align="center" prop="receiveName" width="100" />
      <el-table-column label="联系电话" align="center" prop="receivePhone" width="120" />
      <el-table-column label="收货地址" align="center" width="180">
        <template #default="scope">
          <div class="address-info">
            {{ scope.row.province || '' }}{{ scope.row.city || '' }}{{ scope.row.area || '' }}
            <div class="street-info">{{ scope.row.street || '' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="paymentTime" width="150">
        <template #default="scope">
          <span>{{ parseTime(scope.row.paymentTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:info:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:info:remove']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 0 ? '标记付款' : scope.row.status === 1 ? '标记发货' : '订单详情'" placement="top">
            <el-button 
              link 
              :type="scope.row.status === 0 ? 'success' : scope.row.status === 1 ? 'warning' : 'info'" 
              :icon="scope.row.status === 0 ? 'Money' : scope.row.status === 1 ? 'Truck' : 'View'"
              @click="handleOrderAction(scope.row)" 
              v-hasPermi="['system:info:edit']"
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

    <!-- 添加或修改订单对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="infoRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单编号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="请输入订单编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买人ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入购买人ID" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="支付金额" prop="payment">
              <el-input-number 
                v-model="form.payment" 
                placeholder="请输入支付金额" 
                :min="0" 
                :precision="2"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮费" prop="postage">
              <el-input-number 
                v-model="form.postage" 
                placeholder="请输入邮费" 
                :min="0" 
                :precision="2"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="支付类型" prop="paymentType">
              <el-select v-model="form.paymentType" placeholder="请选择支付类型" style="width: 100%">
                <el-option label="在线支付" :value="1" />
                <el-option label="货到付款" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择订单状态" style="width: 100%">
                <el-option label="待付款" :value="0" />
                <el-option label="已付款" :value="1" />
                <el-option label="已发货" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已取消" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiveName">
              <el-input v-model="form.receiveName" placeholder="请输入收货人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="receivePhone">
              <el-input v-model="form.receivePhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="请输入省份" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区" prop="area">
              <el-input v-model="form.area" placeholder="请输入区" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="详细地址" prop="street">
              <el-input v-model="form.street" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮编" prop="postalCode">
              <el-input v-model="form.postalCode" placeholder="请输入邮编" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付时间" prop="paymentTime">
              <el-date-picker
                v-model="form.paymentTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择支付时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.id">
          <el-col :span="12">
            <el-form-item label="删除状态" prop="deleteFlag">
              <el-select v-model="form.deleteFlag" placeholder="请选择删除状态" style="width: 100%">
                <el-option label="正常" :value="0" />
                <el-option label="已删除" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="Info">
import { listInfo, getInfo, delInfo, addInfo, updateInfo } from "@/api/system/info";
import { ArrowDown } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const infoList = ref([]);
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
    orderNo: null,
    userId: null,
    // 高级查询条件
    status: null,
    paymentType: null,
    receiveName: null,
    receivePhone: null,
    province: null,
    city: null,
    deleteFlag: null,
    // 范围查询
    minPayment: null,
    maxPayment: null,
    // 时间范围
    paymentTimeRange: null,
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
      { required: true, message: "联系电话不能为空", trigger: "blur" }
    ],
    street: [
      { required: true, message: "详细地址不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询订单列表 */
function getList() {
  loading.value = true;
  
  const params = { ...queryParams.value };
  
  // 调试：查看请求参数
  console.log('请求参数:', JSON.stringify(params, null, 2));
  
  // ... 处理参数的代码 ...
  
  listInfo(params).then(response => {
    // 调试：查看响应数据
    console.log('响应数据:', {
      rows: response.rows?.length || 0,
      total: response.total,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    });
    
    infoList.value = response.rows;
    total.value = response.total;  // 这里应该是正确的总记录数
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
    paymentType: null,
    receiveName: null,
    receivePhone: null,
    province: null,
    city: null,
    deleteFlag: null,
    minPayment: null,
    maxPayment: null,
    paymentTimeRange: null,
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
  title.value = "添加订单";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value[0];
  getInfo(_id).then(response => {
    form.value = response.data;
    // 确保必要字段有默认值
    if (form.value.status === undefined || form.value.status === null) {
      form.value.status = 0;
    }
    if (form.value.deleteFlag === undefined || form.value.deleteFlag === null) {
      form.value.deleteFlag = 0;
    }
    if (form.value.paymentType === undefined || form.value.paymentType === null) {
      form.value.paymentType = 1;
    }
    open.value = true;
    title.value = "修改订单";
  });
}


/** 提交按钮 */
/** 提交按钮 */
function submitForm() {
  proxy.$refs["infoRef"].validate(valid => {
    if (valid) {
      // 如果是修改操作
      if (form.value.id != null) {
        updateInfo(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();  // 修改成功后重新加载当前页
        }).catch(error => {
          proxy.$modal.msgError("修改失败：" + (error.message || "未知错误"));
        });
      } else {
        // 新增操作 - 生成唯一的订单编号
        if (!form.value.orderNo) {
          form.value.orderNo = generateOrderNo();
        }
        
        addInfo(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          
          // 关键修复：新增后回到第一页重新查询
          queryParams.value.pageNum = 1;
          getList();
        }).catch(error => {
          proxy.$modal.msgError("新增失败：" + (error.message || "未知错误"));
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id ? [row.id] : ids.value;
  proxy.$modal.confirm('是否确认删除订单编号为"' + _ids.join(',') + '"的数据项？').then(function() {
    return delInfo(_ids);
  }).then(() => {
    // 删除后重新查询数据
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
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

/** 处理订单操作（付款、发货等） */
function handleOrderAction(row) {
  if (row.status === 0) {
    // 标记付款
    proxy.$modal.confirm(`确认要将订单"${row.orderNo}"标记为已付款吗？`).then(() => {
      const orderData = { ...row, status: 1 };
      return updateInfo(orderData);
    }).then(response => {
      proxy.$modal.msgSuccess("标记付款成功");
      getList();
    }).catch(() => {});
  } else if (row.status === 1) {
    // 标记发货
    proxy.$modal.confirm(`确认要将订单"${row.orderNo}"标记为已发货吗？`).then(() => {
      const orderData = { ...row, status: 2 };
      return updateInfo(orderData);
    }).then(response => {
      proxy.$modal.msgSuccess("标记发货成功");
      getList();
    }).catch(() => {});
  }
}

/** 获取状态标签类型 */
function getStatusTagType(status) {
  switch (status) {
    case 0: return 'danger'; // 待付款
    case 1: return 'success'; // 已付款
    case 2: return 'warning'; // 已发货
    case 3: return 'info'; // 已完成
    case 4: return ''; // 已取消
    default: return '';
  }
}

/** 获取状态文本 */
function getStatusText(status) {
  switch (status) {
    case 0: return '待付款';
    case 1: return '已付款';
    case 2: return '已发货';
    case 3: return '已完成';
    case 4: return '已取消';
    default: return '未知';
  }
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

.address-info {
  font-size: 12px;
  line-height: 1.4;
}

.street-info {
  color: #666;
  margin-top: 2px;
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
</style>