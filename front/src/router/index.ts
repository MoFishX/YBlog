import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUserStore as useAdminUserStore } from '@/stores/adminUser'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  },
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/article/:id',
      name: 'ArticleDetail',
      component: () => import('@/views/ArticleDetail.vue')
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('@/views/SearchView.vue'),
      props: (route) => ({ keyword: route.query.q || '' })
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/user/:id',
      name: 'UserCenter',
      component: () => import('@/views/UserCenter.vue')
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/LoginView.vue'),
      meta: { blank: true }
    },
    {
      path: '/admin',
      component: () => import('@/components/admin/layout/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, blank: true },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'articles',
          name: 'AdminArticleList',
          component: () => import('@/views/admin/ArticleList.vue')
        },
        {
          path: 'articles/editor',
          name: 'AdminArticleNew',
          component: () => import('@/views/admin/ArticleEditor.vue')
        },
        {
          path: 'articles/editor/:id',
          name: 'AdminArticleEdit',
          component: () => import('@/views/admin/ArticleEditor.vue')
        },
        {
          path: 'users',
          name: 'AdminUserList',
          component: () => import('@/views/admin/UserList.vue')
        },
        {
          path: 'comments',
          name: 'AdminCommentList',
          component: () => import('@/views/admin/CommentList.vue')
        },
        {
          path: 'tags',
          name: 'AdminTagManage',
          component: () => import('@/views/admin/TagManage.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

router.beforeEach((to) => {
  if (to.meta.guest) {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      return { name: 'Home' }
    }
  }

  if (to.meta.requiresAdmin) {
    const adminUserStore = useAdminUserStore()
    if (!adminUserStore.isAdmin) {
      return { name: 'NotFound' }
    }
  }

  if (to.meta.requiresAuth) {
    const adminUserStore = useAdminUserStore()
    if (!adminUserStore.isLoggedIn) {
      return { name: 'AdminLogin', query: { redirect: to.fullPath } }
    }
  }

  if (to.name === 'AdminLogin') {
    const adminUserStore = useAdminUserStore()
    if (adminUserStore.isLoggedIn && adminUserStore.isAdmin) {
      return { name: 'AdminDashboard' }
    }
  }
})

export default router
