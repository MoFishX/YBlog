# 高性能博客平台 — 后端开发文档

> 基于 Spring Boot 3 + MyBatis-Plus + MySQL + Redis + Spring @Async 的博客后端工程

---

## 一、技术栈

| 类别         | 选型                       | 说明                              |
| ------------ | -------------------------- | --------------------------------- |
| 基础框架     | Spring Boot 3 + Spring MVC | 主框架，RESTful API               |
| ORM          | MyBatis-Plus               | 减少模板代码，分页插件            |
| 数据库       | MySQL 8                    | 主数据存储                        |
| 缓存         | Redis 7                    | 热点缓存 / 排行榜 / 阅读量计数    |
| 异步任务     | Spring @Async              | 异步解耦：邮件                  |
| 安全认证     | Spring Security + JWT      | 无状态认证 + 角色鉴权             |
| API 文档     | Knife4j (Swagger 增强)     | 接口文档自动生成 + 在线调试       |
| 参数校验     | Jakarta Validation         | `@Valid` + 分组校验               |
| 对象转换     | MapStruct                  | Entity ↔ VO ↔ DTO                 |
| 工具库       | Hutool                     | 常用工具（雪花ID、JSON 等）       |
| 日志         | Logback + SLF4J            | 统一日志输出                      |
| 构建         | Maven                      | 依赖管理 + 多环境 Profile         |
| 部署         | Docker + Docker Compose    | 容器化编排                        |

---

## 二、项目目录结构

```
blog-backend/
├── pom.xml
├── Dockerfile
├── docker-compose.yml                        # 本地开发中间件编排
└── src/
    ├── main/
    │   ├── java/com/blog/
    │   │   ├── BlogApplication.java          # 启动类
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java       # Spring Security 配置
    │   │   │   ├── JwtConfig.java            # JWT 密钥 + 过期时间
    │   │   │   ├── RedisConfig.java          # Redis 序列化配置
│   │   │   ├── AsyncConfig.java          # 异步任务线程池配置
│   │   │   ├── MyBatisPlusConfig.java    # 分页插件
    │   │   │   ├── WebMvcConfig.java         # CORS / 拦截器注册
    │   │   │   └── Knife4jConfig.java        # API 文档配置
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java  # JWT 验证过滤器
    │   │   │   ├── JwtTokenProvider.java         # Token 生成 / 解析
    │   │   │   ├── UserDetailsServiceImpl.java   # 加载用户信息
    │   │   │   └── SecurityUtils.java            # 获取当前登录用户
    │   │   ├── controller/
    │   │   │   ├── AuthController.java       # 登录 / 注册
    │   │   │   ├── ArticleController.java    # 文章 CRUD + 搜索
    │   │   │   ├── CommentController.java    # 评论
    │   │   │   ├── TagController.java        # 标签
    │   │   │   └── UserController.java       # 用户信息
│   │   ├── service/
│   │   │   ├── ArticleService.java
│   │   │   ├── impl/ArticleServiceImpl.java
│   │   │   ├── CommentService.java
│   │   │   ├── impl/CommentServiceImpl.java
│   │   │   ├── AuthService.java
│   │   │   ├── impl/AuthServiceImpl.java
│   │   │   ├── AsyncTaskService.java      # 异步任务服务
    │   │   ├── mapper/
    │   │   │   ├── ArticleMapper.java
    │   │   │   ├── CommentMapper.java
    │   │   │   ├── UserMapper.java
    │   │   │   └── TagMapper.java
    │   │   ├── entity/
    │   │   │   ├── Article.java
    │   │   │   ├── Comment.java
    │   │   │   ├── User.java
    │   │   │   └── Tag.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── ArticleCreateRequest.java
    │   │   │   │   ├── ArticleQueryRequest.java
    │   │   │   │   └── CommentCreateRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java      # 统一响应体
    │   │   │       ├── PageResult.java       # 分页结果
    │   │   │       ├── LoginResponse.java
    │   │   │       └── ArticleVO.java        # 文章视图对象
    │   │   ├── converter/
    │   │   │   └── ArticleConverter.java     # MapStruct 转换器
    │   │   ├── enums/
    │   │   │   ├── RoleEnum.java             # 角色枚举
    │   │   │   └── ArticleStatusEnum.java    # 文章状态枚举
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
    │   │   │   ├── BusinessException.java     # 业务异常
    │   │   │   └── ErrorCode.java             # 错误码枚举
    │   │   ├── schedule/
    │   │   │   └── ViewCountSyncTask.java     # 阅读量定时落库
    │   │   └── utils/
    │   │       ├── RedisUtils.java            # Redis 操作封装
    │   │       └── SnowflakeIdGenerator.java  # 分布式 ID（可选）
    │   └── resources/
    │       ├── application.yml                # 主配置
    │       ├── application-dev.yml            # 开发环境
    │       ├── application-prod.yml           # 生产环境
    │       └── mapper/
    │           ├── ArticleMapper.xml
    │           └── CommentMapper.xml
    └── test/
        └── java/com/blog/
            ├── controller/
            └── service/
```

