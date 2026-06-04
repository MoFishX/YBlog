package com.yvmoux.blog.service;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.response.StatsVO;
import com.yvmoux.blog.dto.response.TrendVO;
import com.yvmoux.blog.dto.response.UserVO;

import java.util.List;

public interface AdminService {
    PageResult<UserVO> getUserList(Integer page, Integer pageSize, String keyword);
    void updateUserStatus(Long userId, String status);
    void updateUserRole(Long userId, String role);
    StatsVO getStats();
    List<TrendVO> getWeeklyTrend();
}
