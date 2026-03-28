import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import UserLayout from '../layout/UserLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '/home',
          name: 'Home',
          component: () => import('../views/Home.vue')
        },
        {
          path: 'sys/staff',
          name: 'Staff',
          component: () => import('../views/Staff.vue')
        },
        {
          path: 'sys/dept',
          name: 'Dept',
          component: () => import('../views/Dept.vue')
        }
      ]
    },
    {
      path: '/user',
      component: UserLayout,
      children: [
        {
          path: 'home',
          name: 'UserHome',
          component: () => import('../views/UserHome.vue')
        },
        {
          path: 'news/:id',
          name: 'UserNewsDetail',
          component: () => import('../views/UserNewsDetail.vue')
        },
        {
          path: 'collect',
          name: 'UserCollect',
          component: () => import('../views/UserCollect.vue')
        },
        {
          path: 'history',
          name: 'UserHistory',
          component: () => import('../views/UserHistory.vue')
        }
      ]
    }
  ]
})

// 路由守卫：检查登录状态 + 角色访问控制
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('auth_token')
  const role = localStorage.getItem('auth_role')
  const whiteList = ['/login', '/register']

  if (!whiteList.includes(to.path) && !token) {
    next('/login')
    return
  }

  if (token && whiteList.includes(to.path)) {
    next(role === 'admin' ? '/home' : '/user/home')
    return
  }

  if ((to.path === '/home' || to.path.startsWith('/sys')) && role !== 'admin') {
    next('/user/home')
    return
  }

  if (to.path.startsWith('/user') && role !== 'user') {
    next('/home')
    return
  }

  next()
})

export default router