---

## 三、环境搭建

### 3.1 前置要求

- JDK 17+
- Maven 3.8+
- MySQL 8
- Redis 7

### 3.2 Docker Compose — 中间件一键启动

```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: blog
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

```bash
docker compose up -d
```

### 3.3 Maven 依赖 (`pom.xml` 核心片段)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.7</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- Knife4j -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.4.0</version>
    </dependency>

    <!-- Hutool -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.25</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 3.4 配置文件 (`application.yml`)

```yaml
spring:
  profiles:
    active: dev

  datasource:
    url: jdbc:mysql://localhost:3306/blog?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      id-type: auto
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

jwt:
  secret: your-base64-encoded-secret-key-at-least-256-bits
  expiration: 86400000  # 24 小时

# Knife4j
springdoc:
  swagger-ui:
    path: /swagger-ui.html

server:
  port: 8080
```

---

## 四、核心模块设计

### 4.1 统一响应体 (`ApiResponse<T>`)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

所有 Controller 返回 `ApiResponse<T>`，前端统一解析。

### 4.2 JWT 认证流程

```
[Client]                    [Server]
   |                            |
   |--- POST /auth/login ------>| ① 校验用户名密码
   |                            | ② 生成 JWT (sub=userId, role, exp)
   |<-- { token, user } -------| ③ 返回 Token + 用户信息
   |                            |
   |--- GET /articles --------->| ④ Authorization: Bearer <token>
   |    (Header: Bearer token)  | ⑤ JwtAuthenticationFilter 解析 Token
   |                            | ⑥ 写入 SecurityContext
   |<-- 200 { data: [...] } ----| ⑦ @PreAuthorize 鉴权 → 返回数据
```

#### JwtTokenProvider

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### JwtAuthenticationFilter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
```

#### SecurityConfig

```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles", "/articles/*").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.3 阅读量统计优化（Redis 计数 + 定时落库）

#### 阅读流程

```
用户访问文章详情
    ↓
Controller 调用 ArticleService.getDetail(id)
    ↓
Service: redisUtils.incr("article:view:" + id, 1)   // Redis INCR
    ↓
Service: 查询 MySQL 返回文章基本信息（不含 viewCount 的实时准确值）
    ↓
拼装 ArticleVO: viewCount = redis + MySQL base
    ↓
