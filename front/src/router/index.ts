import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
      path: '/tags',
      name: 'TagList',
      component: () => import('@/views/TagList.vue')
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
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/dashboard/write',
      name: 'ArticleWrite',
      component: () => import('@/views/ArticleWrite.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/dashboard/write/:id',
      name: 'ArticleEdit',
      component: () => import('@/views/ArticleWrite.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/Settings.vue'),
      meta: { requiresAuth: true }
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
  const userStore = useUserStore()

  if (to.meta.guest) {
    if (userStore.isLoggedIn) {
      return { name: 'Home' }
    }
  }

  if (to.meta.requiresAdmin) {
    if (!userStore.isAdmin) {
      return { name: 'NotFound' }
    }
  }

  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
