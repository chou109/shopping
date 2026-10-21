import request from '@/utils/request'

// 查询用户足迹列表
export function listFootprintByUser(params = {}) {
  return request({
    url: '/system/footprint/my',
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
    url: `/system/footprint/my/${id}`,
    method: 'delete'
  })
}

// 清空用户足迹
export function clearFootprint() {
  return request({
    url: '/system/footprint/my/clear',
    method: 'delete'
  })
}

// 记录用户足迹
export function recordFootprint({ targetType, targetId }) {
  return request({
    url: '/system/footprint/record',
    method: 'post',
    silentAuthFailure: true,
    params: {  // 改为 params
      targetType,
      targetId
    }
  })
}
