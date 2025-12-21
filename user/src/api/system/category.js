import request from '@/utils/request'

// 查询类目列表
export function listCategory(query) {
  return request({
    url: '/system/category/list',
    method: 'get',
    params: query
  })
}

// 查询类目详细
export function getCategory(id) {
  return request({
    url: '/system/category/' + id,
    method: 'get'
  })
}

// 新增类目
export function addCategory(data) {
  return request({
    url: '/system/category',
    method: 'post',
    data: data
  })
}

// 修改类目
export function updateCategory(data) {
  return request({
    url: '/system/category',
    method: 'put',
    data: data
  })
}

// 删除类目
export function delCategory(id) {
  return request({
    url: '/system/category/' + id,
    method: 'delete'
  })
}

// 批量删除类目
export function delCategoryByIds(ids) {
  return request({
    url: '/system/category/' + ids,
    method: 'delete'
  })
}

// 获取分类树形结构
export function getCategoryTree() {
  return request({
    url: '/system/category/tree',
    method: 'get'
  })
}

// 获取父分类下拉树列表
export function getCategoryTreeselect() {
  return request({
    url: '/system/category/treeselect',
    method: 'get'
  })
}

// 导出类目列表
export function exportCategory(query) {
  return request({
    url: '/system/category/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 启用/禁用分类
export function changeCategoryStatus(data) {
  return request({
    url: '/system/category/changeStatus',
    method: 'put',
    data: data
  })
}

// 设置/取消热门分类
export function changeCategoryHotFlag(data) {
  return request({
    url: '/system/category/changeHotFlag',
    method: 'put',
    data: data
  })
}

// ================ 前台接口（无需权限） ================

// 获取启用状态的分类列表（前台接口）
export function getEnabledCategories() {
  return request({
    url: '/system/category/enabled/list',
    method: 'get'
  })
}

// 获取热门分类列表（前台接口）
export function getHotCategories() {
  return request({
    url: '/system/category/hot/list',
    method: 'get'
  })
}

// 获取分类树（前台接口）
export function getFrontCategoryTree() {
  return request({
    url: '/system/category/front/tree',
    method: 'get'
  })
}