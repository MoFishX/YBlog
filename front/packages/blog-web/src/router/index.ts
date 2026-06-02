import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
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
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

export default router
