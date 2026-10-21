import request from '@/utils/request'

// 查询用户足迹列表
export function listFootprintByUser(userId, params = {}) {
  return request({
    url: `/system/footprint/user/${userId}`,
    method: 'get',
    params: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 20,
      targetType: params.targetType
    }
  })
}

// 删除足迹
export function deleteFootprint(id) {
  return request({
    url: `/system/footprint/${id}`,
    method: 'delete'
  })
}

// 清空用户足迹
export function clearFootprint(userId) {
  return request({
    url: `/system/footprint/clear/${userId}`,
    method: 'delete'
  })
}

// 记录用户足迹
export function recordFootprint({ targetType, targetId }) {
  return request({
    url: '/system/footprint/record',
    method: 'post',
    params: {  // 改为 params
      targetType,
      targetId
    }
  })
}
