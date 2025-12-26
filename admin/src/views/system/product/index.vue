<template>
  <div class="app-container">
    <!-- 基础查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px"
      class="search-form">
      <el-row :gutter="20">
        <!-- 第一行：基础查询条件 -->
        <el-col :span="6">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="热门状态" prop="hotFlag">
            <el-select v-model="queryParams.hotFlag" placeholder="全部" clearable style="width: 200px">
              <el-option label="全部" :value="null" />
              <el-option label="热门" :value="1" />
              <el-option label="非热门" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <!-- 店铺名称搜索框 -->
        <el-col :span="6">
          <el-form-item label="店铺名称" prop="shopId">
            <div class="store-select-wrapper" @click.stop="toggleStoreCascader">
              <el-select v-model="queryParams.shopId" placeholder="点击选择店铺" clearable filterable
                style="width: 200px" @click.stop>
                <el-option label="全部" :value="null" />
                <el-option v-for="store in storeTreeOptions" :key="store.id" :label="store.shopName"
                  :value="store.id" />
              </el-select>

              <!-- 级联选择器面板 -->
              <div v-if="showStoreCascader" class="cascader-panel store-cascader" @click.stop>
                <div class="cascader-container">
                  <!-- 左侧店铺列表 -->
                  <div class="cascader-menu left-menu">
                    <div v-for="store in storeTree" :key="store.id" class="cascader-menu-item"
                      :class="{ active: queryParams.shopId === store.id }" 
                      @click.stop="handleStoreClick(store)">
                      <span>{{ store.shopName }}</span>
                      <el-tag v-if="store.status === 1" size="small" type="success" style="margin-left: 8px;">正常</el-tag>
                      <el-tag v-else-if="store.status === 2" size="small" type="warning" style="margin-left: 8px;">关闭</el-tag>
                      <el-tag v-else size="small" type="info" style="margin-left: 8px;">审核中</el-tag>
                    </div>
                  </div>

                  <!-- 右侧店铺详情 -->
                  <div v-if="selectedStore" class="cascader-menu right-menu">
                    <div class="store-detail-panel">
                      <div class="store-detail-header">
                        <h4>{{ selectedStore.shopName }}</h4>
                      </div>
                      <div class="store-detail-info">
                        <p><span class="detail-label">联系电话:</span> {{ selectedStore.contactPhone || '未填写' }}</p>
                        <p><span class="detail-label">店铺地址:</span> {{ selectedStore.contactAddress || '未填写' }}</p>
                        <p><span class="detail-label">店铺评分:</span> 
                          <el-rate
                            v-model="selectedStore.shopScore"
                            disabled
                            show-score
                            text-color="#ff9900"
                            score-template="{value}"
                            style="display: inline-block; margin-left: 5px;"
                          />
                        </p>
                        <p><span class="detail-label">商品数量:</span> {{ selectedStore.productCount || 0 }}</p>
                        <p><span class="detail-label">销售数量:</span> {{ selectedStore.salesCount || 0 }}</p>
                      </div>
                      <div class="store-detail-footer">
                        <el-button type="primary" size="small" @click.stop="confirmStoreSelection">选择此店铺</el-button>
                      </div>
                    </div>
                  </div>

                  <div v-if="storeTree.length === 0" class="cascader-menu right-menu empty-tips">
                    暂无店铺数据
                  </div>
                </div>
              </div>
            </div>
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
      <div v-if="showAdvanced" class="advanced-search"
        style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
        <el-row :gutter="20">
          <!-- 商品ID -->
          <el-col :span="6">
            <el-form-item label="商品ID" prop="productId">
              <el-input v-model="queryParams.productId" placeholder="请输入商品编码" clearable />
            </el-form-item>
          </el-col>
          
          <!-- 品类 -->
          <el-col :span="6">
            <el-form-item label="品类" prop="categoryId">
              <div class="cascader-select-wrapper" @click.stop="toggleCascader">
                <el-select v-model="queryParams.categoryId" placeholder="点击选择品类" clearable filterable
                  style="width: 200px" @click.stop>
                  <el-option label="全部" :value="null" />
                  <el-option v-for="category in categoryTreeOptions" :key="category.id" :label="category.name"
                    :value="category.id" />
                </el-select>

                <!-- 级联选择器面板 -->
                <div v-if="showCascader" class="cascader-panel" @click.stop>
                  <div class="cascader-container">
                    <!-- 左侧父级菜单 -->
                    <div class="cascader-menu left-menu">
                      <div v-for="parent in categoryTree" :key="parent.id" class="cascader-menu-item"
                        :class="{ active: activeParentId === parent.id }" @mouseenter="handleParentHover(parent)"
                        @click.stop="handleParentClick(parent)">
                        <span>{{ parent.name }}</span>
                        <el-icon v-if="parent.children && parent.children.length > 0" style="margin-left: 8px;">
                          <ArrowRight />
                        </el-icon>
                      </div>
                    </div>

                    <!-- 右侧子级菜单 -->
                    <div v-if="activeParentId && activeChildren.length > 0" class="cascader-menu right-menu">
                      <div v-for="child in activeChildren" :key="child.id" class="cascader-menu-item"
                        :class="{ active: queryParams.categoryId === child.id }" @click.stop="handleChildClick(child)">
                        <span>{{ child.name }}</span>
                      </div>
                    </div>

                    <div v-if="activeParentId && activeChildren.length === 0"
                      class="cascader-menu right-menu empty-tips">
                      该分类下没有子分类
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          
          <!-- 商品状态 - 放在品类后面 -->
          <el-col :span="6">
            <el-form-item label="商品状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 200px">
                <el-option label="全部" :value="null" />
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <!-- 这里留空一个位置，保持布局平衡 -->
          <el-col :span="6">
            <!-- 可以为空，或者放其他查询条件 -->
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="8">
            <el-form-item label="价格范围">
              <div style="display: flex; align-items: center;">
                <el-input-number v-model="queryParams.minPrice" placeholder="最低价" :min="0" :precision="2"
                  :controls="false" style="flex: 1" />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number v-model="queryParams.maxPrice" placeholder="最高价" :min="0" :precision="2"
                  :controls="false" style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存范围">
              <div style="display: flex; align-items: center;">
                <el-input-number v-model="queryParams.minStock" placeholder="最小库存" :min="0" :controls="false"
                  style="flex: 1" />
                <span style="margin: 0 10px; color: #ccc">-</span>
                <el-input-number v-model="queryParams.maxStock" placeholder="最大库存" :min="0" :controls="false"
                  style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间">
              <el-date-picker v-model="queryParams.createTime" type="daterange" range-separator="至"
                start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="6">
            <el-form-item label="删除状态" prop="deleteFlag">
              <el-select v-model="queryParams.deleteFlag" placeholder="全部" clearable style="width: 200px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['system:product:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['system:product:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['system:product:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['system:product:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
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
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="热门" align="center" prop="hotFlag" width="80">
        <template #default="scope">
          <div v-if="scope.row.hotFlag === 1" style="color: #F56C6C; cursor: pointer;" @click="toggleHot(scope.row)">
            <el-icon>
              <el-icon v-if="scope.row.hotFlag === 1" size="13">
                <svg t="1766388502966" class="icon" viewBox="0 0 1024 1024" version="1.1"
                  xmlns="http://www.w3.org/2000/svg" p-id="10265" width="200" height="200">
                  <path
                    d="M843.292444 605.184c-112.782222-244.224-39.964444-322.531556-39.253333-323.441778a3.868444 3.868444 0 0 0 0.426667-4.864 3.868444 3.868444 0 0 0-4.664889-1.336889c-1.649778 0.625778-158.577778 65.962667-138.780445 210.972445-43.093333-38.741333-244.224-237.169778-121.173333-470.727111a3.982222 3.982222 0 0 0-0.625778-4.465778 3.527111 3.527111 0 0 0-4.238222-0.938667c-2.702222 1.024-65.450667 27.960889-115.370667 95.687111-46.08 62.464-89.059556 172.743111-31.288888 343.552 21.020444 80.384-18.631111 145.521778-39.879112 173.169778 5.290667-19.057778 11.804444-51.370667 10.666667-88.462222-1.649778-54.272-21.219556-129.251556-104.931555-177.720889a3.868444 3.868444 0 0 0-4.152889 0.199111 3.640889 3.640889 0 0 0-1.450667 4.039111c0.199111 1.450667 31.601778 144.184889-23.296 210.147556-47.331556 56.974222-93.838222 156.188444-68.664889 248.064 21.020444 76.856889 86.784 135.480889 195.555556 174.193778 27.847111 8.590222 54.584889 15.132444 83.996444 20.622222a3.640889 3.640889 0 0 0 4.352-2.588445c0.512-1.962667-0.312889-4.039111-2.161778-4.750222-55.836444-24.234667-113.408-74.581333-70.627555-177.009778 40.704-88.234667 33.450667-133.717333 25.258666-153.287111 21.020444 11.804444 62.862222 42.666667 52.394667 96.739556a3.925333 3.925333 0 0 0 1.763556 4.039111 3.754667 3.754667 0 0 0 4.352-0.312889c1.336889-1.024 127.089778-104.504889 69.916444-231.281778 28.16 16.270222 102.855111 73.130667 58.823111 203.406222-14.705778 56.149333 25.258667 96.426667 26.936889 98.076445 1.336889 1.251556 3.328 1.564444 4.864 0.426667 1.450667-0.938667 2.076444-2.901333 1.450667-4.551112-1.336889-3.527111-31.061333-83.797333 33.564444-128.113777-3.413333 20.622222-8.903111 71.566222 14.279111 99.328 20.821333 25.059556 40.817778 66.076444 34.702223 106.069333-4.238222 28.16-20.935111 51.882667-49.720889 70.542222a3.783111 3.783111 0 0 0-1.336889 4.977778c0.711111 1.763556 2.901333 2.588444 4.750222 1.962667 71.054222-22.983111 164.152889-69.603556 204.657778-158.577778 30.122667-65.564444 26.282667-144.184889-11.093334-233.784889z"
                    fill="#d81e06" p-id="10266"></path>
                </svg>
              </el-icon>
            </el-icon>
          </div>
          <el-icon v-else size="13">
            <svg t="1766388502966" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
              p-id="10265" width="200" height="200">
              <path
                d="M843.292444 605.184c-112.782222-244.224-39.964444-322.531556-39.253333-323.441778a3.868444 3.868444 0 0 0 0.426667-4.864 3.868444 3.868444 0 0 0-4.664889-1.336889c-1.649778 0.625778-158.577778 65.962667-138.780445 210.972445-43.093333-38.741333-244.224-237.169778-121.173333-470.727111a3.982222 3.982222 0 0 0-0.625778-4.465778 3.527111 3.527111 0 0 0-4.238222-0.938667c-2.702222 1.024-65.450667 27.960889 -115.370667 95.687111-46.08 62.464-89.059556 172.743111-31.288888 343.552 21.020444 80.384-18.631111 145.521778-39.879112 173.169778 5.290667-19.057778 11.804444-51.370667 10.666667-88.462222-1.649778-54.272-21.219556-129.251556-104.931555-177.720889a3.868444 3.868444 0 0 0-4.152889 0.199111 3.640889 3.640889 0 0 0-1.450667 4.039111c0.199111 1.450667 31.601778 144.184889-23.296 210.147556-47.331556 56.974222-93.838222 156.188444-68.664889 248.064 21.020444 76.856889 86.784 135.480889 195.555556 174.193778 27.847111 8.590222 54.584889 15.132444 83.996444 20.622222a3.640889 3.640889 0 0 0 4.352-2.588445c0.512-1.962667-0.312889-4.039111-2.161778-4.750222-55.836444-24.234667-113.408-74.581333-70.627555-177.009778 40.704-88.234667 33.450667-133.717333 25.258666-153.287111 21.020444 11.804444 62.862222 42.666667 52.394667 96.739556a3.925333 3.925333 0 0 0 1.763556 4.039111 3.754667 3.754667 0 0 0 4.352-0.312889c1.336889-1.024 127.089778-104.504889 69.916444-231.281778 28.16 16.270222 102.855111 73.130667 58.823111 203.406222-14.705778 56.149333 25.258667 96.426667 26.936889 98.076445 1.336889 1.251556 3.328 1.564444 4.864 0.426667 1.450667-0.938667 2.076444-2.901333 1.450667-4.551112-1.336889-3.527111-31.061333-83.797333 33.564444-128.113777-3.413333 20.622222-8.903111 71.566222 14.279111 99.328 20.821333 25.059556 40.817778 66.076444 34.702223 106.069333-4.238222 28.16-20.935111 51.882667-49.720889 70.542222a3.783111 3.783111 0 0 0-1.336889 4.977778c0.711111 1.763556 2.901333 2.588444 4.750222 1.962667 71.054222-22.983111 164.152889-69.603556 204.657778-158.577778 30.122667-65.564444 26.282667-144.184889-11.093334-233.784889z"
                fill="#8a8a8a" p-id="10266"></path>
            </svg> </el-icon>
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
              v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
              v-hasPermi="['system:product:remove']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === 1 ? '下架' : '上架'" placement="top">
            <el-button link :type="scope.row.status === 1 ? 'warning' : 'success'"
              :icon="scope.row.status === 1 ? 'Top' : 'Bottom'" @click="toggleStatus(scope.row)"
              v-hasPermi="['system:product:edit']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.hotFlag === 1 ? '取消热门' : '设为热门'" placement="top">
            <el-button link :type="scope.row.hotFlag === 1 ? 'danger' : 'info'" @click="toggleHot(scope.row)"
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

    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

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
            <el-form-item label="品类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="点击选择品类" clearable filterable
                  style="width: 200px" @click.stop>
                  <el-option label="全部" :value="null" />
                  <el-option v-for="category in categoryTreeOptions" :key="category.id" :label="category.name"
                    :value="category.id" />
                </el-select>
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
const LOCAL_CATEGORY_MAP = Object.freeze({
  7: '手机通讯',
  8: '手机配件',
  15: '电脑整机',
  18: '家具电器',
  23: '特色女装',
  24: '精品男装',
  25: '品质男鞋',
  26: '潮流女鞋',
  27: '功能箱包',
  31: '黄金首饰',
  32: '钻石彩宝',
  39: '面部护理'
});

