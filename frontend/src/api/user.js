import request from '@/utils/request'

/**
 * 分页查询用户列表
 */
export function getUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

/**
 * 切换用户状态（启用/禁用）
 */
export function toggleUserStatus(userId) {
  return request({
    url: `/user/${userId}/toggle-status`,
    method: 'put'
  })
}