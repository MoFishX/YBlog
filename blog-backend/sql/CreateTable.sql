-- 用户表
create table user (
                      id            bigint       not null auto_increment               comment '用户ID',
                      username      varchar(50)  not null                              comment '用户名',
                      password      varchar(255) not null                              comment '密码（Bcrypt加密）',
                      email         varchar(100)                                       comment '邮箱',
                      avatar        varchar(255)                                       comment '头像URL',
                      role          varchar(20)  not null default 'USER'               comment '角色：USER普通用户 / ADMIN管理员',
                      status        varchar(20)  not null default 'ACTIVE'             comment '状态：ACTIVE正常 / BANNED封禁',
                      created_at    datetime     not null                               comment '创建时间',
                      updated_at    datetime     not null                               comment '更新时间',
                      primary key (id),
                      unique key uk_username (username)
) engine=InnoDB;

-- 文章表
create table article (
                         id          bigint       not null auto_increment                comment '文章ID',
                         title       varchar(200) not null                               comment '标题',
                         summary     varchar(500)                                        comment '摘要',
                         ai_summary  longtext                                            comment 'AI生成的摘要',
                         ai_summary_long  longtext                                       comment 'AI生成的总结',
                         cover_image varchar(255)                                        comment '封面图URL',
                         author_id   bigint       not null                               comment '作者ID',
                         status      varchar(20)  not null default 'PUBLISHED'           comment '状态：DRAFT草稿 / PUBLISHED已发布',
                         view_count  bigint       not null default 0                     comment '阅读量',
                         like_count  bigint       not null default 0                     comment '点赞数',
                         created_at  datetime     not null                               comment '创建时间',
                         updated_at  datetime     not null                               comment '更新时间',
                         primary key (id),
                         index idx_author (author_id),
                         index idx_created (created_at),
                         fulltext index ft_article_title_summary (title, summary) with parser ngram
) engine=InnoDB;

-- 文章内容表
create table article_content (
                                 article_id bigint   not null comment '文章ID',
                                 content    longtext not null comment '内容（Markdown）',
                                 primary key (article_id),
                                 fulltext index ft_article_content (content) with parser ngram
) engine=InnoDB;

-- 评论表
create table comment (
                         id         bigint      not null auto_increment           comment '评论ID',
                         article_id bigint      not null                          comment '文章ID',
                         user_id    bigint      not null                          comment '评论用户ID',
                         content    text        not null                          comment '评论内容',
                         parent_id  bigint      default null                      comment '父评论ID（支持回复）',
                         status     varchar(20) not null default 'ACTIVE'         comment '状态：ACTIVE正常 / DELETED已删除',
                         is_read    tinyint     not null default 0                comment '是否已读：0未读 / 1已读',
                         created_at datetime    not null          comment '创建时间',
                         primary key (id),
                         index idx_article (article_id)
) engine=InnoDB;

-- 标签表
create table tag (
                     id   bigint      not null auto_increment comment '标签ID',
                     name varchar(50) not null                comment '标签名称',
                     primary key (id),
                     unique key uk_name (name)
) engine=InnoDB;

-- 文章-标签关联表
create table article_tag (
                             article_id bigint not null comment '文章ID',
                             tag_id     bigint not null comment '标签ID',
                             primary key (article_id, tag_id)
) engine=InnoDB;

-- 用户点赞表
create table user_like (
                           id         bigint   not null auto_increment           comment 'ID',
                           user_id    bigint   not null                          comment '用户ID',
                           article_id bigint   not null                          comment '文章ID',
                           created_at datetime not null comment '点赞时间',
                           primary key (id),
                           unique key uk_user_article (user_id, article_id)
) engine=InnoDB;