// 转换为数组格式，用于下拉框选项
const LOCAL_CATEGORY_OPTIONS = Object.entries(LOCAL_CATEGORY_MAP).map(([id, name]) => ({
  id: Number(id),
  name: name
}));
import { listProduct, getProduct, delProduct, addProduct, updateProduct, updateProductStatus, updateProductHotFlag } from "@/api/system/product";
import { listCategoryFront } from "@/api/system/category";
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue';
import { listStore } from "@/api/system/store";

const { proxy } = getCurrentInstance();
const filteredCategoryOptions = ref([...LOCAL_CATEGORY_OPTIONS]);
const categoryLoading = ref(false);

const productList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const categoryTree = ref([]);
const showCascader = ref(false);
const activeParentId = ref(null);
const activeChildren = ref([]);
const categoryTreeOptions = ref([]);

// 新增店铺相关的响应式变量
const storeTree = ref([]);
const storeTreeOptions = ref([]);
const showStoreCascader = ref(false);
const selectedStore = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    // 基础查询条件
    name: null,
    status: null,
    hotFlag: null,
    shopId: null, // 修改为shopId
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

/** 将树形数据扁平化，用于下拉框显示 */
function flattenCategoryTree(treeData, level = 0) {
  const result = [];

  const flatten = (categories, currentLevel) => {
    categories.forEach(category => {
      const indent = '　'.repeat(currentLevel);
      result.push({
        id: category.id,
        label: `${indent}${category.name}`,
        name: category.name,
        level: currentLevel,
        original: category
      });

      if (category.children && category.children.length > 0) {
        flatten(category.children, currentLevel + 1);
      }
    });
  };

  flatten(treeData, level);
  return result;
}

