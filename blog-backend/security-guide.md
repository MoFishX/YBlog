# Spring Security + JWT 认证鉴权详解

> 本文档深入说明 Spring Security 在本博客项目中的应用原理，并补充 JWT 和 Security 的基础知识。

---

## 一、JWT 基础

### 1.1 JWT 是什么

JWT（JSON Web Token）是一个**自包含的防篡改令牌**，用于在网络间安全传递身份信息。它由三部分组成，用 `.` 分隔：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIn0.abc123def456
    ↑                         ↑                              ↑
   Header                   Payload                      Signature
```

| 部分 | Base64 解码后 | 说明 |
|------|-------------|------|
| **Header** | `{"alg": "HS256", "typ": "JWT"}` | 声明签名算法（本项目用 HMAC-SHA256） |
| **Payload** | `{"sub": "1", "role": "ADMIN", "exp": 1700000000}` | 存放用户 ID、角色、过期时间等业务数据 |
| **Signature** | `HMAC-SHA256(Header + "." + Payload, secretKey)` | 用密钥对前两部分签名 |

> **关键认知**：Payload 只是 Base64 编码，不是加密。任何人都可以解码看到内容，**绝对不能在 Payload 里存密码等敏感信息**。

### 1.2 为什么 JWT 是安全的——验签原理

假设攻击者截获了你的 Token，把 Payload 中的 `"role": "USER"` 改成 `"role": "ADMIN"`：

```
原始 Token:  Header.user.payload  +  signature（用"USER"参与计算）
篡改后请求:  Header.admin.payload +  signature（仍是旧签名）
```

服务端收到请求后：
1. 用同样的 `secretKey` 对 `Header.admin.payload` 重新计算签名
2. 拿新签名跟 Token 里的旧签名比对 → **不匹配** → 拒绝请求

攻击者改不了签名，因为他不知道 `secretKey`（只存在服务端）。这就是 JWT 防篡改的原理——**不是防止被人看到内容，而是防止内容被修改**。

### 1.3 Session vs Token——为什么叫"无状态"

**Session 模式（有状态）**

```
浏览器            服务器 A            服务器 B
  |                  |
  |--- 登录 -------->|  生成 sessionId = "abc123"
  |<-- Cookie --------|  内存存: {"abc123" → {userId: 1, role: "ADMIN"}}
  |                  |
  |--- 下次请求 ----->|  拿 sessionId 查内存 → 找到用户信息
```

问题：服务器 A 和 B 各存各的内存。请求落到 B 时，B 不认识 A 发的 sessionId → 被当作未登录。需要额外引入 Redis 共享 Session。

**Token 模式（无状态）**

```
浏览器            服务器 A            服务器 B
  |                  |                  |
  |--- 登录 -------->|  返回 JWT       |
  |<-- token --------|  (用户信息在 token 里，服务器不存) |
  |                  |                  |
  |--- 带 token 请求 ----------------->|  验签成功 → 从 token 中直接读到 userId+role
```

Token 里本身就装着用户信息，**任何一台服务器有密钥就能验证，不需要查任何共享存储**。这就是无状态——每台机器独立即可。

---

## 二、Spring Security 核心概念

### 2.1 过滤器链（Filter Chain）

Spring Security 本质上是一串 Filter 组成的管道，请求依次经过：

```
请求进入
    ↓
[Filter 1] → [Filter 2] → [Filter 3] → ... → [DispatcherServlet] → [Controller]
    ↑              ↑              ↑                                 ↑
  认证阶段      认证/过渡       鉴权阶段                         业务处理
```

两个关键概念对应链上的不同阶段：

| 概念 | 英文 | 问题 | 时机 |
|------|------|------|------|
| **认证** | Authentication | "你是谁？" | Filter 链前半段 |
| **鉴权** | Authorization | "你能做什么？" | Filter 链后半段 + `@PreAuthorize` |

### 2.2 SecurityContext——认证结果的存放容器

认证通过的标志产物：向 `SecurityContext` 里放入一个 `Authentication` 对象。

```
SecurityContext
    └── Authentication
        ├── getPrincipal()  →  用户 ID（本项目存 Long 型）
        ├── getAuthorities()  →  权限列表 [ROLE_ADMIN] 或 [ROLE_USER]
        └── isAuthenticated()  →  true（已认证）
