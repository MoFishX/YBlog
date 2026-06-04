// ============================================================
// mockData.js — 前后端分离开发时的 Mock 数据方案
// 使用方式：在 request.ts 中引入即可，通过 USE_MOCK 切换
// ============================================================

export const USE_MOCK = false // 上线前改为 false

// ==================== 随机数据生成工具 ====================

let _idCounter = 0
function nextId() {
  return ++_idCounter
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function pick(arr) {
  return arr[randomInt(0, arr.length - 1)]
}

function pickN(arr, n) {
  const shuffled = [...arr].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, n)
}

function randomDate(daysBack = 365) {
  const now = Date.now()
  const past = now - daysBack * 86400000
  return new Date(past + Math.random() * (now - past)).toISOString()
}

function randomDateStr(daysBack = 365) {
  const d = new Date(randomDate(daysBack))
  return d.toISOString().replace('T', ' ').substring(0, 19)
}

// ==================== 静态素材库 ====================

const USERNAMES = [
  '程序员小明', '前端小张', '后端老李', '全栈老王', '设计师小花',
  '测试同学', '运维大大', '产品经理', '算法专家', '架构师',
  'React爱好者', 'Vue开发者', 'Node高手', 'Pythonista', 'Gopher'
]

const AVATARS = [
  'https://api.dicebear.com/9.x/thumbs/svg?seed=1',
  'https://api.dicebear.com/9.x/thumbs/svg?seed=2',
  'https://api.dicebear.com/9.x/thumbs/svg?seed=3',
  'https://api.dicebear.com/9.x/thumbs/svg?seed=4',
  'https://api.dicebear.com/9.x/thumbs/svg?seed=5',
  null
]

const TITLES = [
  '深入理解JavaScript闭包与作用域链',
  'React Hooks最佳实践与常见陷阱',
  'Vue3 Composition API实战指南',
  'Node.js性能优化：从入门到精通',
  'TypeScript高级类型体操全解析',
  '前端工程化之Monorepo架构设计',
  'CSS布局终极指南：Grid与Flexbox',
  'Webpack5模块联邦实战教程',
  '从零搭建企业级前端脚手架',
  '微前端架构设计与实现方案',
  '浏览器渲染原理深度剖析',
  'HTTP/3与QUIC协议详解',
  'GraphQL vs REST：接口设计对比',
  'Docker容器化前端部署实战',
  '前端性能监控与错误追踪方案',
  'Serverless架构在前端的应用',
  'WebAssembly入门与实践',
  '函数式编程在前端的应用',
  '设计模式在前端项目中的应用',
  '前端安全防护：XSS与CSRF'
]

const SUMMARIES = [
  '本文详细介绍了相关技术的核心概念和实际应用场景，帮助读者快速掌握要点。',
  '通过实际案例讲解技术原理，包含完整的代码示例和最佳实践建议。',
  '从基础概念出发，逐步深入，最终给出生产级别的解决方案。',
  '结合多年实战经验，总结了该技术的常见问题和解决方法。',
  '适合有一定基础的开发者阅读，文中提供了可复用的代码模板。'
]

const CONTENTS = [
  '<h1>概述</h1><p>这是一篇关于前端技术的深度文章，内容涵盖了从基础到进阶的知识点。</p><h2>基础知识</h2><p>首先我们需要理解一些基本概念...</p><pre><code>const foo = "hello world"</code></pre><h2>进阶内容</h2><p>在实际项目中我们需要注意以下几点...</p><blockquote>引用一段经典论述</blockquote><h2>总结</h2><p>通过本文的学习，你应该能够掌握核心要点并在实际项目中应用。</p>',
  '<h1>引言</h1><p>随着前端技术的不断发展，新的框架和工具层出不穷。</p><h2>核心概念</h2><p>我们先来梳理一下核心概念...</p><pre><code class="language-javascript">function App() {\n  return <div>Hello</div>\n}</code></pre><h2>实践案例</h2><p>下面通过一个完整的项目来演示...</p><h2>注意事项</h2><p>在实际开发中还需要注意以下几点...</p>',
  '<h1>背景</h1><p>在日常开发中经常遇到这个需求。</p><h2>解决方案</h2><p>经过调研决定采用以下方案...</p><pre><code class="language-typescript">interface Config {\n  name: string\n  version: string\n}</code></pre><h2>性能对比</h2><p>优化后的性能提升了约40%。</p><h2>总结</h2><p>这套方案已经在生产环境稳定运行超过半年。</p>'
]

