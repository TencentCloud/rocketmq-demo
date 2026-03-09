export const validators = {
  required: (message: string = 'This field is required') => (value: unknown) => {
    if (value === null || value === undefined || value === '') {
      return message
    }
    return true
  },

  minLength: (min: number, message?: string) => (value: string) => {
    if (!value || value.length < min) {
      return message || `Minimum length is ${min} characters`
    }
    return true
  },

  maxLength: (max: number, message?: string) => (value: string) => {
    if (value && value.length > max) {
      return message || `Maximum length is ${max} characters`
    }
    return true
  },

  email: (message: string = 'Invalid email format') => (value: string) => {
    if (!value) return true
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(value) ? true : message
  },

  url: (message: string = 'Invalid URL format') => (value: string) => {
    if (!value) return true
    try {
      new URL(value)
      return true
    } catch {
      return message
    }
  },

  pattern:
    (regex: RegExp, message: string = 'Invalid format') =>
    (value: string) => {
      if (!value) return true
      return regex.test(value) ? true : message
    },

  number: (message: string = 'Must be a number') => (value: unknown) => {
    if (value === null || value === undefined || value === '') return true
    return !isNaN(Number(value)) ? true : message
  },

  integer: (message: string = 'Must be an integer') => (value: unknown) => {
    if (value === null || value === undefined || value === '') return true
    return Number.isInteger(Number(value)) ? true : message
  },

  min: (min: number, message?: string) => (value: number) => {
    if (value === null || value === undefined) return true
    return value >= min ? true : message || `Minimum value is ${min}`
  },

  max: (max: number, message?: string) => (value: number) => {
    if (value === null || value === undefined) return true
    return value <= max ? true : message || `Maximum value is ${max}`
  },

  range:
    (min: number, max: number, message?: string) =>
    (value: number) => {
      if (value === null || value === undefined) return true
      return value >= min && value <= max
        ? true
        : message || `Value must be between ${min} and ${max}`
    }
}

export default validators
