import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/',
      component: () => import('../layouts/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('../views/user/UserList.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'categories',
          name: 'categories',
          component: () => import('../views/category/CategoryList.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: 'dishes',
          name: 'dishes',
          component: () => import('../views/dish/DishList.vue'),
          meta: { title: '菜品管理' }
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/order/OrderList.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'orders/:id',
          name: 'orderDetail',
          component: () => import('../views/order/OrderDetail.vue'),
          meta: { title: '订单详情' }
        },
        {
          path: 'banners',
          name: 'banners',
          component: () => import('../views/banner/BannerList.vue'),
          meta: { title: '轮播图管理' }
        },
        {
          path: 'feedbacks',
          name: 'feedbacks',
          component: () => import('../views/feedback/FeedbackList.vue'),
          meta: { title: '反馈管理' }
        }
      ]
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('adminToken')
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
