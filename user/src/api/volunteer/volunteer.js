import request from '@/utils/request'

export function getVolunteerList(params){
    return request({
        url: '/api/volunteer/list',
        method: 'GET',
        params
    })
}
export function listVolunteer(query) {
    return request({
      url: '/message/volunteer/list',
      method: 'get',
      params: query
    })
  }
  export function getVolunteer(id) {
    return request({
      url: '/message/volunteer/' + id,
      method: 'get'
    })
  }  

export function getVolunteerDetail(id){
    return request({
        url: '/api/volunteer/' + id,
        method: 'GET'
    })
}

export function viewVolunteer(data){
    return request({
        url: '/api/volunteer/view',
        method: 'PUT',
        data
    })
}