返回前端
```

#### 定时任务落库

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class ViewCountSyncTask {

    private final RedisUtils redisUtils;
    private final ArticleMapper articleMapper;

    /**
     * 每 5 分钟将 Redis 阅读量增量同步到 MySQL
     */
    @Scheduled(fixedRate = 300_000)
    public void syncViewCount() {
        Set<String> keys = redisUtils.keys("article:view:*");
        if (CollUtil.isEmpty(keys)) return;

        for (String key : keys) {
            String idStr = key.substring(key.lastIndexOf(":") + 1);
            Long articleId = Long.parseLong(idStr);
            Long incrCount = redisUtils.getSet(key, "0"); // 取出并重置

            if (incrCount != null && incrCount > 0) {
                // UPDATE article SET view_count = view_count + #{incrCount} WHERE id = #{articleId}
                articleMapper.incrementViewCount(articleId, incrCount);
            }
        }
        log.info("阅读量同步完成，处理 {} 篇文章", keys.size());
    }
}
```

```java
// ArticleMapper.java
@Update("UPDATE article SET view_count = view_count + #{incrCount} WHERE id = #{id}")
void incrementViewCount(@Param("id") Long id, @Param("incrCount") Long incrCount);
```

> **面试要点**：Redis INCR 是 O(1) 原子操作，能抗住高并发；定时批量落库减少了 MySQL 写入频率；最终一致性（极端宕机场景可能丢失一个计数周期的增量）。

### 4.4 热门文章排行榜（Redis ZSet）

```java
// 阅读文章时更新分数
public void recordView(Long articleId) {
    // 阅读量权重 1 + 点赞数权重 2
    redisUtils.opsForZSet().incrementScore("hot:articles", String.valueOf(articleId), 1);
}

// 点赞时更新分数
public void recordLike(Long articleId) {
    redisUtils.opsForZSet().incrementScore("hot:articles", String.valueOf(articleId), 2);
}

// 获取 Top N
public List<ArticleVO> getHotArticles(int topN) {
    Set<ZSetOperations.TypedTuple<String>> tuples =
        redisUtils.opsForZSet().reverseRangeWithScores("hot:articles", 0, topN - 1);

    if (CollUtil.isEmpty(tuples)) return Collections.emptyList();

    // 批量查 MySQL
    List<Long> ids = tuples.stream()
        .map(t -> Long.parseLong(t.getValue()))
        .collect(Collectors.toList());

    return articleMapper.selectBatchIds(ids).stream()
        .map(articleConverter::toVO)
        .collect(Collectors.toList());
}
```

### 4.5 缓存经典问题处理

| 问题     | 方案                                                         | 代码位置          |
| -------- | ------------------------------------------------------------ | ----------------- |
| 穿透     | **空值缓存** — 查询不存在的文章 ID 也缓存一个 `null` 标记，TTL 设短（如 60s） | `ArticleServiceImpl.getDetail()` |
| 击穿     | **互斥锁** — `redis.setIfAbsent("lock:article:" + id, "1", 10, TimeUnit.SECONDS)`  获取锁后重建缓存，失败则自旋重试 | 同上              |
| 雪崩     | **随机过期** — 缓存 TTL 基础值 ± 随机偏移（如 `300 + random(60)` 秒） | `RedisUtils.set(key, val, ttl)` |

```java
// 击穿处理 — 互斥锁示例
public Article getArticleWithCache(Long id) {
    String cacheKey = "article:" + id;
    Article article = redisUtils.get(cacheKey, Article.class);

    if (article != null) return article; // 命中缓存（包括空值标记）

    // 未命中 → 加锁重建
    String lockKey = "lock:article:" + id;
    boolean locked = redisUtils.setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

    if (locked) {
        try {
            article = articleMapper.selectById(id);
            if (article != null) {
                redisUtils.set(cacheKey, article, 300 + ThreadLocalRandom.current().nextInt(60), TimeUnit.SECONDS);
            } else {
                // 空值缓存，防穿透
                redisUtils.set(cacheKey, new Article(), 60, TimeUnit.SECONDS);
            }
        } finally {
            redisUtils.delete(lockKey);
        }
    } else {
        // 自旋等待
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        return getArticleWithCache(id);
    }

    return article;
}
```

### 4.6 Spring @Async 异步解耦

#### 异步线程池配置

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

#### 异步任务服务

