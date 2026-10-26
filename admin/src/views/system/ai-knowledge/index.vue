<template>
  <div class="app-container gogo-knowledge">
    <el-card shadow="never" class="intro-card">
      <div class="intro-heading">
        <div>
          <h2>逛逛知识库</h2>
          <p>维护客服可引用的常见问题与商品资料。内容会作为知识资料提供给模型，不会作为系统指令执行。</p>
        </div>
        <el-button type="primary" :icon="Refresh" :loading="loadingMetrics" @click="loadMetrics">刷新统计</el-button>
      </div>
      <div class="metrics-row">
        <div class="metric"><span>会话请求</span><strong>{{ metrics.requests ?? 0 }}</strong></div>
        <div class="metric"><span>候选资料返回率</span><strong>{{ percent(metrics.retrievalHitRate) }}</strong></div>
        <div class="metric"><span>工具成功率</span><strong>{{ percent(metrics.toolSuccessRate) }}</strong></div>
        <div class="metric"><span>平均响应</span><strong>{{ Math.round(metrics.averageResponseMs || 0) }} ms</strong></div>
      </div>
      <div class="metrics-note">统计为当前后端进程累计值，重启后清零；候选资料返回率不代表答案已通过人工质量评估。</div>
    </el-card>

    <el-card shadow="never" class="knowledge-card">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索问题或答案" :prefix-icon="Search" class="keyword-input" @keyup.enter="loadKnowledge" />
        <div class="toolbar-actions">
          <el-button :icon="Refresh" :loading="loading" @click="loadKnowledge">刷新</el-button>
          <el-button v-hasPermi="['system:ai:knowledge:edit']" type="primary" :icon="Plus" @click="openCreate">新增知识</el-button>
        </div>
      </div>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" class="error-alert">
        <template #default><el-button link type="primary" :disabled="loading" @click="loadKnowledge">重试</el-button></template>
      </el-alert>

      <el-table v-loading="loading" :data="filteredRows" row-key="id" empty-text="暂无知识，可新增 FAQ 或商品说明">
        <el-table-column prop="question" label="问题 / 标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="answer" label="回答 / 内容" min-width="360">
          <template #default="{ row }"><div class="answer-cell">{{ row.answer }}</div></template>
        </el-table-column>
        <el-table-column label="适用范围" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.productId == null" type="success" effect="light">通用知识</el-tag>
            <span v-else>商品 #{{ row.productId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:ai:knowledge:edit']" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-hasPermi="['system:ai:knowledge:edit']" link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footnote">当前 FAQ 表没有启停/版本字段；更新会立即影响后续检索。每次检索最多向模型提供 5 条资料。</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识' : '新增知识'" width="min(680px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="问题 / 标题" prop="question">
          <el-input v-model="form.question" maxlength="255" show-word-limit placeholder="例如：如何申请退款？" />
        </el-form-item>
        <el-form-item label="回答 / 内容" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="8" maxlength="10000" show-word-limit placeholder="填写已核实的商城规则或商品说明。不要写入密钥、用户隐私或临时库存价格。" />
        </el-form-item>
        <el-form-item label="关联商品" prop="productId">
          <div class="scope-control">
            <el-checkbox v-model="isGlobal" @change="toggleGlobal">通用知识</el-checkbox>
            <el-select v-model="form.productId" filterable remote clearable reserve-keyword :remote-method="searchProducts"
              :loading="loadingProducts" :disabled="isGlobal" placeholder="搜索并选择商品" class="product-select">
              <el-option v-for="product in productOptions" :key="product.id" :label="`${product.name}（#${product.id}）`" :value="product.id" />
            </el-select>
          </div>
          <div class="field-hint">通用知识适用于所有商品；关联商品资料会在用户查看该商品时一并检索。列表只显示当前账号可访问的在售商品。</div>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="10000" :precision="0" controls-position="right" />
          <span class="field-hint inline-hint">数值越高越优先参与候选排序</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveKnowledge">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { addAiKnowledge, deleteAiKnowledge, getAiKnowledgeMetrics, listAiKnowledge, updateAiKnowledge } from '@/api/system/aiKnowledge'
import { listProduct } from '@/api/system/product'

const loading = ref(false)
const loadingMetrics = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const errorMessage = ref('')
const keyword = ref('')
const knowledgeRows = ref([])
const productOptions = ref([])
const formRef = ref()
const isGlobal = ref(true)
const loadingProducts = ref(false)
let productSearchRequestId = 0
const metrics = reactive({})
const form = reactive({ id: null, question: '', answer: '', productId: null, priority: 0 })
const rules = {
  question: [{ required: true, message: '请填写问题或标题', trigger: 'blur' }, { max: 255, message: '最多 255 个字符', trigger: 'blur' }],
  answer: [{ required: true, message: '请填写回答或内容', trigger: 'blur' }, { max: 10000, message: '最多 10000 个字符', trigger: 'blur' }]
}

const filteredRows = computed(() => {
  const query = keyword.value.trim().toLocaleLowerCase()
  if (!query) return knowledgeRows.value
  return knowledgeRows.value.filter(row => `${row.question || ''}\n${row.answer || ''}`.toLocaleLowerCase().includes(query))
})

function percent(value) {
  return `${Math.round((Number(value) || 0) * 100)}%`
}

