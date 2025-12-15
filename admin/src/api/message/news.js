import request from '@/utils/request'

// 查询新闻公告列表
export function listNews(query) {
  return request({
    url: '/message/news/list',
    method: 'get',
    params: query
  })
}

// 查询新闻公告详细
export function getNews(id) {
  return request({
    url: '/message/news/' + id,
    method: 'get'
  })
}

// 新增新闻公告
export function addNews(data) {
  return request({
    url: '/message/news',
    method: 'post',
    data: data
  })
}

// 修改新闻公告
export function updateNews(data) {
  return request({
    url: '/message/news',
    method: 'put',
    data: data
  })
}

// 删除新闻公告
export function delNews(id) {
  return request({
    url: '/message/news/' + id,
    method: 'delete'
  })
}
