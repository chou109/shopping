import request from '@/utils/request'

// 查询社交媒体互动列表
export function listMediaInteraction(query) {
  return request({
    url: '/mediaAndPublic/mediaInteraction/list',
    method: 'get',
    params: query
  })
}

// 查询社交媒体互动详细
export function getMediaInteraction(id) {
  return request({
    url: '/mediaAndPublic/mediaInteraction/' + id,
    method: 'get'
  })
}

// 新增社交媒体互动
export function addMediaInteraction(data) {
  return request({
    url: '/mediaAndPublic/mediaInteraction',
    method: 'post',
    data: data
  })
}

// 修改社交媒体互动
export function updateMediaInteraction(data) {
  return request({
    url: '/mediaAndPublic/mediaInteraction',
    method: 'put',
    data: data
  })
}

// 删除社交媒体互动
export function delMediaInteraction(id) {
  return request({
    url: '/mediaAndPublic/mediaInteraction/' + id,
    method: 'delete'
  })
}
