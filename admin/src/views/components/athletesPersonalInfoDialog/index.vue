<template>
  <el-dialog :title="title" v-model="open" width="80%" append-to-body :close-on-click-modal="false" :show-close="false" @open="handleOpen">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="运动员姓名" prop="athleteName">
        <el-input
          v-model="queryParams.athleteName"
          placeholder="请输入运动员姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="athleteGender">
        <el-select v-model="queryParams.athleteGender" placeholder="请选择性别" clearable style="width: 200px">
          <el-option
            v-for="dict in sex_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="年龄" prop="athleteAge">
        <el-input
          v-model="queryParams.athleteAge"
          placeholder="请输入年龄"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="国籍" prop="athleteNationality">
        <el-input
          v-model="queryParams.athleteNationality"
          placeholder="请输入国籍"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="健康状况" prop="healthStatus">
        <el-select v-model="queryParams.healthStatus" placeholder="请选择健康状况" clearable style="width: 200px">
          <el-option
            v-for="dict in health_status"
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

 
    <el-table v-loading="loading" :data="athletesPersonalInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id"  width="70px"/>
     
      <el-table-column label="运动员姓名" align="center" prop="athleteName" />
      <el-table-column label="性别" align="center" prop="athleteGender" width="100px">
        <template #default="scope">
          <dict-tag :options="sex_type" :value="scope.row.athleteGender"/>
        </template>
      </el-table-column>
      <el-table-column label="年龄" align="center" prop="athleteAge"  width="100px"/>
      <el-table-column label="身高" align="center" prop="athleteHeight"  width="100px"/>
      <el-table-column label="体重" align="center" prop="athleteWeight"  width="100px"/>
      <el-table-column label="国籍" align="center" prop="athleteNationality"  width="100px"/>
      <el-table-column label="成绩历史" align="center" prop="performanceHistory" />
      <el-table-column label="健康状况" align="center" prop="healthStatus">
        <template #default="scope">
          <dict-tag :options="health_status" :value="scope.row.healthStatus"/>
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

<script setup name="AthletesPersonalInfo">
import { listAthletesPersonalInfo } from "@/api/athletes/athletesPersonalInfo";

const { proxy } = getCurrentInstance();
const { sex_type, health_status } = proxy.useDict('sex_type', 'health_status');

const athletesPersonalInfoList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const total = ref(0);
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
    athleteName: null,
    athleteGender: null,
    athleteAge: null,
    athleteNationality: null,
    healthStatus: null
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
        selectedList.value = [rawRow]; 
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

/** 查询运动员个人信息管理列表 */
function getList() {
  loading.value = true;
  listAthletesPersonalInfo(queryParams.value).then(response => {
    athletesPersonalInfoList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
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