```

后续所有鉴权操作都读这个容器：Filter 链后半段读它决定 403 还是放行，`@PreAuthorize` 读它判断角色，`SecurityUtils.getCurrentUserId()` 读它返回当前用户。

`SecurityContextHolder` 用 ThreadLocal 存放它，确保同一请求的所有代码读到同一份。

### 2.3 `.addFilterBefore()` 的作用——注册 Filter

Spring Security 默认已经有若干内置 Filter（表单登录、Session 管理、权限检查等），但你写的 `JwtAuthenticationFilter` 需要**手动注册**：

```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
```

这句话做了两件事：
1. **注册**：告诉 Spring Security，"我的 JwtFilter 也是 Filter 链上的一员，用它"
2. **定位**：指定它执行的位置——在 UsernamePasswordAuthFilter 之前

不写这行，`JwtFilter` 不会被 Spring Security 调用，等于没写这个 Filter。

### 2.4 为什么"之前"——认证要先于鉴权

```
时间线 →

JwtFilter（认证）          →           鉴权 Filter          →           Controller
写入 SecurityContext                   读取 SecurityContext            执行业务逻辑
(userId + role)                        (检查是否有权限)
```

JwtFilter 必须在鉴权 Filter 之前，否则鉴权时 SecurityContext 里是空的，鉴权判断"没登录"→ 403。

至于选谁作为"之前"的参照物不重要，`UsernamePasswordAuthenticationFilter` 只是链上一个众所周知的 Filter，方便做位置参照。写成 `.addFilterBefore(jwtFilter, AuthorizationFilter.class)` 效果一样。

---

## 三、本项目完整流程

### 3.1 注册

```
POST /auth/register  { username, password, email }
    → AuthServiceImpl.register()
        → 检查用户名是否重复
        → BCrypt 加密密码（存入数据库的不是明文）
        → 插入 user 表
        → 异步发送欢迎邮件
        → 返回成功
```

### 3.2 登录——发 Token

```
POST /auth/login  { username: "admin", password: "123456" }
    → AuthServiceImpl.login()
        → userMapper 查用户
        → passwordEncoder.matches(rawPassword, encodedPassword) 比对密码
        → jwtTokenProvider.generateToken(userId, role)
            → Jwts.builder()
                .subject("1")                       // sub: userId
                .claim("role", "ADMIN")             // 自定义字段
                .expiration(24小时后)                // 过期时间
                .signWith(secretKey)                 // 签名
                .compact()
            → 返回 "eyJhbGci...eyJzdWIiOiIxIi4..abc123"
        → 返回 { token, expiresIn: 86400000, user: {...} }
```

### 3.3 带 Token 请求——Filter 拦截

```
GET /articles/mine
Header: Authorization: Bearer eyJhbGci...
```

#### JwtAuthenticationFilter 的执行过程

```java
@Override
protected void doFilterInternal(request, response, chain) {
    // ===== 第 1 步：提取 Token =====
    String header = request.getHeader("Authorization");   // "Bearer eyJhbGci..."
    if (header == null || !header.startsWith("Bearer ")) {
        chain.doFilter(request, response);   // 没 Token，直接放下一个 Filter
        return;                               // 后续鉴权 Filter 发现未认证 → 403
    }
    String token = header.substring(7);                   // 去掉 "Bearer " 前缀

    // ===== 第 2 步：检查黑名单（Redis）=====
    if (redisUtils.hasKey("blacklist:token:" + token)) {
        chain.doFilter(request, response);   // Token 已登出，当作无 Token 处理
        return;
    }

    // ===== 第 3 步：验签 =====
    if (!jwtTokenProvider.validateToken(token)) {
        chain.doFilter(request, response);   // 签名不对或过期
        return;
    }

    // ===== 第 4 步：提取用户信息，写入 SecurityContext =====
    Long userId = jwtTokenProvider.getUserId(token);      // 从 Payload 取 sub
    String role = jwtTokenProvider.getRole(token);         // 从 Payload 取 role

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    var authToken = new UsernamePasswordAuthenticationToken(
        userId,        // principal：存 userId，方便 SecurityUtils 拿
        null,          // credentials：不用存密码
        authorities    // 权限：ROLE_ADMIN 或 ROLE_USER
    );
    SecurityContextHolder.getContext().setAuthentication(authToken);

    // ===== 第 5 步：放行 =====
    chain.doFilter(request, response);
}
```

### 3.4 SecurityConfig——路径级别鉴权

Filter 认证完成后，SecurityConfig 的规则决定谁可以访问什么：

```java
http.authorizeHttpRequests(auth -> auth
    // 公开接口（不需要登录）
    .requestMatchers("/auth/**", "/swagger-ui/**", "/swagger-ui.html",
                     "/v3/api-docs/**", "/webjars/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/articles", "/articles/*",
                     "/articles/hot").permitAll()
    .requestMatchers(HttpMethod.GET, "/articles/*/comments",
                     "/user/*", "/tags").permitAll()

    // 管理员专属
    .requestMatchers("/admin/**").hasRole("ADMIN")

    // 其余一切都要登录
    .anyRequest().authenticated()
);
```

```
执行逻辑（从上到下匹配，命中即停止）：

