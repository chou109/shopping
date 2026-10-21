import axios from "axios";
const request = axios.create({
    baseURL: 'http://localhost:8000/api',
    crossDomain: true,
    timeout: 20000,
    withCredentials: false
})
export default request