import moment from 'moment'

/**
 * 时间工具函数
 */

/**
 * 格式化日期为 YYYY-MM-DD
 */
export function formatDate(date) {
  return moment(date).format('YYYY-MM-DD')
}

/**
 * 格式化时间为 MM-DD HH:mm
 */
export function formatTime(time) {
  return moment(time).format('MM-DD HH:mm')
}

/**
 * 格式化完整时间为 YYYY-MM-DD HH:mm:ss
 */
export function formatDateTime(dateTime) {
  return moment(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 组合日期和时间为完整的日期时间字符串
 */
export function combineDateTime(date, time) {
  return `${date} ${time}:00`
}

/**
 * 格式化时间范围显示
 */
export function formatTimeRange(startTime, endTime) {
  const start = moment(startTime).format('MM-DD HH:mm')
  const end = moment(endTime).format('HH:mm')
  return `${start} - ${end}`
}

/**
 * 检查时间是否有效
 */
export function isValidTime(date, startTime, endTime) {
  if (!date || !startTime || !endTime) {
    return false
  }
  
  const start = moment(`${date} ${startTime}`)
  const end = moment(`${date} ${endTime}`)
  const now = moment()
  
  // 检查开始时间不能是过去时间
  if (start.isBefore(now)) {
    return false
  }
  
  // 检查结束时间必须晚于开始时间
  if (end.isSameOrBefore(start)) {
    return false
  }
  
  return true
}

/**
 * 生成时间选项（8:00-22:00）
 */
export function generateTimeOptions() {
  const times = []
  for (let i = 8; i <= 22; i++) {
    const hour = i.toString().padStart(2, '0')
    times.push(`${hour}:00`)
  }
  return times
}