/** 从后端加载分类树 */
async function loadCategoryTree() {
  try {
    const response = await listCategoryFront();
    console.log('Category tree response:', response);

    if (response && response.data) {
      const categories = response.data;

      if (Array.isArray(categories)) {
        const firstItem = categories[0];
        if (firstItem && firstItem.children !== undefined) {
          categoryTree.value = categories;
        } else {
          categoryTree.value = buildCategoryTree(categories);
        }
        generateCategoryOptions();
      }
    }
  } catch (error) {
    console.error('加载分类树失败:', error);
    categoryTree.value = [];
    categoryTreeOptions.value = [];
  }
}

/** 从后端加载店铺列表 */
async function loadStoreList() {
  try {
    const response = await listStore({
      pageNum: 1,
      pageSize: 1000,
      deleteFlag: 0
    });
    
    console.log('Store list response:', response);

    if (response && response.code === 200) {
      const stores = response.rows || response.data || [];
      storeTree.value = stores;
      generateStoreOptions();
    }
  } catch (error) {
    console.error('加载店铺列表失败:', error);
    storeTree.value = [];
    storeTreeOptions.value = [];
  }
}

/** 生成店铺选项 */
function generateStoreOptions() {
  storeTreeOptions.value = storeTree.value.map(store => ({
    id: store.id,
    shopName: store.shopName,
    status: store.status,
    contactAddress: store.contactAddress
  }));
}

