<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header>
            <span>缓存列表</span>
            <el-button
              style="float: right; padding: 3px 0"
              link
              type="primary"
              icon="Refresh"
              @click="refreshCacheNames()"
            ></el-button>
          </template>
          <el-table
            v-loading="loading"
            :data="cacheNames"
            :height="tableHeight"
            highlight-current-row
            @row-click="getCacheKeys"
            style="width: 100%"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>

            <el-table-column
              label="缓存名称"
              align="center"
              prop="cacheName"
              :show-overflow-tooltip="true"
              :formatter="nameFormatter"
            ></el-table-column>

            <el-table-column
              label="备注"
              align="center"
              prop="remark"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  icon="Delete"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-alert v-if="cacheNamesError" :title="cacheNamesError" type="error" :closable="false" show-icon>
            <template #default><el-button link type="primary" :disabled="loading" @click="refreshCacheNames">重试</el-button></template>
          </el-alert>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header>
            <span>键名列表</span>
            <el-button
              style="float: right; padding: 3px 0"
              link
              type="primary"
              icon="Refresh"
              @click="refreshCacheKeys()"
            ></el-button>
          </template>
          <el-table
            v-loading="subLoading"
            :data="cacheKeys"
            :height="tableHeight"
            highlight-current-row
            @row-click="handleCacheValue"
            style="width: 100%"
          >
            <el-table-column
              label="序号"
              width="60"
              type="index"
            ></el-table-column>
            <el-table-column
              label="缓存键名"
              align="center"
              :show-overflow-tooltip="true"
              :formatter="keyFormatter"
            >
            </el-table-column>
            <el-table-column
              label="操作"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  icon="Delete"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-alert v-if="cacheKeysError" :title="cacheKeysError" type="error" :closable="false" show-icon>
            <template #default><el-button link type="primary" :disabled="subLoading" @click="refreshCacheKeys">重试</el-button></template>
          </el-alert>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card :bordered="false" style="height: calc(100vh - 125px)">
          <template #header>
            <span>缓存内容</span>
            <el-button
              style="float: right; padding: 3px 0"
              link
              type="primary"
              icon="Refresh"
              @click="handleClearCacheAll()"
              >清理全部</el-button
            >
          </template>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存名称:" prop="cacheName">
                  <el-input v-model="cacheForm.cacheName" :readOnly="true" />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存键名:" prop="cacheKey">
                  <el-input v-model="cacheForm.cacheKey" :readOnly="true" />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存内容:" prop="cacheValue">
                  <el-input
                    v-model="cacheForm.cacheValue"
                    type="textarea"
                    :rows="8"
                    :readOnly="true"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-alert v-if="cacheValueError" :title="cacheValueError" type="error" :closable="false" show-icon>
            <template #default><el-button link type="primary" @click="retryCacheValue">重试</el-button></template>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CacheList">
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache";

const { proxy } = getCurrentInstance();

const cacheNames = ref([]);
const cacheKeys = ref([]);
const cacheForm = ref({});
const loading = ref(true);
const subLoading = ref(false);
const cacheNamesError = ref("");
const cacheKeysError = ref("");
const cacheValueError = ref("");
const nowCacheName = ref("");
const tableHeight = ref(window.innerHeight - 200);
let cacheNamesRequestId = 0;
let cacheKeysRequestId = 0;
let cacheValueRequestId = 0;
let selectedCacheKey = "";

/** 查询缓存名称列表 */
async function getCacheNames() {
  const requestId = ++cacheNamesRequestId;
  loading.value = true;
  cacheNamesError.value = "";
  try {
    const response = await listCacheName();
    if (requestId !== cacheNamesRequestId) return false;
    if (!Array.isArray(response.data)) throw new Error("缓存名称列表格式异常");
    cacheNames.value = response.data;
    return true;
  } catch (error) {
    if (requestId === cacheNamesRequestId) {
      console.error("获取缓存名称列表失败:", error);
      cacheNamesError.value = "缓存名称列表加载失败，请重试";
    }
    return false;
  } finally {
    if (requestId === cacheNamesRequestId) loading.value = false;
  }
}

