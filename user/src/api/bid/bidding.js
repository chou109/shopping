import request from '@/utils/request'

// 查询招投标列表
export function listBidding(query) {
  return request({
    url: '/bid/bidding/front/list',
    method: 'get',
    params: query
  })
}

export function listBiddingByNoSignUp(query) {
  return request({
    url: '/bid/bidding/front/list/noSignUp',
    method: 'get',
    params: query
  })
}

export function listBiddingBySignUp(query) {
  return request({
    url: '/bid/bidding/front/list/signUp',
    method: 'get',
    params: query
  })
}

// 查询招投标详细
export function getBidding(id) {
  return request({
    url: '/bid/bidding/front/' + id,
    method: 'get'
  })
}

export function viewBidding(id) {
  return request({
    url: '/bid/bidding/front/view',
    method: 'put',
    params: {id}
  })
}

export function getNewestBidding() {
  return request({
    url: '/bid/bidding/newest',
    method: 'get'
  })
}

// 新增招投标
export function addBidding(data) {
  return request({
    url: '/bid/bidding',
    method: 'post',
    data: data
  })
}

// 修改招投标
export function updateBidding(data) {
  return request({
    url: '/bid/bidding',
    method: 'put',
    data: data
  })
}

// 删除招投标
export function delBidding(id) {
  return request({
    url: '/bid/bidding/' + id,
    method: 'delete'
  })
}

// 导出招投标
export function exportBidding(query) {
  return request({
    url: '/bid/bidding/export',
    method: 'get',
    params: query
  })
}
