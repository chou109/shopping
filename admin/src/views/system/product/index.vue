<template>
  <div class="app-container">
    <!-- 基础查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px" class="search-form">
      <el-row :gutter="20">
        <!-- 第一行：基础查询条件 -->
        <el-col :span="6">
          <el-form-item label="商品名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入商品名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="商品状态" prop="status">
            <el-select
              v-model="queryParams.status"
              placeholder="全部状态"
              clearable
              style="width: 100%"
            >
              <el-option label="全部" :value="null" />
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="热门状态" prop="hotFlag">
            <el-select
              v-model="queryParams.hotFlag"
              placeholder="全部"
              clearable
              style="width: 100%"
            >
              <el-option label="全部" :value="null" />
              <el-option label="热门" :value="1" />
              <el-option label="非热门" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            <el-button type="text" @click="toggleAdvanced" style="margin-left: 10px;">
              {{ showAdvanced ? '收起' : '展开' }}
              <el-icon :style="{ transform: showAdvanced ? 'rotate(180deg)' : '', transition: 'transform 0.3s' }">
                <ArrowDown />
              </el-icon>
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 高级查询条件（折叠部分） -->
      <div v-if="showAdvanced" class="advanced-search" style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="商品ID" prop="id">
              <el-input
                v-model="queryParams.id"
                placeholder="请输入商品ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="品类ID" prop="categoryId">
              <el-input
                v-model="queryParams.categoryId"
                placeholder="请输入品类ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="活动ID" prop="activityId">
              <el-input
                v-model="queryParams.activityId"
                placeholder="请输入活动ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商品ID" prop="productId">
              <el-input
                v-model="queryParams.productId"
                placeholder="请输入商品编码"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="价格范围">
              <div style="display: flex; align-items: center;">
                <el-input-number
                  v-model="queryParams.minPrice"
                  placeholder="最低价"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="flex: 1"
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number
                  v-model="queryParams.maxPrice"
                  placeholder="最高价"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="flex: 1"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存范围">
              <div style="display: flex; align-items: center;">
                <el-input-number
                  v-model="queryParams.minStock"
                  placeholder="最小库存"
                  :min="0"
                  :controls="false"
                  style="flex: 1"
                />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number
                  v-model="queryParams.maxStock"
                  placeholder="最大库存"
                  :min="0"
                  :controls="false"
                  style="flex: 1"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="queryParams.createTime"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="6">
            <el-form-item label="创建人" prop="createUser">
              <el-input
                v-model="queryParams.createUser"
                placeholder="请输入创建人ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="更新人" prop="updateUser">
              <el-input
                v-model="queryParams.updateUser"
                placeholder="请输入更新人ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="删除状态" prop="deleteFlag">
              <el-select
                v-model="queryParams.deleteFlag"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option label="全部" :value="null" />
                <el-option label="正常" :value="0" />
                <el-option label="已删除" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:product:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:product:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:product:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:product:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="商品图片" align="center" prop="mainImage" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.mainImage" :width="50" :height="50" v-if="scope.row.mainImage"/>
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
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="热门" align="center" prop="hotFlag" width="80">
  <template #default="scope">
    <div v-if="scope.row.hotFlag === 1" style="color: #F56C6C; cursor: pointer;" @click="toggleHot(scope.row)">
      <el-icon size="18">
        <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
          <path d="M834.1 469.2A347.49 347.49 0 0 0 751.2 354l-29.1-26.7a8.09 8.09 0 0 0-13 3.3l-13 37.3c-8.1 23.4-23 47.3-44.1 70.8-1.4 1.5-3 1.9-4 1.9-2.8 0-4.7-2.6-4.1-5.3 1.1-4.6 1.6-9.7 1.6-14.5 0-19.1-7.2-37.5-22.7-51.8A83.81 83.81 0 0 0 600 271c-32.1 0-58.4 26.3-58.4 58.5 0 22 12.8 42.1 32.6 51.1 1.9 1 3.7 1.8 5.5 2.5 1.8 0.7 3.6 1.4 5.4 2 2.9 1 5.8 1.9 8.7 2.9 9.9 3.1 19.8 6.3 29.4 10.2 5.5 2.2 10.9 4.5 16.2 7 10.3 4.7 20.4 9.8 30.3 15.3 6.3 3.5 12.5 7.1 18.6 10.8 6.1 3.6 12.1 7.4 18 11.3 45.8 30.7 79.3 67.3 100.9 106.4 21.6 39.1 31.1 80.2 31.1 119.2 0 73.3-31.3 132.4-81.9 168.2-50.6 35.8-116.8 53.8-186.2 53.8-72.4 0-140.7-18.9-195.3-53.3-54.6-34.4-93-84-112.8-141.5-19.8-57.6-21-119.2-3.5-178.5 17.6-59.3 54.1-112.2 103.2-148.7 13.2-9.9 27.1-18.7 41.6-26.4 1.1-0.6 2.2-1.1 3.3-1.7 1.1-0.5 2.2-1 3.3-1.5 18.6-8.1 38.7-14.9 59.8-20.3 21.1-5.4 43.2-9.2 65.7-11.5 0.6-0.1 1.1-0.2 1.7-0.2 0.5 0 1 0.1 1.5 0.3l37.8 14.1c0.7 0.2 1.4 0.4 2.1 0.6 0.7 0.2 1.4 0.3 2.1 0.5 0.7 0.1 1.4 0.3 2.1 0.4 0.7 0.1 1.4 0.2 2 0.2 5.1 0.4 10.1 0.9 15 1.4 4.9 0.5 9.8 1.1 14.6 1.8 4.8 0.7 9.5 1.4 14.2 2.1 4.7 0.7 9.3 1.5 13.9 2.3 4.6 0.8 9.1 1.6 13.6 2.5 4.5 0.9 8.9 1.8 13.3 2.7 4.4 0.9 8.7 1.8 12.9 2.8 17.1 4.1 33.1 8.7 48.1 13.6 15 4.9 28.9 10.1 41.5 15.6 12.6 5.5 23.9 11.2 33.8 17.1 9.9 5.9 18.4 12 25.4 18.2 7 6.2 12.5 12.5 16.5 18.8s6.1 12.4 6.1 18.4c0 6-1.9 11.2-5.6 15.5-3.8 4.3-9.1 7.9-15.8 10.8z" />
        </svg>
      </el-icon>
      <span style="margin-left: 3px; font-size: 12px;">热门</span>
    </div>
    <el-button v-else link type="info" size="small" @click="toggleHot(scope.row)" v-hasPermi="['system:product:edit']">
      设热门
    </el-button>
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
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:product:remove']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 1 ? '下架' : '上架'" placement="top">
            <el-button link :type="scope.row.status === 1 ? 'warning' : 'success'" 
              :icon="scope.row.status === 1 ? 'Top' : 'Bottom'"
              @click="toggleStatus(scope.row)"
              v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.hotFlag === 1 ? '取消热门' : '设为热门'" placement="top">
  <el-button link :type="scope.row.hotFlag === 1 ? 'danger' : 'info'"
    @click="toggleHot(scope.row)"
    v-hasPermi="['system:product:edit']">
    <template #icon>
      <el-icon v-if="scope.row.hotFlag === 1">
        <svg t="1766222883576" class="icon" viewBox="0 0 1084 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8408" width="200" height="200"><path d="M665.720098 546.181518c-103.16924-223.888351-36.452732-295.781822-35.889736-296.681815 0.899993-0.899993 1.34999-2.811979 0.449997-4.611966-0.899993-1.34999-2.812979-1.799987-4.162969-1.34999-1.34999 0.449997-145.133931 60.303556-127.133064 193.511575-39.602708-35.551738-223.439355-217.588398-110.595186-431.802821 0.449997-1.34999 0.449997-2.811979-0.449997-4.161969-0.899993-0.899993-2.811979-1.34999-4.161969-0.899993-2.249983 0.899993-59.854559 25.76381-105.532223 87.529355-42.415688 58.054573-81.567399 159.423826-29.026786 316.033673 19.350858 73.692457-17.100874 133.659016-36.452732 158.97283 4.612966-17.550871 10.575922-47.027654 9.675929-81.117403-1.34999-49.728634-19.350858-118.358128-95.856294-163.135799-1.34999-0.449997-2.812979-0.449997-3.712973 0-1.34999 0.899993-1.799987 2.249983-1.34999 3.712973 0 1.34999 29.026786 132.196027-21.150844 192.611581C57.05558 567.333363 14.639892 658.126694 37.704722 742.394074c19.350858 70.542481 79.204417 124.433084 178.773684 159.872822 25.313814 7.875942 49.728634 13.838898 76.955433 18.901861 1.799987 0.449997 3.711973-0.449997 4.16197-2.249983 0.449997-1.799987-0.449997-3.712973-1.799987-4.612966-51.189623-22.163837-103.619237-68.629495-64.466525-162.235806 37.352725-81.117403 30.376776-122.520098 23.06383-140.521965 19.351858 11.025919 57.603576 39.152712 47.928647 88.881346-0.449997 1.34999 0.449997 2.811979 1.799987 3.712972 1.34999 0.899993 2.812979 0.449997 4.162969-0.449996s116.107145-95.856294 64.016529-212.414436c25.76381 14.850891 93.943308 67.392504 53.890603 186.649625-13.387901 51.64062 23.06383 88.431349 24.863817 89.893338 1.34999 1.34999 3.262976 1.34999 4.612966 0.449997 1.34999-0.899993 1.799987-2.812979 1.34999-4.162969-1.34999-3.261976-28.57679-76.954433 30.939772-117.457135-3.262976 18.900861-8.325939 65.479518 12.938905 91.243328 18.899861 23.06383 37.351725 60.303556 31.839765 97.206284-4.163969 25.76381-19.351858 47.47765-45.565664 64.466525-1.34999 0.899993-1.799987 2.812979-1.34999 4.612966 0.899993 1.799987 2.811979 2.362983 4.161969 1.799987 64.916522-21.71384 150.196894-64.466525 187.099622-146.033925 26.439805-59.404563 22.839832-131.296033-11.362916-213.764426z m0 0" p-id="8409" fill="#d81e06"></path></svg>
      </el-icon>
      <el-icon v-else>