/** 切换店铺级联面板显示 */
function toggleStoreCascader() {
  showStoreCascader.value = !showStoreCascader.value;
  
  if (showStoreCascader.value && queryParams.value.shopId) {
    const store = storeTree.value.find(s => s.id === queryParams.value.shopId);
    if (store) {
      selectedStore.value = store;
    }
  }
}

/** 处理店铺点击 */
function handleStoreClick(store) {
  selectedStore.value = store;
  queryParams.value.shopId = store.id;
}

/** 确认店铺选择 */
function confirmStoreSelection() {
  if (selectedStore.value) {
    queryParams.value.shopId = selectedStore.value.id;
    showStoreCascader.value = false;
    handleQuery();
  }
}

/** 点击页面其他区域关闭店铺级联面板 */
function handleStoreClickOutside(event) {
  const storeCascaderEl = document.querySelector('.store-select-wrapper');
  if (storeCascaderEl && !storeCascaderEl.contains(event.target)) {
    showStoreCascader.value = false;
  }
}

/** 点击页面其他区域关闭品类级联面板 */
function handleClickOutside(event) {
  const cascaderEl = document.querySelector('.cascader-select-wrapper');
  if (cascaderEl && !cascaderEl.contains(event.target)) {
    showCascader.value = false;
  }
}