/** 刷新缓存名称列表 */
async function refreshCacheNames() {
  if (await getCacheNames()) proxy.$modal.msgSuccess("刷新缓存列表成功");
}

/** 清理指定名称缓存 */
async function handleClearCacheName(row) {
  try {
    await clearCacheName(row.cacheName);
    proxy.$modal.msgSuccess("清理缓存名称[" + row.cacheName + "]成功");
    if (nowCacheName.value === row.cacheName) {
      nowCacheName.value = "";
      cacheKeys.value = [];
      cacheForm.value = {};
    }
    await getCacheNames();
  } catch (error) {
    console.error("清理缓存名称失败:", error);
    proxy.$modal.msgError("清理缓存名称失败");
  }
}

/** 查询缓存键名列表 */
async function getCacheKeys(row) {
  const cacheName = row !== undefined ? row.cacheName : nowCacheName.value;
  if (cacheName === "") {
    return;
  }
  const requestId = ++cacheKeysRequestId;
  cacheValueRequestId += 1;
  selectedCacheKey = "";
  nowCacheName.value = cacheName;
  subLoading.value = true;
  cacheKeysError.value = "";
  cacheForm.value = {};
  try {
    const response = await listCacheKey(cacheName);
    if (requestId !== cacheKeysRequestId) return;
    if (!Array.isArray(response.data)) throw new Error("缓存键名列表格式异常");
    cacheKeys.value = response.data;
  } catch (error) {
    if (requestId === cacheKeysRequestId) {
      console.error("获取缓存键名列表失败:", error);
      cacheKeys.value = [];
      cacheKeysError.value = "缓存键名列表加载失败，请重试";
    }
  } finally {
    if (requestId === cacheKeysRequestId) subLoading.value = false;
  }
}

/** 刷新缓存键名列表 */
async function refreshCacheKeys() {
  await getCacheKeys();
  if (!cacheKeysError.value) proxy.$modal.msgSuccess("刷新键名列表成功");
}

/** 清理指定键名缓存 */
async function handleClearCacheKey(cacheKey) {
  try {
    await clearCacheKey(cacheKey);
    proxy.$modal.msgSuccess("清理缓存键名[" + cacheKey + "]成功");
    await getCacheKeys();
  } catch (error) {
    console.error("清理缓存键名失败:", error);
    proxy.$modal.msgError("清理缓存键名失败");
  }
}

/** 列表前缀去除 */
function nameFormatter(row) {
  return row.cacheName.replace(":", "");
}

/** 键名前缀去除 */
function keyFormatter(cacheKey) {
  return cacheKey.replace(nowCacheName.value, "");
}

/** 查询缓存内容详细 */
async function handleCacheValue(cacheKey) {
  const requestId = ++cacheValueRequestId;
  selectedCacheKey = cacheKey;
  cacheValueError.value = "";
  try {
    const response = await getCacheValue(nowCacheName.value, cacheKey);
    if (requestId !== cacheValueRequestId) return;
    if (!response?.data || typeof response.data !== "object") throw new Error("缓存内容格式异常");
    cacheForm.value = response.data;
  } catch (error) {
    if (requestId === cacheValueRequestId) {
      console.error("获取缓存内容失败:", error);
      cacheValueError.value = "缓存内容加载失败，请重试";
    }
  }
}

function retryCacheValue() {
  if (selectedCacheKey) handleCacheValue(selectedCacheKey);
}

/** 清理全部缓存 */
async function handleClearCacheAll() {
  try {
    await clearCacheAll();
    cacheKeys.value = [];
    cacheForm.value = {};
    proxy.$modal.msgSuccess("清理全部缓存成功");
    await getCacheNames();
  } catch (error) {
    console.error("清理全部缓存失败:", error);
    proxy.$modal.msgError("清理全部缓存失败");
  }
}

getCacheNames();
onBeforeUnmount(() => {
  cacheNamesRequestId += 1;
  cacheKeysRequestId += 1;
  cacheValueRequestId += 1;
});
</script>
