package com.yvmoux.blog.schedule;

import cn.hutool.core.collection.CollUtil;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class ViewCountSyncTask {

    private final RedisUtils redisUtils;
    private final ArticleMapper articleMapper;

    /**
     * 定时同步文章阅读量，将 Redis 中缓存的增量数据持久化到数据库。
     * <p>
     * 遍历所有 "article:view:*" 格式的缓存键，对每个键依次执行：
     * <ol>
     *   <li>从键名中提取文章 ID</li>
     *   <li>原子获取当前阅读增量并重置计数</li>
     *   <li>若增量大于 0 则累加到数据库</li>
     * </ol>
     * </p>
     */
    @Scheduled(fixedRate = 300_000)
    public void syncViewCount() {
        // 获取所有文章阅读量缓存键
        Set<String> keys = redisUtils.keys("article:view:*");
        if (CollUtil.isEmpty(keys)) return;

        for (String key : keys) {
            try {
                // 从缓存键中提取文章 ID 并获取阅读增量
                String idStr = key.substring(key.lastIndexOf(":") + 1);
                Long articleId = Long.parseLong(idStr);
                Long incrCount = redisUtils.getSet(key, 0L);

                if (incrCount != null && incrCount > 0) {
                    articleMapper.incrementViewCount(articleId, incrCount);
                }
            } catch (Exception e) {
                log.error("同步文章阅读量失败: key={}", key, e);
            }
        }
        log.info("阅读量同步完成，处理 {} 篇文章", keys.size());
    }
}