async function loadKnowledge() {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await listAiKnowledge()
    if (response?.code !== 200 || !Array.isArray(response.data)) throw new Error(response?.msg || '知识库返回格式异常')
    knowledgeRows.value = response.data
  } catch (error) {
    errorMessage.value = error?.message || '知识库加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function loadMetrics() {
  loadingMetrics.value = true
  try {
    const response = await getAiKnowledgeMetrics()
    if (response?.code !== 200 || !response.data || typeof response.data !== 'object') throw new Error(response?.msg || '统计返回格式异常')
    Object.assign(metrics, response.data)
  } catch (error) {
    ElMessage.warning(error?.message || '统计加载失败')
  } finally {
    loadingMetrics.value = false
  }
}

function resetForm(row = null) {
  form.id = row?.id ?? null
  form.question = row?.question ?? ''
  form.answer = row?.answer ?? ''
  form.productId = row?.productId ?? null
  form.priority = row?.priority ?? 0
  isGlobal.value = row?.productId == null
  productOptions.value = row?.productId == null ? [] : [{ id: row.productId, name: `商品 #${row.productId}` }]
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm(row)
  dialogVisible.value = true
}

function toggleGlobal(value) {
  if (value) {
    form.productId = null
    productOptions.value = []
  }
}

async function searchProducts(query = '') {
  const requestId = ++productSearchRequestId
  loadingProducts.value = true
  try {
    const response = await listProduct({ pageNum: 1, pageSize: 30, status: 1, name: query.trim() })
    if (requestId !== productSearchRequestId) return
    if (response?.code !== 200 || !Array.isArray(response.rows)) throw new Error(response?.msg || '商品列表返回格式异常')
    const selected = productOptions.value.filter(product => product.id === form.productId)
    const matches = response.rows.map(product => ({ id: product.id, name: product.name || `商品 #${product.id}` }))
    productOptions.value = [...new Map([...selected, ...matches].map(product => [product.id, product])).values()]
  } catch (error) {
    if (requestId === productSearchRequestId) ElMessage.warning(error?.message || '商品搜索失败')
  } finally {
    if (requestId === productSearchRequestId) loadingProducts.value = false
  }
}

async function saveKnowledge() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!isGlobal.value && (!Number.isInteger(Number(form.productId)) || Number(form.productId) < 1)) {
    ElMessage.warning('请选择有效的商品，或切换为通用知识')
    return
  }
  const payload = {
    ...(form.id == null ? {} : { id: form.id }),
    question: form.question.trim(),
    answer: form.answer.trim(),
    productId: isGlobal.value ? null : Number(form.productId),
    priority: Number(form.priority) || 0
  }
  saving.value = true
  try {
    const response = form.id == null ? await addAiKnowledge(payload) : await updateAiKnowledge(payload)
    if (response?.code !== 200) throw new Error(response?.msg || '保存失败')
    ElMessage.success('知识已保存')
    dialogVisible.value = false
    await loadKnowledge()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除“${row.question}”吗？删除后该知识将不再参与检索。`, '删除知识', { type: 'warning' })
    const response = await deleteAiKnowledge(row.id)
    if (response?.code !== 200) throw new Error(response?.msg || '删除失败')
    ElMessage.success('知识已删除')
    await loadKnowledge()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.message || '删除失败，请稍后重试')
  }
}

onMounted(() => {
  loadKnowledge()
  loadMetrics()
})
</script>

<style scoped>
.intro-card, .knowledge-card { border: 1px solid var(--el-border-color-lighter); border-radius: 10px; }
.knowledge-card { margin-top: 16px; }
.intro-heading, .toolbar, .toolbar-actions, .metrics-row, .scope-control { display: flex; align-items: center; }
.intro-heading, .toolbar { justify-content: space-between; gap: 16px; }
.intro-heading h2 { margin: 0; font-size: 18px; color: var(--el-text-color-primary); }
.intro-heading p { margin: 7px 0 0; color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.6; }
.metrics-row { gap: 12px; margin-top: 18px; }
.metric { display: flex; flex: 1; flex-direction: column; gap: 6px; min-width: 120px; padding: 12px 14px; border-radius: 8px; background: var(--el-fill-color-light); }
.metric span, .metrics-note, .field-hint, .table-footnote { color: var(--el-text-color-secondary); font-size: 12px; }
.metric strong { color: var(--el-text-color-primary); font-size: 20px; font-weight: 600; }
.metrics-note { margin-top: 10px; line-height: 1.6; }
.toolbar { margin-bottom: 16px; }
.toolbar-actions { gap: 8px; }
.keyword-input { max-width: 360px; }
.error-alert { margin-bottom: 14px; }
.answer-cell { display: -webkit-box; overflow: hidden; line-height: 1.6; white-space: pre-line; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
.table-footnote { padding-top: 12px; line-height: 1.6; }
.scope-control { gap: 14px; }
.product-select { width: min(320px, 100%); }
.field-hint { display: block; margin-top: 4px; line-height: 1.5; }
.inline-hint { display: inline-block; margin: 0 0 0 10px; }
@media (max-width: 720px) {
  .intro-heading, .toolbar { align-items: stretch; flex-direction: column; }
  .metrics-row { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .keyword-input { max-width: none; }
  .toolbar-actions { justify-content: flex-end; }
}
</style>
