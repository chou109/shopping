import request from '@/utils/request'
export function listFeedback(query) {
    return request({
      url: '/message/feedback/list',
      method: 'get',
      params: query
    })
  }
  export function getFeedback(id) {
    return request({
      url: '/message/feedback/' + id,
      method: 'get'
    })
  }  


  export function addFeedback(data) {
    return request({
      url: '/message/feedback',
      method: 'post',
      data: data
    })
  }