const TAG_NAMES = [
  'JavaScript', 'TypeScript', 'Vue', 'React', 'Node.js',
  'CSS', '前端工程化', '性能优化', '架构设计', '算法',
  'Python', 'Go', 'Docker', '数据库', '微服务'
]

const COVER_IMAGES = [
  'https://picsum.photos/seed/1/800/400',
  'https://picsum.photos/seed/2/800/400',
  'https://picsum.photos/seed/3/800/400',
  'https://picsum.photos/seed/4/800/400',
  'https://picsum.photos/seed/5/800/400',
  null
]

const COMMENT_CONTENTS = [
  '写得非常好，受益匪浅！',
  '讲得很清楚，解决了我一直以来的困惑。',
  '期待更多这样的干货文章！',
  '有个小问题想请教一下作者...',
  '收藏了，以后慢慢看。',
  '点赞，代码示例很实用。',
  '文章总结得很到位，特别是性能优化那部分。',
  '希望作者能出一系列教程。',
  'mark一下，最近正好在学这个。',
  '感谢分享，已转发给团队同学。'
]

// ==================== 数据生成函数 ====================

function randomUser(overrides = {}) {
  return {
    id: nextId(),
    username: pick(USERNAMES),
    email: `${pick(['dev', 'coder', 'admin', 'user'])}${nextId()}@example.com`,
    avatar: pick(AVATARS),
    role: 'ADMIN',
    articleCount: randomInt(0, 120),
    status: pick(['ACTIVE', 'ACTIVE', 'ACTIVE', 'BANNED']),
    createdAt: randomDateStr(),
    ...overrides
  }
}

function randomTag(overrides = {}) {
  return {
    id: nextId(),
    name: pick(TAG_NAMES),
    articleCount: randomInt(0, 50),
    ...overrides
  }
}

function randomArticleListItem(overrides = {}) {
  const created = randomDateStr()
  return {
    id: nextId(),
    title: pick(TITLES),
    summary: pick(SUMMARIES),
    coverImage: pick(COVER_IMAGES),
    author: {
      id: randomInt(1, 20),
      username: pick(USERNAMES),
      avatar: pick(AVATARS)
    },
    tags: pickN(ALL_TAGS_CACHE, randomInt(2, 5)),
    viewCount: randomInt(100, 99999),
    likeCount: randomInt(0, 5000),
    commentCount: randomInt(0, 200),
    createdAt: created,
    updatedAt: Math.random() > 0.5 ? randomDateStr(Math.floor((Date.now() - new Date(created).getTime()) / 86400000)) : null,
    status: pick(['PUBLISHED', 'PUBLISHED', 'PUBLISHED', 'DRAFT']),
    ...overrides
  }
}

function randomArticle(overrides = {}) {
  return {
    id: nextId(),
    title: pick(TITLES),
    content: pick(CONTENTS),
    summary: pick(SUMMARIES),
    coverImage: pick(COVER_IMAGES),
    status: pick(['PUBLISHED', 'PUBLISHED', 'DRAFT', 'REVIEWING']),
    author: {
      id: randomInt(1, 20),
      username: pick(USERNAMES),
      avatar: pick(AVATARS)
    },
    tags: pickN(ALL_TAGS_CACHE, randomInt(2, 5)),
    viewCount: randomInt(100, 99999),
    likeCount: randomInt(0, 5000),
    isLiked: Math.random() > 0.5,
    commentCount: randomInt(0, 200),
    createdAt: randomDateStr(),
    updatedAt: randomDateStr(),
    ...overrides
  }
}

