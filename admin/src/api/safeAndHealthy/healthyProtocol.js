import request from '@/utils/request'

// 查询安全与健康协议列表
export function listHealthyProtocol(query) {
  return request({
    url: '/safeAndHealthy/healthyProtocol/list',
    method: 'get',
    params: query
  })
}

// 查询安全与健康协议详细
export function getHealthyProtocol(id) {
  return request({
    url: '/safeAndHealthy/healthyProtocol/' + id,
    method: 'get'
  })
}

// 新增安全与健康协议
export function addHealthyProtocol(data) {
  return request({
    url: '/safeAndHealthy/healthyProtocol',
    method: 'post',
    data: data
  })
}

// 修改安全与健康协议
export function updateHealthyProtocol(data) {
  return request({
    url: '/safeAndHealthy/healthyProtocol',
    method: 'put',
    data: data
  })
}

// 删除安全与健康协议
export function delHealthyProtocol(id) {
  return request({
    url: '/safeAndHealthy/healthyProtocol/' + id,
    method: 'delete'
  })
}
