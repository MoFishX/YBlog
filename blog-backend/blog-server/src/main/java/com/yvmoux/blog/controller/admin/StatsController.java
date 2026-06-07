package com.yvmoux.blog.controller.admin;

import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.StatsVO;
import com.yvmoux.blog.dto.response.TrendVO;
import com.yvmoux.blog.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理-统计")
@RestController("adminStatsController")
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class StatsController {

    private final AdminService adminService;

    @Operation(summary = "系统统计概览")
    @GetMapping
    public Result<StatsVO> stats() {
        log.info("获取系统统计概览");
        Result<StatsVO> result = Result.success(adminService.getStats());
        log.info("获取系统统计概览成功");
        return result;
    }

    @Operation(summary = "周访问趋势")
    @GetMapping("/weekly-trend")
    public Result<List<TrendVO>> weeklyTrend() {
        log.info("获取周访问趋势");
        Result<List<TrendVO>> result = Result.success(adminService.getWeeklyTrend());
        log.info("获取周访问趋势成功, days: {}", result.getData().size());
        return result;
    }
}