/** 将平铺数据转换为树形结构 */
function buildCategoryTree(flatCategories) {
  const categoryMap = {};
  const tree = [];

  flatCategories.forEach(category => {
    categoryMap[category.id] = {
      ...category,
      children: []
    };
  });

  flatCategories.forEach(category => {
    const node = categoryMap[category.id];

    if (category.parentId === null || category.parentId === 0 || !categoryMap[category.parentId]) {
      tree.push(node);
    } else {
      const parent = categoryMap[category.parentId];
      if (parent) {
        parent.children.push(node);
      } else {
        tree.push(node);
      }
    }
  });

  return tree;
}

function generateCategoryOptions() {
  const result = [];

  const flatten = (categories, level = 0) => {
    categories.forEach(category => {
      const hasChildren = category.children && category.children.length > 0;

      if (!hasChildren) {
        const parentNames = getParentPath(categoryTree.value, category.id);
        const displayName = parentNames.length > 0
          ? `${parentNames.join(' / ')} / ${category.name}`
          : category.name;

        result.push({
          id: category.id,
          name: displayName,
          originalName: category.name,
          level: level
        });
      }

      if (hasChildren) {
        flatten(category.children, level + 1);
      }
    });
  };

  flatten(categoryTree.value);
  categoryTreeOptions.value = result;
}

/** 处理父级菜单悬停 */
function handleParentHover(parent) {
  activeParentId.value = parent.id;
  activeChildren.value = parent.children || [];
}

/** 切换级联面板显示 */
function toggleCascader() {
  showCascader.value = !showCascader.value;
  if (showCascader.value && queryParams.value.categoryId) {
    findParentForSelected();
  }
}

/** 获取分类的父级路径 */
function getParentPath(tree, categoryId, path = []) {
  for (const node of tree) {
    if (node.id === categoryId) {
      return path;
    }

    if (node.children && node.children.length > 0) {
      const childPath = getParentPath(node.children, categoryId, [...path, node.name]);
      if (childPath !== null) {
        return childPath;
      }
    }
  }
  return null;
}

/** 查找已选分类的父级 */
function findParentForSelected() {
  const selectedId = queryParams.value.categoryId;
  if (!selectedId) return;

  const findParent = (categories) => {
    for (const category of categories) {
      if (category.children) {
        const found = category.children.find(child => child.id === selectedId);
        if (found) {
          activeParentId.value = category.id;
          activeChildren.value = category.children;
          return true;
        }

        if (findParent(category.children)) {
          return true;
        }
      }
    }
    return false;
  };

  findParent(categoryTree.value);
}