function randomComment(overrides = {}) {
  return {
    id: nextId(),
    content: pick(COMMENT_CONTENTS),
    user: {
      id: randomInt(1, 20),
      username: pick(USERNAMES),
      avatar: pick(AVATARS)
    },
    replyTo: Math.random() > 0.6 ? { id: randomInt(1, 50), username: pick(USERNAMES) } : null,
    createdAt: randomDateStr(30),
    ...overrides
  }
}

function randomHotArticle(rank) {
  return {
    rank,
    id: nextId(),
    title: pick(TITLES),
    summary: pick(SUMMARIES),
    viewCount: randomInt(5000, 99999),
    likeCount: randomInt(500, 5000),
    author: {
      id: randomInt(1, 20),
      username: pick(USERNAMES),
      avatar: pick(AVATARS)
    },
    createdAt: randomDateStr(7)
  }
}

function randomSearchArticle(overrides = {}) {
  return {
    id: nextId(),
    title: pick(TITLES),
    titleHighlight: pick(['<em>', '']) + pick(TITLES).substring(0, 6) + pick(['</em>', '']),
    summary: pick(SUMMARIES),
    contentHighlight: pick(['<em>关键词</em>在这里出现', '']),
    author: {
      id: randomInt(1, 20),
      username: pick(USERNAMES)
    },
    tags: pickN(ALL_TAGS_CACHE, randomInt(2, 4)),
    viewCount: randomInt(100, 99999),
    likeCount: randomInt(0, 5000),
    createdAt: randomDateStr(),
    ...overrides
  }
}

function randomReplyNotification(overrides = {}) {
  return {
    id: nextId(),
    content: pick(COMMENT_CONTENTS),
    articleId: randomInt(1, 100),
    articleTitle: pick(TITLES),
    user: {
      id: randomInt(1, 20),
      username: pick(USERNAMES)
    },
    isRead: Math.random() > 0.5,
    createdAt: randomDateStr(30),
    ...overrides
  }
}

function randomAdminComment(overrides = {}) {
  return {
    id: nextId(),
    content: pick(COMMENT_CONTENTS),
    articleId: randomInt(1, 100),
    articleTitle: pick(TITLES),
    user: {
      id: randomInt(1, 20),
      username: pick(USERNAMES)
    },
    status: pick(['NORMAL', 'NORMAL', 'NORMAL', 'HIDDEN']),
    createdAt: randomDateStr(30),
    ...overrides
  }
}

function randomDashboardStats() {
  return {
    userCount: randomInt(500, 50000),
    articleCount: randomInt(100, 10000),
    commentCount: randomInt(500, 50000),
    totalViews: randomInt(10000, 999999),
    totalLikes: randomInt(5000, 500000),
    todayViews: randomInt(100, 5000),
    todayNewUsers: randomInt(5, 50),
    todayNewArticles: randomInt(3, 30)
  }
}

function randomTrendItem(dateStr) {
  return {
    date: dateStr,
    count: randomInt(50, 5000)
  }
}

function buildTrendList() {
  const list = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    list.push(randomTrendItem(d.toISOString().substring(0, 10)))
  }
  return list
}

// ==================== 缓存：预生成的数据 ====================

const USERS_CACHE = Array.from({ length: 20 }, () => randomUser())
const ALL_TAGS_CACHE = Array.from({ length: 15 }, (_, i) => ({
  id: i + 1,
  name: TAG_NAMES[i] || pick(TAG_NAMES),
  articleCount: randomInt(0, 50)
}))
const ARTICLES_CACHE = Array.from({ length: 60 }, () => randomArticleListItem())
const ARTICLES_DETAIL_CACHE = new Map()
const HOT_ARTICLES_CACHE = Array.from({ length: 10 }, (_, i) => randomHotArticle(i + 1))

