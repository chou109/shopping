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
  
export function getNewsList(params){
    return request({
        url: '/api/news/list',
        method: 'GET',
        params
    })
}

export function getNewsDetail(id){
    return request({
        url: '/api/news/' + id,
        method: 'GET'
    })
}

export function viewNews(data){
    return request({
        url: '/api/news/view',
        method: 'PUT',
        data
    })
}