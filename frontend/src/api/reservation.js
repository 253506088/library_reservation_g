import request from '@/utils/request'

/**
 * 预约相关API
 */

// 创建预约
export function createReservation(data) {
  return request({
    url: '/reservation',
    method: 'post',
    data
  })
}

// 取消预约
export function cancelReservation(id) {
  return request({
    url: `/reservation/${id}/cancel`,
    method: 'put'
  })
}

// 扫码签到
export function checkIn(orderNo) {
  return request({
    url: '/reservation/checkin',
    method: 'post',
    params: { orderNo }
  })
}

// 分页查询预约记录
export function getReservationPage(params) {
  return request({
    url: '/reservation/page',
    method: 'get',
    params
  })
}

// 根据流水号查询预约记录
export function getReservationByOrderNo(orderNo) {
  return request({
    url: `/reservation/order/${orderNo}`,
    method: 'get'
  })
}