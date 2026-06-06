package com.yvmoux.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yvmoux.blog.converter.UserConverter;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final RedisUtils redisUtils;
    private final UserConverter userConverter;

    @Override
    public PageResult<UserVO> getUserList(Integer page, Integer pageSize, String keyword) {
        // 条件查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("username", keyword);
        }
        wrapper.orderByDesc("created_at");

        // 分页查询
        Page<User> userPage = userMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 构建返回值
        List<UserVO> userVOList = userConverter.toUserVOList(userPage.getRecords());

        return new PageResult<>(userVOList, userPage.getTotal());
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        // 获取用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新状态
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        // 获取用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新角色
        user.setRole(role);
        userMapper.updateById(user);
    }

    @Override
    public StatsVO getStats() {
        // 查询各项统计数据
        long articleCount = articleMapper.countAll();
        long totalViews = articleMapper.sumViewCount();
        long totalLikes = articleMapper.sumLikeCount();
        long todayArticles = articleMapper.countToday();
        long userCount = userMapper.selectCount(null);
        long commentCount = commentMapper.countAll();

        // 从 Redis 获取今日实时数据
        String todayKey = "stats:new_users:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Long todayNewUsers = redisUtils.get(todayKey, Long.class);
        String todayViewKey = "stats:views:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Long todayViews = redisUtils.get(todayViewKey, Long.class);

        // 构建返回值
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
        // 获取最近 7 天的访问趋势
        List<TrendVO> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = "stats:views:" + date.format(DateTimeFormatter.ISO_DATE);
            Long count = redisUtils.get(dateKey, Long.class);

            trends.add(TrendVO.builder()
                    .date(date.format(formatter))
                    .count(count != null ? count : 0L)
                    .build());
        }

        return trends;
    }
}
