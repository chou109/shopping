import request from '@/utils/request'

// 查询招标轮播图列表
export function listBanner(query) {
  return request({
    url: '/bid/banner/list',
    method: 'get',
    params: query
  })
}