/** 处理父级菜单点击 */
function handleParentClick(parent) {
  activeParentId.value = parent.id;
  activeChildren.value = parent.children || [];
}

/** 处理子级菜单点击 */
function handleChildClick(child) {
  queryParams.value.categoryId = child.id;
}

/** 品类选择变化处理 */
function handleCategoryChange(value) {
  console.log('Selected category id:', value);
}

/** 过滤品类选项（用于下拉框搜索） */
function filterCategoryOptions(query) {
  if (!query) {
    return categoryTreeOptions.value;
  }

  const searchText = query.toLowerCase();
  const matchedCategories = categoryTreeOptions.value.filter(item =>
    item.name.toLowerCase().includes(searchText) ||
    item.id.toString().includes(searchText)
  );

  if (matchedCategories.length === 0) {
    return [];
  }

  return categoryTreeOptions.value.filter(item =>
    matchedCategories.some(match => match.id === item.id)
  );
}

/** 查询商品列表 */
function getList() {
  loading.value = true;

  const params = { ...queryParams.value };

  if (params.createTime && params.createTime.length === 2) {
    params.beginCreateTime = params.createTime[0];
    params.endCreateTime = params.createTime[1];
    delete params.createTime;
  }

  Object.keys(params).forEach(key => {
    if (params[key] === '' || params[key] === null || params[key] === undefined) {
      delete params[key];
    }
  });

  listProduct(params).then(response => {
    productList.value = response.rows;
    console.log('Fetched product list:', response.rows);
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
    status: 1,
    price: null,
    stock: null,
    hotFlag: 0
  };
  proxy.resetForm("productRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  console.log('Query params:', queryParams.value);
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
    shopId: null,
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
  proxy.$modal.confirm('是否确认删除商品编号为"' + _ids.join(',') + '"的数据项？').then(function () {
    return delProduct(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

/** 导出按钮操作 */
function handleExport() {
  const params = { ...queryParams.value };
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
  }).catch(() => { });
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
  }).catch(() => { });
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategoryTree();
  loadStoreList();
  getList();
  
  // 添加点击外部关闭监听
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('click', handleStoreClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('click', handleStoreClickOutside);
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

/* 级联选择器样式 */
.cascader-select-wrapper,
.store-select-wrapper {
  position: relative;
}

.cascader-panel {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 5px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 400px;
  max-height: 300px;
  overflow: hidden;
}

.store-cascader .cascader-container {
  min-width: 500px;
}

.cascader-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.cascader-menu {
  flex: 1;
  min-height: 200px;
  overflow-y: auto;
  border: none;
  padding: 0;
}

.left-menu {
  border-right: 1px solid #e4e7ed;
  min-width: 150px;
}

.right-menu {
  min-width: 150px;
}

.cascader-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.3s;
}

.cascader-menu-item:hover {
  background-color: #f5f7fa;
}

.cascader-menu-item.active {
  background-color: #ecf5ff;
  color: #409eff;
}

.empty-tips {
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 店铺详情面板样式 */
.store-detail-panel {
  padding: 15px;
  max-width: 300px;
}

.store-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.store-detail-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.store-detail-info {
  margin-bottom: 15px;
}

.store-detail-info p {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.4;
}

.detail-label {
  display: inline-block;
  width: 70px;
  color: #666;
  font-weight: 500;
}

.store-detail-footer {
  text-align: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

/* 店铺项样式 */
.cascader-menu-item .el-tag {
  margin-left: 8px;
  font-size: 10px;
  height: 18px;
  line-height: 16px;
}

/* 确保级联面板在所有元素之上 */
.cascader-panel {
  z-index: 9999;
}

/* 店铺下拉框优化 */
:deep(.el-select-dropdown__item) {
  padding: 0 20px;
  height: 36px;
  line-height: 36px;
}

:deep(.el-select .el-input__inner) {
  cursor: pointer;
}

:deep(.el-select .el-select__caret) {
  opacity: 0.5;
}

/* 点击展开按钮样式 */
.cascader-select-wrapper :deep(.el-input),
.store-select-wrapper :deep(.el-input) {
  cursor: pointer;
}
</style>