function getOrCreateArticleDetail(id) {
  if (!ARTICLES_DETAIL_CACHE.has(id)) {
    const base = ARTICLES_CACHE.find(a => a.id === Number(id)) || pick(ARTICLES_CACHE)
    ARTICLES_DETAIL_CACHE.set(id, randomArticle({ id: Number(id), title: base.title }))
  }
  return ARTICLES_DETAIL_CACHE.get(id)
}

// ==================== Pagination 辅助 ====================

function paginate(list, page = 1, pageSize = 10) {
  const start = (page - 1) * pageSize
  const records = list.slice(start, start + pageSize)
  return {
    records,
    total: list.length,
    page,
    pageSize
  }
}

// ==================== 响应包装 ====================

function ok(payload, config = {}) {
  return new Promise((resolve) => {
    // 模拟网络延迟 100-400ms
    setTimeout(() => {
      resolve({
        data: { code: 200, message: 'OK', data: payload },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
        request: {}
      })
    }, randomInt(100, 400))
  })
}

function fail(code, message, config = {}) {
  const error = new Error(`Request failed with status code ${code}`)
  error.config = config
  error.response = {
    status: code,
    statusText: '',
    headers: {},
    config,
    data: { code, message, data: null }
  }
  return Promise.reject(error)
}

// ==================== 路由匹配引擎 ====================

