import axios from 'axios'

// 创建axios默认请求
const service = axios.create({
  baseURL: process.env.BASE_URL,
  timeout: 40000 // request timeout,
})

export default {
  get (url, param) {
    return new Promise((resolve, reject) => {
      service({
        method: 'get',
        url,
        params: param
      }).then(res => {
        resolve(res)
      }).catch(error => {
        reject(error)
      })
    })
  },
  post (url, param) {
    return new Promise((resolve, reject) => {
      service.post(
        url,
        param
      ).then(res => {
        resolve(res)
      }).catch(error => {
        reject(error)
      })
    })
  },
  put (url, param) {
    return new Promise((resolve, reject) => {
      service.put(
        url,
        param
      ).then(res => {
        resolve(res)
      }).catch(error => {
        reject(error)
      })
    })
  },
  delete (url) {
    return new Promise((resolve, reject) => {
      service.delete(
        url
      ).then(res => {
        resolve(res)
      }).catch(error => {
        reject(error)
      })
    })
  }
}
