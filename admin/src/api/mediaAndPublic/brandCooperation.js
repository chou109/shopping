import request from '@/utils/request'

// 查询品牌合作列表
export function listBrandCooperation(query) {
  return request({
    url: '/mediaAndPublic/brandCooperation/list',
    method: 'get',
    params: query
  })
}

// 查询品牌合作详细
export function getBrandCooperation(id) {
  return request({
    url: '/mediaAndPublic/brandCooperation/' + id,
    method: 'get'
  })
}

// 新增品牌合作
export function addBrandCooperation(data) {
  return request({
    url: '/mediaAndPublic/brandCooperation',
    method: 'post',
    data: data
  })
}

// 修改品牌合作
export function updateBrandCooperation(data) {
  return request({
    url: '/mediaAndPublic/brandCooperation',
    method: 'put',
    data: data
  })
}

// 删除品牌合作
export function delBrandCooperation(id) {
  return request({
    url: '/mediaAndPublic/brandCooperation/' + id,
    method: 'delete'
  })
}