```java
@Slf4j
@Service
public class AsyncTaskService {

    @Async
    public void sendWelcomeEmail(String email) {
        log.info("收到邮件任务: {}", email);
        // TODO: 接入真实邮件服务
    }
}
```

#### 业务层调用

```java
// AuthServiceImpl.java - 注册成功后
asyncTaskService.sendWelcomeEmail(user.getEmail());
```

> **设计要点**：使用 Spring 内置 `@Async` 实现异步解耦，注册接口快速返回，邮件发送在后台线程执行，无需额外部署中间件。

### 4.7 接口限流（Redis + Lua）

```java
@Component
@Slf4j
public class RateLimiter {

    /**
     * 计数器限流 — 每个 IP 每分钟最多 N 次
     */
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        String script = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
        """;

        Long count = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList("rate:" + key),
            String.valueOf(windowSeconds)
        );

        return count != null && count <= limit;
    }
}
```

在需要限流的接口上使用拦截器或注解：

```java
@GetMapping("/articles/search")
public ApiResponse<PageResult<ArticleVO>> search(...) {
    String ip = HttpUtil.getClientIP(request);
    if (!rateLimiter.tryAcquire("search:" + ip, 30, 60)) {
        return ApiResponse.error(429, "请求过于频繁，请稍后再试");
    }
    // ...
}
```

### 4.8 全局异常处理

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("未捕获异常", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

---

## 五、数据库表设计

### 5.1 SQL DDL

```sql
CREATE DATABASE blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE `user` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(50) NOT NULL UNIQUE,
    `password`   VARCHAR(255) NOT NULL,
    `email`      VARCHAR(100),
    `avatar`     VARCHAR(255),
    `role`       VARCHAR(20) NOT NULL DEFAULT 'USER',   -- USER / ADMIN
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- 文章表
CREATE TABLE `article` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(200) NOT NULL,
    `summary`     VARCHAR(500),
    `cover_image` VARCHAR(255),
    `author_id`   BIGINT       NOT NULL,
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',  -- DRAFT / PUBLISHED
    `view_count`  BIGINT       NOT NULL DEFAULT 0,
    `like_count`  BIGINT       NOT NULL DEFAULT 0,
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL,
    `deleted_at`  DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_author` (`author_id`),
    INDEX `idx_created` (`created_at`),
    INDEX `idx_deleted` (`deleted_at`)
) ENGINE=InnoDB;

-- 文章内容表（分离大字段，避免列表查询拖慢性能）
CREATE TABLE `article_content` (
    `article_id` BIGINT   NOT NULL,
    `content`    LONGTEXT NOT NULL,
    PRIMARY KEY (`article_id`)
) ENGINE=InnoDB;

-- 评论表
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT       NOT NULL,
    `user_id`    BIGINT       NOT NULL,
    `content`    TEXT         NOT NULL,
    `parent_id`  BIGINT       DEFAULT NULL,   -- 支持回复评论
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_article` (`article_id`)
) ENGINE=InnoDB;

-- 标签表
CREATE TABLE `tag` (
    `id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50)  NOT NULL UNIQUE,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- 文章-标签关联表
