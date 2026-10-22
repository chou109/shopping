<template>
  <div class="app-container product-management">
    <!-- 搜索区域卡片 -->
    <el-card class="search-card" shadow="hover" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter="handleQuery" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商品状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%" @change="handleQuery">
                <el-option label="全部" :value="null" />
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="热门状态" prop="hotFlag">
              <el-select v-model="queryParams.hotFlag" placeholder="全部" clearable style="width: 100%" @change="handleQuery">
                <el-option label="全部" :value="null" />
                <el-option label="热门" :value="1" />
                <el-option label="非热门" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item class="search-buttons">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button type="primary" link @click="toggleAdvanced">
                {{ showAdvanced ? '收起' : '展开' }}
                <el-icon :class="{ 'rotate-icon': showAdvanced }">
                  <ArrowDown />
                </el-icon>
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 高级查询条件 -->
        <el-collapse-transition>
          <div v-show="showAdvanced" class="advanced-search-section">
            <el-divider content-position="left">更多筛选</el-divider>
            <el-row :gutter="24">
              <el-col :span="6">
                <el-form-item label="商品ID" prop="productId">
                  <el-input v-model="queryParams.productId" placeholder="请输入商品ID" clearable style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="商品类型" prop="isBlindBox">
                  <el-select v-model="queryParams.isBlindBox" placeholder="全部类型" clearable style="width: 100%">
                    <el-option label="普通商品" :value="0" />
                    <el-option label="盲盒专属" :value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="品类" prop="categoryId">
                  <el-select v-model="queryParams.categoryId" placeholder="点击选择品类" clearable filterable
                    :loading="categoryTreeLoading" style="width: 150px">
                    <el-option label="全部" :value="null" />
                    <el-option v-for="category in categoryTreeOptions" :key="category.id" :label="category.name" :value="category.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>
      </el-form>
      
      <el-alert v-if="categoryTreeError" class="category-load-error" type="error" :closable="false"
        :title="categoryTreeError">
        <template #default>
          <el-button link type="primary" :disabled="categoryTreeLoading" @click="loadCategoryTree">重试加载分类</el-button>
        </template>
      </el-alert>

      <!-- 筛选条件标签展示 -->
      <div class="filter-tags" v-if="hasActiveFilters">
        <span class="filter-label">当前筛选：</span>
        <el-tag v-if="queryParams.status !== null" :type="queryParams.status === 1 ? 'success' : 'danger'" closable @close="clearFilter('status')" effect="dark" size="large">
          {{ queryParams.status === 1 ? '上架' : '下架' }}
        </el-tag>
        <el-tag v-if="queryParams.hotFlag !== null" :type="queryParams.hotFlag === 1 ? 'danger' : 'info'" closable @close="clearFilter('hotFlag')" effect="dark" size="large">
          {{ queryParams.hotFlag === 1 ? '热门' : '非热门' }}
        </el-tag>
        <el-tag v-if="queryParams.isBlindBox !== null" type="warning" closable @close="clearFilter('isBlindBox')" effect="dark" size="large">
          {{ queryParams.isBlindBox === 1 ? '盲盒专属' : '普通商品' }}
        </el-tag>
      </div>
    </el-card>

    <!-- 操作按钮区域 -->
    <div class="toolbar-wrapper">
      <div class="action-buttons">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:product:add']">新增</el-button>
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:product:edit']">修改</el-button>
        <el-button type="danger" plain icon="Delete"
          :disabled="multiple || deletingProductIds.size > 0 || statusUpdatingIds.size > 0 || hotUpdatingIds.size > 0"
          :loading="deletingProductIds.size > 0" @click="handleDelete" v-hasPermi="['system:product:remove']">删除</el-button>
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:product:export']">导出</el-button>
      </div>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </div>

    <!-- 数据表格卡片 -->
    <el-card class="table-card" shadow="hover">
    <el-alert v-if="listError" :title="listError" type="error" :closable="false" show-icon>
      <template #default><el-button link type="primary" @click="getList">重试</el-button></template>
    </el-alert>
    <el-table v-else ref="productTableRef" v-loading="loading" :data="productList" @selection-change="handleSelectionChange"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
      :row-style="{ height: '60px' }"
      stripe
      border
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="商品图片" align="center" prop="mainImage" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.mainImage" :width="50" :height="50" v-if="scope.row.mainImage" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" align="center" prop="name" min-width="150">
        <template #default="scope">
          <div class="product-name">{{ scope.row.name }}</div>
          <div class="product-subtitle" style="color: #999; font-size: 12px;">{{ scope.row.subTitle || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="品类" align="center" width="120">
        <template #default="scope">
          <el-tag size="small" v-if="scope.row.category">{{ scope.row.category.name }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center" prop="price" width="120">
        <template #default="scope">
          <span style="color: #e4393c; font-weight: bold;">¥{{ scope.row.price || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存" align="center" prop="stock" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.stock > 10 ? 'success' : scope.row.stock > 0 ? 'warning' : 'danger'" size="small">
            {{ scope.row.stock || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 0 ? 'danger' : 'warning'" size="small">
            {{ scope.row.status === 1 ? '上架' : scope.row.status === 0 ? '下架' : '状态未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品类型" align="center" prop="isBlindBox" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.isBlindBox === 1 ? 'warning' : 'info'" size="small">
            {{ scope.row.isBlindBox === 1 ? '盲盒专属' : '普通商品' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="热门" align="center" prop="hotFlag" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.hotFlag === 1 ? 'danger' : scope.row.hotFlag === 0 ? 'info' : 'warning'" size="small">
            {{ scope.row.hotFlag === 1 ? '热门' : scope.row.hotFlag === 0 ? '普通' : '状态未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
              :loading="productDetailLoadingId === scope.row.id"
              :aria-label="`修改商品：${scope.row.name || '未知商品'}`"
              v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
              :loading="deletingProductIds.has(scope.row.id)"
              :disabled="deletingProductIds.size > 0 || statusUpdatingIds.has(scope.row.id) || hotUpdatingIds.has(scope.row.id)"
              :aria-label="`删除商品：${scope.row.name || '未知商品'}`"
              v-hasPermi="['system:product:remove']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 1 ? '下架' : '上架'" placement="top">
            <el-button link :type="scope.row.status === 1 ? 'warning' : 'success'"
              :icon="scope.row.status === 1 ? 'Top' : 'Bottom'" @click="toggleStatus(scope.row)"
              :loading="statusUpdatingIds.has(scope.row.id)"
              :disabled="statusUpdatingIds.has(scope.row.id) || deletingProductIds.has(scope.row.id) || ![0, 1].includes(scope.row.status)"
              :aria-label="`${[0, 1].includes(scope.row.status) ? (scope.row.status === 1 ? '下架' : '上架') : '状态未知'}商品：${scope.row.name || '未知商品'}`"
              v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.hotFlag === 1 ? '取消热门' : '设为热门'" placement="top">
            <el-button link :type="scope.row.hotFlag === 1 ? 'danger' : 'info'" @click="toggleHot(scope.row)"
              :loading="hotUpdatingIds.has(scope.row.id)"
              :disabled="hotUpdatingIds.has(scope.row.id) || deletingProductIds.has(scope.row.id) || ![0, 1].includes(scope.row.hotFlag)"
              :aria-label="`${scope.row.hotFlag === 1 ? '取消热门' : '设为热门'}：${scope.row.name || '未知商品'}`"
              v-hasPermi="['system:product:edit']">
              <template #icon>
                <el-icon v-if="scope.row.hotFlag === 1">
                  <svg t="1766222883576" class="icon" viewBox="0 0 1084 1024" version="1.1"
                    xmlns="http://www.w3.org/2000/svg" p-id="8408" width="200" height="200">
                    <path
                      d="M665.720098 546.181518c-103.16924-223.888351-36.452732-295.781822-35.889736-296.681815 0.899993-0.899993 1.34999-2.811979 0.449997-4.611966-0.899993-1.34999-2.812979-1.799987-4.162969-1.34999-1.34999 0.449997-145.133931 60.303556-127.133064 193.511575-39.602708-35.551738-223.439355-217.588398-110.595186-431.802821 0.449997-1.34999 0.449997-2.811979-0.449997-4.161969-0.899993-0.899993-2.811979-1.34999-4.161969-0.899993-2.249983 0.899993-59.854559 25.76381-105.532223 87.529355-42.415688 58.054573-81.567399 159.423826-29.026786 316.033673 19.350858 73.692457-17.100874 133.659016-36.452732 158.97283 4.612966-17.550871 10.575922-47.027654 9.675929-81.117403-1.34999-49.728634-19.350858-118.358128-95.856294-163.135799-1.34999-0.449997-2.812979-0.449997-3.712973 0-1.34999 0.899993-1.799987 2.249983-1.34999 3.712973 0 1.34999 29.026786 132.196027-21.150844 192.611581C57.05558 567.333363 14.639892 658.126694 37.704722 742.394074c19.350858 70.542481 79.204417 124.433084 178.773684 159.872822 25.313814 7.875942 49.728634 13.838898 76.955433 18.901861 1.799987 0.449997 3.711973-0.449997 4.16197-2.249983 0.449997-1.799987-0.449997-3.712973-1.799987-4.612966-51.189623-22.163837-103.619237-68.629495-64.466525-162.235806 37.352725-81.117403 30.376776-122.520098 23.06383-140.521965 19.351858 11.025919 57.603576 39.152712 47.928647 88.881346-0.449997 1.34999 0.449997 2.811979 1.799987 3.712972 1.34999 0.899993 2.812979 0.449997 4.162969-0.449996s116.107145-95.856294 64.016529-212.414436c25.76381 14.850891 93.943308 67.392504 53.890603 186.649625-13.387901 51.64062 23.06383 88.431349 24.863817 89.893338 1.34999 1.34999 3.262976 1.34999 4.612966 0.449997 1.34999-0.899993 1.799987-2.812979 1.34999-4.162969-1.34999-3.261976-28.57679-76.954433 30.939772-117.457135-3.262976 18.900861-8.325939 65.479518 12.938905 91.243328 18.899861 23.06383 37.351725 60.303556 31.839765 97.206284-4.163969 25.76381-19.351858 47.47765-45.565664 64.466525-1.34999 0.899993-1.799987 2.812979-1.34999 4.612966 0.899993 1.799987 2.811979 2.362983 4.161969 1.799987 64.916522-21.71384 150.196894-64.466525 187.099622-146.033925 26.439805-59.404563 22.839832-131.296033-11.362916-213.764426z m0 0"
                      p-id="8409" fill="#d81e06"></path>
                  </svg>
                </el-icon>
                <el-icon v-else>
                  <svg t="1766222802360" class="icon" viewBox="0 0 1084 1024" version="1.1"
                    xmlns="http://www.w3.org/2000/svg" p-id="8255" width="200" height="200">
                    <path
                      d="M665.720098 546.181518c-103.16924-223.888351-36.452732-295.781822-35.889736-296.681815 0.899993-0.899993 1.34999-2.811979 0.449997-4.611966-0.899993-1.34999-2.812979-1.799987-4.162969-1.34999-1.34999 0.449997-145.133931 60.303556-127.133064 193.511575-39.602708-35.551738-223.439355-217.588398-110.595186-431.802821 0.449997-1.34999 0.449997-2.811979-0.449997-4.161969-0.899993-0.899993-2.811979-1.34999-4.161969-0.899993-2.249983 0.899993-59.854559 25.76381-105.532223 87.529355-42.415688 58.054573-81.567399 159.423826-29.026786 316.033673 19.350858 73.692457-17.100874 133.659016-36.452732 158.97283 4.612966-17.550871 10.575922-47.027654 9.675929-81.117403-1.34999-49.728634-19.350858-118.358128-95.856294-163.135799-1.34999-0.449997-2.812979-0.449997-3.712973 0-1.34999 0.899993-1.799987 2.249983-1.34999 3.712973 0 1.34999 29.026786 132.196027-21.150844 192.611581C57.05558 567.333363 14.639892 658.126694 37.704722 742.394074c19.350858 70.542481 79.204417 124.433084 178.773684 159.872822 25.313814 7.875942 49.728634 13.838898 76.955433 18.901861 1.799987 0.449997 3.711973-0.449997 4.16197-2.249983 0.449997-1.799987-0.449997-3.712973-1.799987-4.612966-51.189623-22.163837-103.619237-68.629495-64.466525-162.235806 37.352725-81.117403 30.376776-122.520098 23.06383-140.521965 19.351858 11.025919 57.603576 39.152712 47.928647 88.881346-0.449997 1.34999 0.449997 2.811979 1.799987 3.712972 1.34999 0.899993 2.812979 0.449997 4.162969-0.449996s116.107145-95.856294 64.016529-212.414436c25.76381 14.850891 93.943308 67.392504 53.890603 186.649625-13.387901 51.64062 23.06383 88.431349 24.863817 89.893338 1.34999 1.34999 3.262976 1.34999 4.612966 0.449997 1.34999-0.899993 1.799987-2.812979 1.34999-4.162969-1.34999-3.261976-28.57679-76.954433 30.939772-117.457135-3.262976 18.900861-8.325939 65.479518 12.938905 91.243328 18.899861 23.06383 37.351725 60.303556 31.839765 97.206284-4.163969 25.76381-19.351858 47.47765-45.565664 64.466525-1.34999 0.899993-1.799987 2.812979-1.34999 4.612966 0.899993 1.799987 2.811979 2.362983 4.161969 1.799987 64.916522-21.71384 150.196894-64.466525 187.099622-146.033925 26.439805-59.404563 22.839832-131.296033-11.362916-213.764426z m0 0"
                      p-id="8256"></path>
                  </svg>
                </el-icon>
              </template>
            </el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    </el-card>

    <!-- 分页组件 -->
    <div class="pagination-wrapper">
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加或修改商品对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body
      :close-on-click-modal="!formSubmitting" :close-on-press-escape="!formSubmitting"
      :show-close="!formSubmitting">
      <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="简要描述" prop="subTitle">
              <el-input v-model="form.subTitle" placeholder="请输入简要描述" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="点击选择品类" clearable filterable
                  :loading="categoryTreeLoading"
                  style="width: 200px" @click.stop>
                  <el-option label="全部" :value="null" />
                  <el-option v-for="category in categoryTreeOptions" :key="category.id" :label="category.name"
                    :value="category.id" />
                </el-select>
                <el-alert v-if="categoryTreeError" class="category-load-error" type="error" :closable="false"
                  :title="categoryTreeError">
                  <template #default>
                    <el-button link type="primary" :disabled="categoryTreeLoading" @click="loadCategoryTree">重试加载分类</el-button>
                  </template>
                </el-alert>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品单价" prop="price">
              <el-input-number v-model="form.price" placeholder="请输入商品单价" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数" prop="stock">
              <el-input-number v-model="form.stock" placeholder="请输入库存数" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择商品状态" style="width: 100%">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="热门状态" prop="hotFlag">
              <el-select v-model="form.hotFlag" placeholder="请选择热门状态" style="width: 100%">
                <el-option label="非热门" :value="0" />
                <el-option label="热门" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品类型" prop="isBlindBox">
              <el-select v-model="form.isBlindBox" placeholder="请选择商品类型" style="width: 100%">
                <el-option label="普通商品" :value="0" />
                <el-option label="盲盒专属" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主图" prop="mainImage">
              <image-upload v-model="form.mainImage" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副图" prop="subImages">
              <image-upload v-model="form.subImages" :limit="5" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="删除标志" prop="deleteFlag" v-if="form.id">
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
        <el-button type="primary" :loading="formSubmitting" :disabled="formSubmitting" @click="submitForm">确 定</el-button>
        <el-button :disabled="formSubmitting" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Product">
import { listProduct, getProduct, delProduct, addProduct, updateProduct, updateProductStatus, updateProductHotFlag } from "@/api/system/product";
import { listCategoryFront } from "@/api/system/category";
import { getMyShop } from "@/api/system/dashboard";
import { buildCategoryOptions, buildCategoryTree } from '@/utils/product-category.mjs';
import { ArrowDown } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance();

const productList = ref([]);
const productTableRef = ref(null);
const open = ref(false);
const loading = ref(true);
const listError = ref("");
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const categoryTree = ref([]);
const categoryTreeOptions = ref([]);
const categoryTreeLoading = ref(false);
const categoryTreeError = ref("");
const myShopId = ref(null);  // 当前用户的店铺ID
let productListRequestId = 0;
let categoryTreeRequestId = 0;

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    status: null,
    hotFlag: null,
    isBlindBox: null,
    productId: null,
    categoryId: null,
  },
  rules: {
    name: [
      { required: true, message: "商品名称不能为空", trigger: "blur" }
    ],
    price: [
      { required: true, message: "商品单价不能为空", trigger: "blur" }
    ],
    stock: [
      { required: true, message: "库存数不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "商品状态不能为空", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);
const productRef = ref(null);
const statusUpdatingIds = ref(new Set());
const hotUpdatingIds = ref(new Set());
const deletingProductIds = ref(new Set());
const formSubmitting = ref(false);
const productDetailLoadingId = ref(null);
let productDetailRequestId = 0;

// 计算属性：是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return queryParams.value.status !== null || queryParams.value.hotFlag !== null || queryParams.value.isBlindBox !== null;
});

/** 清除指定筛选 */
function clearFilter(field) {
  queryParams.value[field] = null;
  handleQuery();
}

/** 从后端加载分类树 */
async function loadCategoryTree() {
  const requestId = ++categoryTreeRequestId;
  categoryTreeLoading.value = true;
  categoryTreeError.value = "";
  try {
    const response = await listCategoryFront();
    if (requestId !== categoryTreeRequestId) return;
    if (response?.code !== 200 || !Array.isArray(response.data)) {
      throw new Error(response?.msg || '分类数据格式异常');
    }
    const categories = response.data;
    const firstItem = categories[0];
    if (firstItem && firstItem.children !== undefined) {
      categoryTree.value = categories;
    } else {
      categoryTree.value = buildCategoryTree(categories);
    }
    generateCategoryOptions();
  } catch (error) {
    if (requestId === categoryTreeRequestId) {
      console.error('加载分类树失败:', error);
      categoryTreeError.value = '商品分类加载失败，请重试';
    }
  } finally {
    if (requestId === categoryTreeRequestId) categoryTreeLoading.value = false;
  }
}

/** 将平铺数据转换为树形结构 */
function generateCategoryOptions() {
  categoryTreeOptions.value = buildCategoryOptions(categoryTree.value);
}

/** 查询商品列表 */
function getList() {
  const requestId = ++productListRequestId;
  loading.value = true;
  listError.value = "";
  productTableRef.value?.clearSelection();
  const userStore = useUserStore();
  const params = { ...queryParams.value };
  
  // user_id=1 是超级管理员，可查看所有商品
  // 其他用户只能查看自己店铺的商品
  if (userStore.userId !== 1 && myShopId.value) {
    params.shopId = myShopId.value;
  }

  Object.keys(params).forEach(key => {
    if (params[key] === '' || params[key] === null || params[key] === undefined) {
      delete params[key];
    }
  });

  return listProduct(params).then(response => {
    if (requestId !== productListRequestId) return;
    productList.value = response.rows || [];
    total.value = Number(response.total) || 0;
  }).catch(() => {
    if (requestId === productListRequestId) {
      listError.value = "商品加载失败，请重试";
    }
  }).finally(() => {
    if (requestId === productListRequestId) loading.value = false;
  });
}

// 切换高级筛选显示
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value;
};

// 取消按钮
function cancel() {
  productDetailRequestId++;
  productDetailLoadingId.value = null;
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
    productId: null,
    categoryId: null,
    name: null,
    subTitle: null,
    mainImage: null,
    subImages: null,
    activityId: null,
    status: 1,
    price: null,
    stock: null,
    hotFlag: 0,
    isBlindBox: 0
  };
  proxy.resetForm("productRef");
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
    name: null,
    status: null,
    hotFlag: null,
    isBlindBox: null,
    productId: null,
    categoryId: null,
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
  productDetailRequestId++;
  productDetailLoadingId.value = null;
  reset();
  open.value = true;
  title.value = "添加商品";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value[0];
  const requestId = ++productDetailRequestId;
  productDetailLoadingId.value = _id;
  getProduct(_id).then(response => {
    if (requestId !== productDetailRequestId) return;
    form.value = response.data;
    if (form.value.status === undefined || form.value.status === null) {
      form.value.status = 1;
    }
    if (form.value.hotFlag === undefined || form.value.hotFlag === null) {
      form.value.hotFlag = 0;
    }
    if (form.value.deleteFlag === undefined || form.value.deleteFlag === null) {
      form.value.deleteFlag = 0;
    }
    open.value = true;
    title.value = "修改商品";
  }).catch(error => {
    if (requestId === productDetailRequestId) console.error('加载商品详情失败:', error);
  }).finally(() => {
    if (requestId === productDetailRequestId) productDetailLoadingId.value = null;
  });
}

/** 提交按钮 */
const submitForm = async () => {
  if (formSubmitting.value || !productRef.value) return;
  formSubmitting.value = true;
  try {
    try {
      await productRef.value.validate();
    } catch {
      return;
    }
    const userStore = useUserStore();
    if (!userStore.userId) {
      ElMessage.error('无法获取用户信息，请重新登录');
      return;
    }
    form.value.createUser = userStore.userId;
    if (form.value.id != null) {
      await updateProduct(form.value);
      proxy.$modal.msgSuccess("修改成功");
    } else {
      await addProduct(form.value);
      proxy.$modal.msgSuccess("新增成功");
    }
    open.value = false;
    await getList();
  } finally {
    formSubmitting.value = false;
  }
};

/** 删除按钮操作 */
async function handleDelete(row) {
  if (deletingProductIds.value.size > 0) return;
  const userStore = useUserStore();
  if (!userStore.userId) {
    ElMessage.error('无法获取用户信息，请重新登录');
    return;
  }
  const _ids = row.id ? [row.id] : ids.value;
  if (_ids.length === 0) return;
  if (_ids.some(id => statusUpdatingIds.value.has(id) || hotUpdatingIds.value.has(id))) return;
  deletingProductIds.value = new Set(_ids);
  try {
    await proxy.$modal.confirm('是否确认删除商品编号为"' + _ids.join(',') + '"的数据项？');
    await delProduct(_ids);
    proxy.$modal.msgSuccess("删除成功");
    await getList();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') console.error('删除商品失败:', error);
  } finally {
    deletingProductIds.value = new Set();
  }
}

/** 导出按钮操作 */
function handleExport() {
  const params = { ...queryParams.value };
  proxy.download('system/product/export', params, `product_${new Date().getTime()}.xlsx`);
}

/** 切换商品状态 */
async function toggleStatus(row) {
  if (statusUpdatingIds.value.has(row.id) || deletingProductIds.value.has(row.id)) return;
  if (![0, 1].includes(row.status)) {
    ElMessage.warning('商品状态未知，请刷新列表后重试');
    return;
  }
  const newStatus = row.status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? '上架' : '下架';
  statusUpdatingIds.value = new Set([...statusUpdatingIds.value, row.id]);
  try {
    await proxy.$modal.confirm(`确认要${statusText}商品"${row.name}"吗？`);
    const response = await updateProductStatus(row.id, newStatus);
    if (response.code !== 200) throw new Error(response.msg || `${statusText}失败`);
    proxy.$modal.msgSuccess(`${statusText}成功`);
    await getList();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') console.error(`${statusText}商品失败:`, error);
  } finally {
    const ids = new Set(statusUpdatingIds.value);
    ids.delete(row.id);
    statusUpdatingIds.value = ids;
  }
}

/** 切换热门状态 */
async function toggleHot(row) {
  if (hotUpdatingIds.value.has(row.id) || deletingProductIds.value.has(row.id)) return;
  if (![0, 1].includes(row.hotFlag)) {
    ElMessage.warning('商品热门状态未知，请刷新列表后重试');
    return;
  }
  const newHotFlag = row.hotFlag === 1 ? 0 : 1;
  const hotText = newHotFlag === 1 ? '设为热门' : '取消热门';
  hotUpdatingIds.value = new Set([...hotUpdatingIds.value, row.id]);
  try {
    await proxy.$modal.confirm(`确认要${hotText}商品"${row.name}"吗？`);
    const response = await updateProductHotFlag(row.id, newHotFlag);
    if (response.code !== 200) throw new Error(response.msg || `${hotText}失败`);
    proxy.$modal.msgSuccess(`${hotText}成功`);
    await getList();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') console.error(`${hotText}商品失败:`, error);
  } finally {
    const ids = new Set(hotUpdatingIds.value);
    ids.delete(row.id);
    hotUpdatingIds.value = ids;
  }
}

/** 加载当前用户的店铺信息 */
async function loadMyShop() {
  const userStore = useUserStore();
  // 管理员无需加载店铺信息
  if (userStore.userId === 1) {
    return;
  }
  try {
    const response = await getMyShop();
    if (response.code === 200 && response.data) {
      myShopId.value = response.data.id;
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error);
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  loadCategoryTree();
  await loadMyShop();  // 先加载店铺信息
  getList();           // 再加载商品列表
});
</script>

<style scoped>
/* 容器基础样式 */
.product-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding: 20px 24px 8px;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.search-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rotate-icon {
  transform: rotate(180deg);
  transition: transform 0.3s;
}

/* 高级筛选区域 */
.advanced-search-section {
  padding-top: 8px;
}

.advanced-search-section :deep(.el-divider__text) {
  font-size: 13px;
  color: #909399;
  background: #fff;
}

/* 筛选条件标签 */
.filter-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 0;
  margin-top: 8px;
  border-top: 1px dashed #e4e7ed;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-tags :deep(.el-tag) {
  font-size: 14px;
  padding: 8px 16px;
}

/* 工具栏 */
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 8px;
}

.table-card :deep(.el-table th) {
  padding: 14px 0;
}

.table-card :deep(.el-table td) {
  padding: 12px 0;
}

.table-card :deep(.el-table .cell) {
  padding: 0 16px;
  line-height: 1.6;
}

/* 分页包裹 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 4px;
}

/* 商品名称 */
.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-subtitle {
  color: #999;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 响应式调整 */
@media screen and (max-width: 1400px) {
  .table-card :deep(.el-table .cell) {
    padding: 0 12px;
  }
}
</style>
