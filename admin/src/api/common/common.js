import request from '@/utils/request'


export function getQuerySelect(portName,method,params){
    return request({
        url: '/online/api/' + portName,
        method: method == null ? "GET" : method,
        params
    })
}


