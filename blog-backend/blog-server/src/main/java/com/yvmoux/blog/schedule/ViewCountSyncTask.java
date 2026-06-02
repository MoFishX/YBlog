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

    @Scheduled(fixedRate = 300_000)
    public void syncViewCount() {
        Set<String> keys = redisUtils.keys("article:view:*");
        if (CollUtil.isEmpty(keys)) return;

        for (String key : keys) {
            try {
                String idStr = key.substring(key.lastIndexOf(":") + 1);
                Long articleId = Long.parseLong(idStr);
                Long incrCount = redisUtils.getSet(key, "0");

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