请求 GET /articles
    → 匹配 ".requestMatchers(GET, "/articles").permitAll()" → 放行 ✓

请求 POST /articles
    → 不匹配上面任何公开规则 → 走到 anyRequest().authenticated()
    → 检查 SecurityContext 里有认证信息 → 放行 ✓
    → 没有 → 401

请求 GET /admin/users
    → 不匹配公开规则 → 检查 hasRole("ADMIN")
    → role 是 ADMIN → 放行 ✓
    → role 是 USER → 403 Forbidden
```

### 3.5 @PreAuthorize —— 方法级别鉴权

路径规则只管"进哪个大门"，更细粒度的控制在方法上：

```java
// 只要登录了就能发文章
@PreAuthorize("isAuthenticated()")
public ApiResponse<ArticleVO> create(@Valid @RequestBody ArticleCreateRequest request) { ... }

// 只有作者本人或管理员才能编辑
@PreAuthorize("isAuthenticated()")
public ApiResponse<ArticleVO> update(@PathVariable Long articleId, ...) {
    // 在方法体里还加了一层：article.getAuthorId() == userId || isAdmin
}

// 整个 Controller 都要求管理员
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController { ... }
```

>`@PreAuthorize` 之所以能生效，靠的是 `@EnableMethodSecurity` 注解——它在 SecurityConfig 类上开启，底层生成 AOP 代理在方法执行前插入权限检查。

### 3.6 代码中获取当前用户

```java
// SecurityUtils 是对 SecurityContext 的封装
@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (Long) auth.getPrincipal();  // JwtFilter 存入的 userId
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
```

使用示例：
```java
Long userId = securityUtils.getCurrentUserId();
boolean isAdmin = securityUtils.isAdmin();
```

### 3.7 登出——Token 黑名单

JWT 一旦发出就无法主动废除（没有服务端状态），所以登出用的是**黑名单**：

```
POST /auth/logout
    → 从请求头取 Token
    → 用 JwtTokenProvider.getExpiration(token) 算剩余时间
    → redisUtils.set("blacklist:token:" + token, "1", 剩余时间)
    → 返回成功
```

下次请求时，JwtFilter 第 2 步先在 Redis 里查黑名单——命中就当作无 Token 处理。

---

## 四、配置速查表

| 配置 | 代码 | 原因 |
|------|------|------|
| 关闭 CSRF | `.csrf(disable)` | 前后端分离，Token 在 Header 里传而不是 Cookie，攻击者无法伪造请求头 |
| 无状态会话 | `.sessionStateless` | JWT 自带用户信息，不需要服务端 HttpSession |
| 注册 JwtFilter | `.addFilterBefore(jwtFilter, ...)` | 必须显式注册，否则 Filter 不执行；放在认证阶段，确保鉴权前有身份信息 |
| BCrypt | `new BCryptPasswordEncoder()` | 相同密码每次生成的密文不同，即使数据库泄露，攻击者也无法反推原密码 |
| 密码存 255 位 | `varchar(255)` | BCrypt 密文固定 60 字符，留 255 是惯例（未来可能换算法） |

---

## 五、面试常见追问

**Q: JWT 过期了怎么办？**
A: 前端收到 401 后跳转登录页。可配合 Refresh Token 机制（本项目有 `/auth/refresh` 接口）。

**Q: JWT 和 OAuth2 什么关系？**
A: JWT 是**格式**（令牌的数据结构），OAuth2 是**协议**（授权流程规范）。OAuth2 协议下通常用 JWT 格式做令牌。

**Q: 密文存储为什么不用 MD5？**
A: MD5 不可逆但可撞库（彩虹表）。BCrypt 自带随机盐值 + 可调节计算成本，是目前主流选择。

**Q: Filter 和 Interceptor 有什么区别？**
A: Filter 属于 Servlet 规范，在 DispatcherServlet 之前执行；Interceptor 属于 Spring MVC，在 Controller 之前执行。Security 鉴权必须在 Filter 层完成，因为 Security 本身就是一个 Filter 链，Interceptor 太晚了。
