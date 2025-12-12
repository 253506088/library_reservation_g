import Vue from 'vue'
import VueRouter from 'vue-router'
import { isMobile } from '@/utils/device'
import { isLoggedIn, isAdmin, isStudent } from '@/utils/auth'

Vue.use(VueRouter)

// 移动端路由
const mobileRoutes = [
  {
    path: '/mobile',
    redirect: '/mobile/login'
  },
  {
    path: '/mobile/login',
    name: 'MobileLogin',
    component: () => import('@/views/mobile/Login.vue')
  },
  {
    path: '/mobile/register',
    name: 'MobileRegister',
    component: () => import('@/views/mobile/Register.vue')
  },
  {
    path: '/mobile/home',
    name: 'MobileHome',
    component: () => import('@/views/mobile/Home.vue'),
    meta: { requiresAuth: true, requiresStudent: true }
  },

  {
    path: '/mobile/my-reservations',
    name: 'MobileMyReservations',
    component: () => import('@/views/mobile/MyReservations.vue'),
    meta: { requiresAuth: true, requiresStudent: true }
  },
  {
    path: '/mobile/qrcode/:orderNo',
    name: 'MobileQRCode',
    component: () => import('@/views/mobile/QRCode.vue'),
    meta: { requiresAuth: true, requiresStudent: true }
  }
]

// PC端路由
const adminRoutes = [
  {
    path: '/admin',
    redirect: '/admin/login'
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'libraries',
        name: 'LibraryManagement',
        component: () => import('@/views/admin/LibraryManagement.vue')
      },
      {
        path: 'seats',
        name: 'SeatManagement',
        component: () => import('@/views/admin/SeatManagement.vue')
      },
      {
        path: 'reservations',
        name: 'ReservationManagement',
        component: () => import('@/views/admin/ReservationManagement.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue')
      },
      {
        path: 'scanner',
        name: 'QRScanner',
        component: () => import('@/views/admin/QRScanner.vue')
      }
    ]
  }
]

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/SimpleTest.vue')
  },
  {
    path: '/auto',
    redirect: () => {
      return isMobile() ? '/mobile' : '/admin'
    }
  },
  ...mobileRoutes,
  ...adminRoutes
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn()) {
      // 未登录，跳转到登录页
      if (isMobile()) {
        next('/mobile/login')
      } else {
        next('/admin/login')
      }
      return
    }
  }
  
  // 检查是否需要管理员权限
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (!isAdmin()) {
      next('/admin/login')
      return
    }
  }
  
  // 检查是否需要学生权限
  if (to.matched.some(record => record.meta.requiresStudent)) {
    if (!isStudent()) {
      next('/mobile/login')
      return
    }
  }
  
  next()
})

export default router