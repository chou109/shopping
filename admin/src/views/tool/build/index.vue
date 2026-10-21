<template>
    <div class="app-container">
        <el-button type="primary" @click="dialogVisible = true">表单构建器</el-button>
        <el-dialog v-model="dialogVisible" width="100%" fullscreen @open="loadDesigner">
            <el-skeleton v-if="loadingDesigner" :rows="8" animated />
            <el-alert v-else-if="loadError" type="error" :title="loadError" :closable="false" />
            <v-form-designer v-else-if="designerReady" style="margin-left: 0;"></v-form-designer>
        </el-dialog>
    </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';

const dialogVisible = ref(false)
const designerReady = ref(false)
const loadingDesigner = ref(false)
const loadError = ref('')
const app = getCurrentInstance().appContext.app

async function loadDesigner() {
    if (designerReady.value || loadingDesigner.value) return

    loadingDesigner.value = true
    loadError.value = ''
    try {
        const [{ default: VForm3 }] = await Promise.all([
            import('vform3-builds'),
            import('vform3-builds/dist/designer.style.css')
        ])
        app.use(VForm3)
        designerReady.value = true
    } catch {
        loadError.value = '表单设计器加载失败，请关闭后重试。'
    } finally {
        loadingDesigner.value = false
    }
}
</script>

<style lang="scss">
</style>