function matchUrl(url, patterns) {
  for (const [pattern, handler] of patterns) {
    // 支持 :param 动态路由
    const regexStr = pattern
      .replace(/\//g, '\\/')
      .replace(/:(\w+)/g, '(?<$1>[^/?]+)')
    const regex = new RegExp(`^${regexStr}$`)
    const match = url.match(regex)
    if (match) {
      return { handler, params: match.groups || {} }
    }
  }
  return null
}

function matchApi(method, url, data, config = {}) {
  // 去掉 query string
  const path = url.split('?')[0]

  // ========== Auth ==========
  if (method === 'post' && path === '/auth/login') {
    const user = pick(USERS_CACHE)
    return ok({ token: 'mock_token_' + nextId(), expiresIn: 7200, user }, config)
  }
  if (method === 'post' && path === '/auth/register') {
    return ok(null, config)
  }
  if (method === 'post' && path === '/auth/refresh') {
    return ok({ token: 'mock_refreshed_token_' + nextId(), expiresIn: 7200 }, config)
  }
  if (method === 'post' && path === '/auth/logout') {
    return ok(null, config)
  }

  // ========== User ==========
  if (method === 'get' && path === '/user/me') {
    const user = pick(USERS_CACHE)
    return ok({ ...user, articleCount: randomInt(0, 120), createdAt: randomDateStr() }, config)
  }
  const userMatch = matchUrl(path, [
    ['/user/:userId', null]
  ])
  if (method === 'get' && userMatch) {
    const user = USERS_CACHE.find(u => u.id === Number(userMatch.params.userId)) || pick(USERS_CACHE)
    return ok({
      id: user.id,
      username: user.username,
      avatar: user.avatar,
      articleCount: randomInt(0, 120),
      createdAt: randomDateStr()
    }, config)
  }
  if (method === 'put' && path === '/user/me') {
    const user = pick(USERS_CACHE)
    return ok({ ...user, ...data }, config)
  }
  if (method === 'put' && path === '/user/me/password') {
    return ok(null, config)
  }

  // ========== Tags ==========
  if (method === 'get' && path === '/tags') {
    return ok(ALL_TAGS_CACHE, config)
  }
  if (method === 'post' && path === '/tags') {
    return ok({ id: nextId(), name: data?.name || 'NewTag' }, config)
  }
  const tagMatch = matchUrl(path, [
    ['/tags/:tagId', null]
  ])
  if (method === 'put' && tagMatch) {
    return ok({ id: Number(tagMatch.params.tagId), name: data?.name || 'UpdatedTag' }, config)
  }
  if (method === 'delete' && tagMatch) {
    return ok(null, config)
  }

  // ========== Upload ==========
  if (method === 'post' && path === '/upload') {
    return ok({
      url: `https://picsum.photos/seed/${nextId()}/800/400`,
      filename: `upload_${nextId()}.jpg`
    }, config)
  }

  // ========== Articles ==========
  if (method === 'get' && path === '/articles') {
    return ok(paginate(ARTICLES_CACHE, data?.page || 1, data?.pageSize || 10), config)
  }
  if (method === 'get' && path === '/articles/hot') {
    const limit = data?.limit || 10
    return ok(HOT_ARTICLES_CACHE.slice(0, limit), config)
  }
  if (method === 'get' && path === '/articles/search') {
    const keyword = data?.keyword || ''
    const filtered = ARTICLES_CACHE.filter(a =>
      a.title.includes(keyword) || a.summary.includes(keyword)
    )
    const page = data?.page || 1
    const pageSize = data?.pageSize || 10
    const records = filtered.slice((page - 1) * pageSize, page * pageSize).map(a =>
      randomSearchArticle({ id: a.id, title: a.title })
    )
    return ok({ records, total: filtered.length, page, pageSize, took: randomInt(5, 50) }, config)
  }
  if (method === 'get' && path === '/articles/mine') {
    const mine = pickN(ARTICLES_CACHE, randomInt(3, 8))
    return ok(paginate(mine, data?.page || 1, data?.pageSize || 10), config)
  }
  if (method === 'post' && path === '/articles') {
    return ok({
      id: nextId(),
      title: data?.title || 'Untitled',
      status: data?.status || 'DRAFT',
      createdAt: randomDateStr()
    }, config)
  }
  const articleMatch = matchUrl(path, [
    ['/articles/:articleId', null],
    ['/articles/:articleId/like', null],
  ])
  if (method === 'get' && articleMatch && !path.endsWith('/like') && !path.endsWith('/comments')) {
    return ok(getOrCreateArticleDetail(articleMatch.params.articleId), config)
  }
  if (method === 'put' && articleMatch && !path.endsWith('/like')) {
    const id = Number(articleMatch.params.articleId)
    return ok({ id, title: data?.title || 'Updated', status: data?.status || 'PUBLISHED', updatedAt: randomDateStr() }, config)
  }
  if (method === 'delete' && articleMatch) {
    return ok(null, config)
  }
  if (method === 'post' && path.endsWith('/like')) {
    return ok({ isLiked: Math.random() > 0.5, likeCount: randomInt(0, 5000) }, config)
  }

  // ========== Comments ==========
  const commentsMatch = matchUrl(path, [
    ['/articles/:articleId/comments', null]
  ])
  if (method === 'get' && commentsMatch) {
    const comments = Array.from({ length: randomInt(3, 15) }, () =>
      randomComment({ replyTo: null })
    )
    return ok(paginate(comments, data?.page || 1, data?.pageSize || 10), config)
  }
  if (method === 'post' && commentsMatch) {
    return ok({ id: nextId(), content: data?.content || '', createdAt: randomDateStr() }, config)
  }
  if (method === 'get' && path === '/comments/replies') {
    const replies = Array.from({ length: randomInt(5, 20) }, () => randomReplyNotification())
    return ok(paginate(replies, data?.page || 1, data?.pageSize || 10), config)
  }
  const commentDeleteMatch = matchUrl(path, [
    ['/comments/:commentId', null]
  ])
  if (method === 'delete' && commentDeleteMatch) {
    return ok(null, config)
  }

  // ========== Admin: Users ==========
  if (method === 'get' && path === '/admin/users') {
    return ok(paginate(USERS_CACHE, data?.page || 1, data?.pageSize || 10), config)
  }
  const adminUserMatch = matchUrl(path, [
    ['/admin/users/:userId/status', null],
    ['/admin/users/:userId/role', null]
  ])
  if (method === 'put' && adminUserMatch) {
    const user = USERS_CACHE.find(u => u.id === Number(adminUserMatch.params.userId)) || pick(USERS_CACHE)
    if (path.endsWith('/status')) {
      return ok({ id: user.id, username: user.username, status: data?.status || 'ACTIVE' }, config)
    }
    if (path.endsWith('/role')) {
      return ok({ id: user.id, username: user.username, role: data?.role || 'USER' }, config)
    }
  }

  // ========== Admin: Articles ==========
  if (method === 'get' && path === '/admin/articles') {
    return ok(paginate(ARTICLES_CACHE, data?.page || 1, data?.pageSize || 10), config)
  }
  const adminArticleMatch = matchUrl(path, [
    ['/admin/articles/:articleId', null],
    ['/admin/articles/:articleId/review', null]
  ])
  if (method === 'delete' && adminArticleMatch) {
    return ok(null, config)
  }
  if (method === 'put' && path.endsWith('/review')) {
    const id = Number(adminArticleMatch.params.articleId)
    return ok({ id, title: pick(TITLES), status: data?.status || 'APPROVED' }, config)
  }
  if (method === 'post' && path === '/admin/articles/batch-delete') {
    const ids = data?.ids || []
    return ok({ deletedCount: ids.length || randomInt(1, 5) }, config)
  }

  // ========== Admin: Comments ==========
  if (method === 'get' && path === '/admin/comments') {
    const comments = Array.from({ length: randomInt(10, 30) }, () => randomAdminComment())
    return ok(paginate(comments, data?.page || 1, data?.pageSize || 10), config)
  }
  const adminCommentDeleteMatch = matchUrl(path, [
    ['/admin/comments/:commentId', null]
  ])
  if (method === 'delete' && adminCommentDeleteMatch) {
    return ok(null, config)
  }

  // ========== Admin: Dashboard ==========
  if (method === 'get' && path === '/admin/stats') {
    return ok(randomDashboardStats(), config)
  }
  if (method === 'get' && path === '/admin/stats/weekly-trend') {
    return ok(buildTrendList(), config)
  }

  // ========== 404 ==========
  console.warn(`[Mock] 未匹配的请求: ${method.toUpperCase()} ${url}`)
  return fail(404, `Mock not found: ${method.toUpperCase()} ${url}`, config)
}

// ==================== Axios 拦截器注入 ====================

/**
 * 在 axios 实例上注入 mock 拦截器
 * @param {import('axios').AxiosInstance} axiosInstance
 */
export function setupMock(axiosInstance) {
  axiosInstance.interceptors.request.use((config) => {
    if (!USE_MOCK) return config

    config.adapter = (cfg) => {
      const url = cfg.url || ''
      const queryParams = cfg.params || {}
      const bodyData = cfg.data ? (typeof cfg.data === 'string' ? JSON.parse(cfg.data) : cfg.data) : {}
      const mergedData = { ...queryParams, ...bodyData }
      return matchApi(cfg.method?.toLowerCase() || 'get', url, mergedData, cfg)
    }
    return config
  })
}

// ==================== 便捷：reset 数据 ====================

export function resetMockData() {
  _idCounter = 0
  USERS_CACHE.length = 0
  USERS_CACHE.push(...Array.from({ length: 20 }, () => randomUser()))
  ARTICLES_CACHE.length = 0
  ARTICLES_CACHE.push(...Array.from({ length: 60 }, () => randomArticleListItem()))
  ARTICLES_DETAIL_CACHE.clear()
  HOT_ARTICLES_CACHE.length = 0
  HOT_ARTICLES_CACHE.push(...Array.from({ length: 10 }, (_, i) => randomHotArticle(i + 1)))
}
