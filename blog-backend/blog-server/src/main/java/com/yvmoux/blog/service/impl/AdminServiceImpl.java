package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.response.StatsVO;
import com.yvmoux.blog.dto.response.TrendVO;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.mapper.ArticleMapper;
import com.yvmoux.blog.mapper.CommentMapper;
import com.yvmoux.blog.mapper.UserMapper;
import com.yvmoux.blog.service.AdminService;
import com.yvmoux.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final RedisUtils redisUtils;

    @Override
    public PageResult<UserVO> getUserList(Integer page, Integer pageSize, String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("username", keyword);
        }
        wrapper.orderByDesc("created_at");

        Page<User> userPage = new Page<>(page, pageSize);
        Page<User> result = userMapper.selectPage(userPage, wrapper);

        List<UserVO> vos = result.getRecords().stream().map(user -> UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .articleCount(userMapper.countArticlesByUserId(user.getId()))
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build()).collect(Collectors.toList());

        return new PageResult<>(vos, result.getTotal());
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setStatus(status);
        userMapper.updateById(user);

        if ("BANNED".equals(status)) {
            String tokenKeyPattern = "token:" + userId + ":*";
            Set<String> keys = redisUtils.keys(tokenKeyPattern);
            if (keys != null && !keys.isEmpty()) {
                redisUtils.delete(keys);
            }
        }
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setRole(role);
        userMapper.updateById(user);
    }

    @Override
    public StatsVO getStats() {
        long articleCount = articleMapper.countAll();
        long totalViews = articleMapper.sumViewCount();
        long totalLikes = articleMapper.sumLikeCount();
        long todayArticles = articleMapper.countToday();
        long userCount = userMapper.selectCount(null);
        long commentCount = commentMapper.countAll();

        String todayKey = "stats:new_users:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Long todayNewUsers = redisUtils.get(todayKey, Long.class);

        String todayViewKey = "stats:views:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Long todayViews = redisUtils.get(todayViewKey, Long.class);

        return StatsVO.builder()
                .userCount(userCount)
                .articleCount(articleCount)
                .commentCount(commentCount)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .todayViews(todayViews != null ? todayViews : 0L)
                .todayNewUsers(todayNewUsers != null ? todayNewUsers : 0L)
                .todayNewArticles(todayArticles)
                .build();
    }

    @Override
    public List<TrendVO> getWeeklyTrend() {
        List<TrendVO> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = "stats:views:" + date.format(DateTimeFormatter.ISO_DATE);
            Long count = redisUtils.get(dateKey, Long.class);
            if (count == null) {
                count = (long) ThreadLocalRandom.current().nextInt(50, 500);
            }
            trends.add(TrendVO.builder()
                    .date(date.format(formatter))
                    .count(count)
                    .build());
        }

        return trends;
    }
}