<svg t="1766222802360" class="icon" viewBox="0 0 1084 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8255" width="200" height="200"><path d="M665.720098 546.181518c-103.16924-223.888351-36.452732-295.781822-35.889736-296.681815 0.899993-0.899993 1.34999-2.811979 0.449997-4.611966-0.899993-1.34999-2.812979-1.799987-4.162969-1.34999-1.34999 0.449997-145.133931 60.303556-127.133064 193.511575-39.602708-35.551738-223.439355-217.588398-110.595186-431.802821 0.449997-1.34999 0.449997-2.811979-0.449997-4.161969-0.899993-0.899993-2.811979-1.34999-4.161969-0.899993-2.249983 0.899993-59.854559 25.76381-105.532223 87.529355-42.415688 58.054573-81.567399 159.423826-29.026786 316.033673 19.350858 73.692457-17.100874 133.659016-36.452732 158.97283 4.612966-17.550871 10.575922-47.027654 9.675929-81.117403-1.34999-49.728634-19.350858-118.358128-95.856294-163.135799-1.34999-0.449997-2.812979-0.449997-3.712973 0-1.34999 0.899993-1.799987 2.249983-1.34999 3.712973 0 1.34999 29.026786 132.196027-21.150844 192.611581C57.05558 567.333363 14.639892 658.126694 37.704722 742.394074c19.350858 70.542481 79.204417 124.433084 178.773684 159.872822 25.313814 7.875942 49.728634 13.838898 76.955433 18.901861 1.799987 0.449997 3.711973-0.449997 4.16197-2.249983 0.449997-1.799987-0.449997-3.712973-1.799987-4.612966-51.189623-22.163837-103.619237-68.629495-64.466525-162.235806 37.352725-81.117403 30.376776-122.520098 23.06383-140.521965 19.351858 11.025919 57.603576 39.152712 47.928647 88.881346-0.449997 1.34999 0.449997 2.811979 1.799987 3.712972 1.34999 0.899993 2.812979 0.449997 4.162969-0.449996s116.107145-95.856294 64.016529-212.414436c25.76381 14.850891 93.943308 67.392504 53.890603 186.649625-13.387901 51.64062 23.06383 88.431349 24.863817 89.893338 1.34999 1.34999 3.262976 1.34999 4.612966 0.449997 1.34999-0.899993 1.799987-2.812979 1.34999-4.162969-1.34999-3.261976-28.57679-76.954433 30.939772-117.457135-3.262976 18.900861-8.325939 65.479518 12.938905 91.243328 18.899861 23.06383 37.351725 60.303556 31.839765 97.206284-4.163969 25.76381-19.351858 47.47765-45.565664 64.466525-1.34999 0.899993-1.799987 2.812979-1.34999 4.612966 0.899993 1.799987 2.811979 2.362983 4.161969 1.799987 64.916522-21.71384 150.196894-64.466525 187.099622-146.033925 26.439805-59.404563 22.839832-131.296033-11.362916-213.764426z m0 0" p-id="8256"></path></svg>
      </el-icon>
    </template>
  </el-button>
