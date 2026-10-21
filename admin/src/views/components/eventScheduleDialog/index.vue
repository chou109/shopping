<template>
  <el-dialog :title="title" v-model="open" width="80%" append-to-body :close-on-click-modal="false" :show-close="false" @open="handleOpen">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="赛事名称" prop="eventName">
        <el-input
          v-model="queryParams.eventName"
          placeholder="请输入赛事名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="赛事日期" style="width: 308px">
        <el-date-picker
          v-model="daterangeEventDate"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="赛事时间" prop="eventTime">
        <el-time-picker clearable
          v-model="queryParams.eventTime"
           format="HH:mm:ss"
        value-format="HH:mm:ss"
          placeholder="请选择赛事时间">
        </el-time-picker>
      </el-form-item>
      <el-form-item label="赛事地点" prop="eventLocation">
        <el-input
          v-model="queryParams.eventLocation"
          placeholder="请输入赛事地点"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Close" @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>


    <el-table v-loading="loading" :data="eventScheduleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="70"/>
      <el-table-column label="赛事名称" align="center" prop="eventName"  width="190"/>
      <el-table-column label="赛事日期" align="center" prop="eventDate" width="150">
        <template #default="scope">
          <span>{{ parseTime(scope.row.eventDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="赛事时间" align="center" prop="eventTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.eventTime, '{h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="赛事地点" align="center" prop="eventLocation" width="160"/>
      <el-table-column label="备注" align="center" prop="remark" width="150"/>
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

<script setup name="EventSchedule">
import { listEventSchedule} from "@/api/competition/eventSchedule";

const { proxy } = getCurrentInstance();

const eventScheduleList = ref([]);
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

const daterangeEventDate = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    eventName: null,
    eventDate: null,
    eventTime: null,
    eventLocation: null,
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
/** 查询赛事管理-赛程规划列表 */
function getList() {
  loading.value = true;
  queryParams.value.params = {};
  if (null != daterangeEventDate && '' != daterangeEventDate) {
    queryParams.value.params["beginEventDate"] = daterangeEventDate.value[0];
    queryParams.value.params["endEventDate"] = daterangeEventDate.value[1];
  }
  listEventSchedule(queryParams.value).then(response => {
    eventScheduleList.value = response.rows;
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
  daterangeEventDate.value = [];
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
