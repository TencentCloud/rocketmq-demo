import dayjs from 'dayjs'

export const formatTime = (
  time: string | number | Date,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string => {
  if (!time) return '-'
  return dayjs(time).format(format)
}

export const formatSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  if (!bytes) return '-'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

export const formatNumber = (num: number): string => {
  if (num === 0) return '0'
  if (!num) return '-'

  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

export const formatPercent = (value: number, total: number): string => {
  if (!total || total === 0) return '0%'
  return `${((value / total) * 100).toFixed(2)}%`
}

export const formatDuration = (ms: number): string => {
  if (!ms || ms < 0) return '-'

  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) return `${days}d ${hours % 24}h`
  if (hours > 0) return `${hours}h ${minutes % 60}m`
  if (minutes > 0) return `${minutes}m ${seconds % 60}s`
  return `${seconds}s`
}
