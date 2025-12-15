<template>
    <div class="app-container">
        <el-button type="primary" @click="dialogVisible = true">表单构建器</el-button>
        <el-dialog v-model="dialogVisible" width="100%" fullscreen>
            <v-form-designer ref="vfdRef" style="margin-left: 0;"></v-form-designer>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref } from 'vue';

const vfdRef = ref(null)
const dialogVisible = ref(false)
</script>

<style lang="scss">
</style>