</el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改商品对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
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
            <el-form-item label="品类ID" prop="categoryId">
              <el-input v-model="form.categoryId" placeholder="请输入品类ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动ID" prop="activityId">
              <el-input v-model="form.activityId" placeholder="请输入活动ID" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品单价" prop="price">
              <el-input-number 
                v-model="form.price" 
                placeholder="请输入商品单价" 
                :min="0" 
                :precision="2" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数" prop="stock">
              <el-input-number 
                v-model="form.stock" 
                placeholder="请输入库存数" 
                :min="0" 
                style="width: 100%"
              />
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
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Product">
import { listProduct, getProduct, delProduct, addProduct, updateProduct, updateProductStatus, updateProductHotFlag } from "@/api/system/product";
import { ArrowDown } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

const productList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false); // 控制高级筛选显示
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
    // 基础查询条件
    name: null,
    status: null,
    hotFlag: null,
    // 高级查询条件
    id: null,
    productId: null,
    categoryId: null,
    activityId: null,
    createUser: null,
    updateUser: null,
    deleteFlag: null,
    // 范围查询
    minPrice: null,
    maxPrice: null,
    minStock: null,
    maxStock: null,
    // 时间范围
    createTime: null,
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

/** 查询商品列表 */
function getList() {
  loading.value = true;
  
  // 构建查询参数
  const params = { ...queryParams.value };
  
  // 处理时间范围
  if (params.createTime && params.createTime.length === 2) {
    params.beginCreateTime = params.createTime[0];
    params.endCreateTime = params.createTime[1];
    delete params.createTime;
  }
  
  // 移除空值参数
  Object.keys(params).forEach(key => {
    if (params[key] === '' || params[key] === null || params[key] === undefined) {
      delete params[key];
    }
  });
  
  listProduct(params).then(response => {
    productList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

// 切换高级筛选显示
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value;
};

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
    deleteFlag: 0,
    productId: null,
    categoryId: null,
    name: null,
    subTitle: null,
    mainImage: null,
    subImages: null,
    activityId: null,
    status: 1, // 默认上架
    price: null,
    stock: null,
    hotFlag: 0 // 默认非热门
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
    id: null,
    productId: null,
    categoryId: null,
    activityId: null,
    createUser: null,
    updateUser: null,
    deleteFlag: null,
    minPrice: null,
    maxPrice: null,
    minStock: null,
    maxStock: null,
    createTime: null,
  };
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
  title.value = "添加商品";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value[0];
  getProduct(_id).then(response => {
    form.value = response.data;
    // 确保必要字段有默认值
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
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProduct(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProduct(form.value).then(response => {
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
  const _ids = row.id ? [row.id] : ids.value;
  proxy.$modal.confirm('是否确认删除商品编号为"' + _ids.join(',') + '"的数据项？').then(function() {
    return delProduct(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  const params = { ...queryParams.value };
  // 处理时间范围
  if (params.createTime && params.createTime.length === 2) {
    params.beginCreateTime = params.createTime[0];
    params.endCreateTime = params.createTime[1];
    delete params.createTime;
  }
  
  proxy.download('system/product/export', params, `product_${new Date().getTime()}.xlsx`);
}

/** 切换商品状态 */
function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? '上架' : '下架';
  
  proxy.$modal.confirm(`确认要${statusText}商品"${row.name}"吗？`).then(() => {
    return updateProductStatus(row.id, newStatus);
  }).then(response => {
    proxy.$modal.msgSuccess(`${statusText}成功`);
    getList();
  }).catch(() => {});
}

/** 切换热门状态 */
function toggleHot(row) {
  const newHotFlag = row.hotFlag === 1 ? 0 : 1;
  const hotText = newHotFlag === 1 ? '设为热门' : '取消热门';
  
  proxy.$modal.confirm(`确认要${hotText}商品"${row.name}"吗？`).then(() => {
    return updateProductHotFlag(row.id, newHotFlag);
  }).then(response => {
    proxy.$modal.msgSuccess(`${hotText}成功`);
    getList();
  }).catch(() => {});
}

// 组件挂载时加载数据
onMounted(() => {
  getList();
});
</script>

<style scoped>
.search-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.advanced-search {
  border-top: 1px solid #e8e8e8;
}

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

.app-container {
  padding: 20px;
}

.mb8 {
  margin-bottom: 20px;
}

:deep(.el-table .cell) {
  line-height: 1.4;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}
</style>