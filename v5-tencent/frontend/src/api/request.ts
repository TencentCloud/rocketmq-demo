import axios, { AxiosError } from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const instance: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    if (data.success === false) {
      MessagePlugin.error(data.message || 'Request failed')
      return Promise.reject(new Error(data.message || 'Request failed'))
    }

    return response
  },
  (error: AxiosError) => {
    let errorMessage = 'Network error'

    if (error.response) {
      const { status, data } = error.response
      errorMessage = (data as { message?: string })?.message || `Error ${status}`
    } else if (error.request) {
      errorMessage = 'No response from server'
    } else {
      errorMessage = error.message
    }

    MessagePlugin.error(errorMessage)
    return Promise.reject(error)
  }
)

export default instance
