import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      component: () => import('@/components/layout/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
        {
          path: 'articles',
          name: 'ArticleList',
          component: () => import('@/views/ArticleList.vue')
        },
        {
          path: 'articles/editor',
          name: 'ArticleNew',
          component: () => import('@/views/ArticleEditor.vue')
        },
        {
          path: 'articles/editor/:id',
          name: 'ArticleEdit',
          component: () => import('@/views/ArticleEditor.vue')
        },
        {
          path: 'users',
          name: 'UserList',
          component: () => import('@/views/UserList.vue')
        },
        {
          path: 'comments',
          name: 'CommentList',
          component: () => import('@/views/CommentList.vue')
        },
        {
          path: 'tags',
          name: 'TagManage',
          component: () => import('@/views/TagManage.vue')
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

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ name: 'NotFound' })
  }

  if (to.name === 'Login' && userStore.isLoggedIn) {
    return next({ name: 'Dashboard' })
  }

  next()
})

export default router
