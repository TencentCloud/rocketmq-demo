import axios, { AxiosError } from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

// Retry configuration
const MAX_RETRY_COUNT = 3
const RETRY_DELAY = 1000 // milliseconds
const RETRY_STATUS_CODES = [408, 429, 500, 502, 503, 504]

interface RetryConfig {
  retryCount?: number
  retryDelay?: number
}

// Extend AxiosRequestConfig with retry configuration
declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    _retryCount?: number
  }
}

const instance: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Initialize retry count
    if (config._retryCount === undefined) {
      config._retryCount = 0
    }
    return config
  },
  (error: AxiosError) => {
    MessagePlugin.error('Request configuration error')
    return Promise.reject(error)
  }
)

// Response interceptor with retry logic
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // Handle business logic errors
    if (data.success === false) {
      const message = data.message || 'Request failed'
      MessagePlugin.error(message)
      return Promise.reject(new Error(message))
    }

    return response
  },
  async (error: AxiosError) => {
    const config = error.config as InternalAxiosRequestConfig & RetryConfig

    // Determine if we should retry
    const shouldRetry = 
      config &&
      config._retryCount !== undefined &&
      config._retryCount < MAX_RETRY_COUNT &&
      (!error.response || RETRY_STATUS_CODES.includes(error.response.status))

    if (shouldRetry) {
      config._retryCount = (config._retryCount || 0) + 1

      // Calculate exponential backoff delay
      const delay = RETRY_DELAY * Math.pow(2, config._retryCount - 1)

      console.warn(
        `Request failed, retrying (${config._retryCount}/${MAX_RETRY_COUNT}) after ${delay}ms...`,
        {
          url: config.url,
          method: config.method,
          error: error.message
        }
      )

      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, delay))

      // Retry the request
      return instance(config)
    }

    // Handle different error scenarios
    let errorMessage = 'Network error'
    let showMessage = true

    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          errorMessage = (data as { message?: string })?.message || 'Invalid request'
          break
        case 401:
          errorMessage = 'Unauthorized, please login again'
          // TODO: Redirect to login page
          break
        case 403:
          errorMessage = 'Access denied'
          break
        case 404:
          errorMessage = 'Resource not found'
          break
        case 408:
          errorMessage = 'Request timeout, please try again'
          break
        case 429:
          errorMessage = 'Too many requests, please try again later'
          break
        case 500:
          errorMessage = 'Internal server error'
          break
        case 502:
          errorMessage = 'Bad gateway'
          break
        case 503:
          errorMessage = 'Service unavailable'
          break
        case 504:
          errorMessage = 'Gateway timeout'
          break
        default:
          errorMessage = (data as { message?: string })?.message || `Error ${status}`
      }
    } else if (error.request) {
      // Request was made but no response received
      if (error.code === 'ECONNABORTED') {
        errorMessage = 'Request timeout, please check your network connection'
      } else if (error.code === 'ERR_NETWORK') {
        errorMessage = 'Network error, please check your connection'
      } else {
        errorMessage = 'No response from server, please try again later'
      }
    } else {
      // Error in request configuration
      errorMessage = error.message || 'Request failed'
    }

    // Show error message to user
    if (showMessage) {
      MessagePlugin.error(errorMessage)
    }

    console.error('Request error:', {
      url: config?.url,
      method: config?.method,
      message: errorMessage,
      error
    })

    return Promise.reject(error)
  }
)

// Export a wrapper function for making requests with custom retry config
export const request = <T = any>(
  config: InternalAxiosRequestConfig & RetryConfig
): Promise<AxiosResponse<T>> => {
  return instance(config)
}

export default instance
