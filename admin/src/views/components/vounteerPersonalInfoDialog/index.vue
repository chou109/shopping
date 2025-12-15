<template>
  <el-dialog :title="title" v-model="open" width="80%" append-to-body :close-on-click-modal="false" :show-close="false" @open="handleOpen">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="志愿者姓名" prop="volunteerName">
        <el-input
          v-model="queryParams.volunteerName"
          placeholder="请输入志愿者姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="volunteerGender">
        <el-select v-model="queryParams.volunteerGender" placeholder="请选择性别" clearable style="width: 200px">
          <el-option
            v-for="dict in sex_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="申请状态" prop="applicationStatus">
        <el-select v-model="queryParams.applicationStatus" placeholder="请选择申请状态" clearable style="width: 200px">
          <el-option
            v-for="dict in application_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Close" @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="applicationApprovalList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="70px" />
   
      <el-table-column label="志愿者姓名" align="center" prop="volunteerName" width="120px"/>
      <el-table-column label="性别" align="center" prop="volunteerGender" width="110px">
        <template #default="scope">
          <dict-tag :options="sex_type" :value="scope.row.volunteerGender"/>
        </template>
      </el-table-column>
      <el-table-column label="电话" align="center" prop="phone" width="130px"/>
  
      <el-table-column label="资格审查" align="center" prop="qualificationReview" />
      <el-table-column label="分配信息" align="center" prop="assignmentInfo" />
      <el-table-column label="申请状态" align="center" prop="applicationStatus" width="120px">
        <template #default="scope">
          <dict-tag :options="application_status" :value="scope.row.applicationStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleSelect(scope.row)">选择</el-button>
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

  </el-dialog>
</template>

<script setup name="ApplicationApproval">
import { listApplicationApproval } from "@/api/volunteer/applicationApproval";
const { proxy } = getCurrentInstance();
const { sex_type, application_status } = proxy.useDict('sex_type', 'application_status');

const applicationApprovalList = ref([]);

const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);

const total = ref(0);
const open = ref(false);
const title = ref("选择运动员个人信息档案");
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  // 弹出层标题
  title: {
    type: String,
    default: "选择运动员个人信息档案",
  },
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    volunteerName: null,
    volunteerGender: null,
    applicationStatus: null,
  },
  rules: {
  }
});

const emit = defineEmits(['handleClose','onSelected']);
const selectedList = ref([]);

function handleOpen() {
  getList();
}

import { toRaw } from 'vue';
const handleSelect = (row) => {
      if (row) {
        const rawRow = toRaw(row);
        console.log(rawRow);
        selectedList.value = [rawRow]; 
        console.log(selectedList.value);
        // 将原始对象放入数组
        emit('onSelected', selectedList.value); // 发射 onSelected 事件
      }
      cancel();
    };
watch(
  () => props.open,
  (val) => {
    if (val) {
      open.value = val;
    }
  }
);
const { queryParams} = toRefs(data);

/** 查询申请与审批列表 */
function getList() {
  loading.value = true;
  listApplicationApproval(queryParams.value).then(response => {
    applicationApprovalList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  selectedList.value = [];
  resetQuery();
  emit('handleClose');
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



getList();
</script>
