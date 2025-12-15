<template>
  <!-- 创建表 -->
  <el-dialog title="创建表" v-model="visible" width="800px" top="5vh" append-to-body>
    <span>创建表语句(支持多个建表语句)：</span>
    <el-input type="textarea" :rows="10" placeholder="请输入文本" v-model="content"></el-input>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleCreateTable">确 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { createTable } from "@/api/tool/gen";
import { modal } from "@/plugins/index";
const emit = defineEmits(["ok"]);
// 遮罩层
const visible = ref(false);
// 文本内容
const content = ref("");

/** 创建按钮操作 */
function handleCreateTable() {
  if (this.content === "") {
    modal.msgError("请输入建表语句");
    return;
  }
  createTable({ sql: content.value }).then(res => {
    modal.msgSuccess(res.msg);
    if (res.code === 200) {
      visible.value = false;
      emit("ok");
    }
  });
}

// 显示弹框
function show() {
  visible.value = true;
}
defineExpose({
  show,
});
</script>