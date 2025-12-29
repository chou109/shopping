<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="创建人" prop="createUser">
        <el-input
          v-model="queryParams.createUser"
          placeholder="请输入创建人"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="更新人" prop="updateUser">
        <el-input
          v-model="queryParams.updateUser"
          placeholder="请输入更新人"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="删除标志" prop="deleteFlag">
        <el-input
          v-model="queryParams.deleteFlag"
          placeholder="请输入删除标志"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺所有者用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入店铺所有者用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺名称" prop="shopName">
        <el-input
          v-model="queryParams.shopName"
          placeholder="请输入店铺名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺logo" prop="shopLogo">
        <el-input
          v-model="queryParams.shopLogo"
          placeholder="请输入店铺logo"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺横幅" prop="shopBanner">
        <el-input
          v-model="queryParams.shopBanner"
          placeholder="请输入店铺横幅"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          placeholder="请输入联系电话"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系地址" prop="contactAddress">
        <el-input
          v-model="queryParams.contactAddress"
          placeholder="请输入联系地址"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="店铺评分" prop="shopScore">
        <el-input
          v-model="queryParams.shopScore"
          placeholder="请输入店铺评分"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="销售数量" prop="salesCount">
        <el-input
          v-model="queryParams.salesCount"
          placeholder="请输入销售数量"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品数量" prop="productCount">
        <el-input
          v-model="queryParams.productCount"
          placeholder="请输入商品数量"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="粉丝数量" prop="followerCount">
        <el-input
          v-model="queryParams.followerCount"
          placeholder="请输入粉丝数量"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

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

    <el-table v-loading="loading" :data="storeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="创建人" align="center" prop="createUser" />
      <el-table-column label="更新人" align="center" prop="updateUser" />
      <el-table-column label="删除标志" align="center" prop="deleteFlag" />
      <el-table-column label="店铺所有者用户ID" align="center" prop="userId" />
      <el-table-column label="店铺名称" align="center" prop="shopName" />
      <el-table-column label="店铺logo" align="center" prop="shopLogo" />
      <el-table-column label="店铺横幅" align="center" prop="shopBanner" />
      <el-table-column label="店铺描述" align="center" prop="description" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="联系地址" align="center" prop="contactAddress" />
      <el-table-column label="店铺评分" align="center" prop="shopScore" />
      <el-table-column label="销售数量" align="center" prop="salesCount" />
      <el-table-column label="商品数量" align="center" prop="productCount" />
      <el-table-column label="粉丝数量" align="center" prop="followerCount" />
      <el-table-column label="店铺状态 1:正常 2:关闭 3:审核中" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:store:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:store:remove']">删除</el-button>
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
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="storeRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="创建人" prop="createUser">
          <el-input v-model="form.createUser" placeholder="请输入创建人" />
        </el-form-item>
        <el-form-item label="更新人" prop="updateUser">
          <el-input v-model="form.updateUser" placeholder="请输入更新人" />
        </el-form-item>
        <el-form-item label="删除标志" prop="deleteFlag">
          <el-input v-model="form.deleteFlag" placeholder="请输入删除标志" />
        </el-form-item>
        <el-form-item label="店铺所有者用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入店铺所有者用户ID" />
        </el-form-item>
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺logo" prop="shopLogo">
          <el-input v-model="form.shopLogo" placeholder="请输入店铺logo" />
        </el-form-item>
        <el-form-item label="店铺横幅" prop="shopBanner">
          <el-input v-model="form.shopBanner" placeholder="请输入店铺横幅" />
        </el-form-item>
        <el-form-item label="店铺描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="联系地址" prop="contactAddress">
          <el-input v-model="form.contactAddress" placeholder="请输入联系地址" />
        </el-form-item>
        <el-form-item label="店铺评分" prop="shopScore">
          <el-input v-model="form.shopScore" placeholder="请输入店铺评分" />
        </el-form-item>
        <el-form-item label="销售数量" prop="salesCount">
          <el-input v-model="form.salesCount" placeholder="请输入销售数量" />
        </el-form-item>
        <el-form-item label="商品数量" prop="productCount">
          <el-input v-model="form.productCount" placeholder="请输入商品数量" />
        </el-form-item>
        <el-form-item label="粉丝数量" prop="followerCount">
          <el-input v-model="form.followerCount" placeholder="请输入粉丝数量" />
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
import { listStore, getStore, delStore, addStore, updateStore } from "@/api/system/store";

const { proxy } = getCurrentInstance();

const storeList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
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
    createUser: null,
    updateUser: null,
    deleteFlag: null,
    userId: null,
    shopName: null,
    shopLogo: null,
    shopBanner: null,
    description: null,
    contactPhone: null,
    contactAddress: null,
    shopScore: null,
    salesCount: null,
    productCount: null,
    followerCount: null,
    status: null
  },
  rules: {
    createTime: [
      { required: true, message: "创建时间不能为空", trigger: "blur" }
    ],
    updateTime: [
      { required: true, message: "更新时间不能为空", trigger: "blur" }
    ],
    createUser: [
      { required: true, message: "创建人不能为空", trigger: "blur" }
    ],
    updateUser: [
      { required: true, message: "更新人不能为空", trigger: "blur" }
    ],
    deleteFlag: [
      { required: true, message: "删除标志不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "店铺所有者用户ID不能为空", trigger: "blur" }
    ],
    shopName: [
      { required: true, message: "店铺名称不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "店铺状态 1:正常 2:关闭 3:审核中不能为空", trigger: "change" }
    ],
    contactPhone: [
      { pattern: /^1[3-9]\d{9}$/, message: "请输入11位正确的手机号码", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询店铺信息列表 */
function getList() {
  loading.value = true;
  listStore(queryParams.value).then(response => {
    storeList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

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
    deleteFlag: null,
    userId: null,
    shopName: null,
    shopLogo: null,
    shopBanner: null,
    description: null,
    contactPhone: null,
    contactAddress: null,
    shopScore: null,
    salesCount: null,
    productCount: null,
    followerCount: null,
    status: null
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
  const _id = row.id || ids.value
  getStore(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改店铺信息";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["storeRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateStore(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addStore(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除店铺信息编号为"' + _ids + '"的数据项？').then(function() {
    return delStore(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}



/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/store/export', {
    ...queryParams.value
  }, `store_${new Date().getTime()}.xlsx`)
}

getList();
</script>