CREATE TABLE `article_tag` (
    `article_id` BIGINT NOT NULL,
    `tag_id`     BIGINT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`)
) ENGINE=InnoDB;
```

### 5.2 Entity 示例

```java
@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String status;        // DRAFT / PUBLISHED
    private Long viewCount;
    private Long likeCount;

    @TableField(exist = false)    // 不映射到数据库
    private List<Tag> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 六、部署架构

```
                    ┌──────────────────┐
                    │   Nginx :80/443  │
                    └────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              │  /           │  /api/*      │
              ▼              ▼              │
    ┌─────────────┐  ┌─────────────┐        │
    │ 前端静态资源  │  │ Spring Boot  │        │
    │ (dist/)     │  │   :8080     │        │
    └─────────────┘  └──────┬──────┘        │
                            │               │
         ┌──────────────────┼───────────────┘
         │                  │
         ▼                  ▼
   ┌──────────┐
   │  Redis   │
   └──────────┘
         │
         ▼
   ┌──────────┐
   │   ES     │
   └──────────┘
```

#### Docker 部署命令

```bash
# 1. 构建后端镜像
mvn clean package -DskipTests
docker build -t blog-backend .

# 2. 编排启动
docker compose up -d
```

---

## 七、开发规范

### 8.1 命名约定

| 层次      | 命名规则                                 | 示例                          |
| --------- | ---------------------------------------- | ----------------------------- |
| Entity    | 数据库表名同名，驼峰                     | `Article.java`                |
| Mapper    | Entity + Mapper 后缀                     | `ArticleMapper.java`          |
| Service   | 接口直接命名；实现类 + Impl 后缀         | `ArticleServiceImpl.java`    |
| Controller| 模块名 + Controller 后缀                 | `ArticleController.java`      |
| DTO       | 功能 + Request/Response 后缀             | `ArticleCreateRequest.java`  |
| VO        | Entity + VO 后缀（与前端交互）           | `ArticleVO.java`              |
| 数据库表  | 小写蛇形                                 | `article`、`article_tag`      |
| 数据库字段 | 小写蛇形                                | `view_count`、`author_id`    |
| REST API  | 资源名复数 + 小写 + 连字符               | `/articles`、`/articles/{id}` |

### 8.2 分层职责

```
Controller  → 仅做参数校验 + 调用 Service + 组装 ApiResponse
Service     → 业务逻辑 + 事务管理
Mapper      → 数据访问（MyBatis / MyBatis-Plus）
Entity      → 数据库映射对象
DTO         → 接口入参
VO          → 接口出参（与 Entity 解耦，避免敏感字段泄露）
Converter   → Entity ↔ VO 对象转换（MapStruct）
```

### 8.3 Git 提交规范

```
feat: 新增文章全文搜索接口
fix: 修复阅读量定时任务重复计数问题
refactor: 抽取缓存逻辑到 CacheService
perf: 优化热门文章排行榜查询
docs: 更新 API 文档
```

### 8.4 代码审查 Checklist

- [ ] Controller 方法是否添加了 `@PreAuthorize` 或路由鉴权
- [ ] 写操作是否使用了 `@Transactional`（必要时）
- [ ] 敏感字段（password）是否在 VO 中排除
- [ ] 分页查询是否设置了合理的 `pageSize` 上限
- [ ] 异步任务是否做了异常处理和日志记录
- [ ] Redis Key 是否有统一的命名空间前缀
- [ ] 日志级别是否合理（`debug` / `info` / `warn` / `error`）

---

## 八、FAQ（常见问题）

**Q: 为什么写操作不直接同步更新 ES？**
A: 对于个人博客的低并发场景，无需引入 Elasticsearch 增加部署成本。全文搜索可通过 MySQL FULLTEXT 索引实现。

**Q: Token 过期了前端怎么处理？**
A: 后端返回 HTTP 401，前端 Axios 响应拦截器自动跳转登录页。也可增加 `/auth/refresh` 刷新 Token 接口（需配合 Refresh Token 机制）。

**Q: MyBatis-Plus 和 JPA 怎么选？**
A: MyBatis-Plus 更灵活，写复杂 SQL 方便（多表联查、动态条件）；JPA 更规范但学习曲线高。博客项目场景推荐 MyBatis-Plus。

**Q: 雪花算法 ID 有必要吗？**
A: 单机 MySQL 自增够用；若后续分库分表或期望 ID 不暴露业务规律，可引入 Hutool 的 `Snowflake`（`IdUtil.getSnowflake()`）。

---

## 九、参考资源

- [Spring Boot 3 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Spring Security 架构](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [Spring Async 文档](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Knife4j 文档](https://doc.xiaominfo.com/)
- [Hutool 文档](https://hutool.cn/)
