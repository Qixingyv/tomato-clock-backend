package com.paolua.tomatoclockbackend.controller;

import com.paolua.tomatoclockbackend.common.response.Result;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.StatisticsDTO;
import com.paolua.tomatoclockbackend.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计控制器
 * 处理统计数据查询请求
 */
@RestController
@RequestMapping("/api/v1/statistics")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取今日统计数据
     *
     * @param authHeader Authorization header
     * @return 今日统计
     */
    @GetMapping("/today")
    public Result<StatisticsDTO> getTodayStatistics(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        StatisticsDTO stats = statisticsService.getTodayStatistics(userId);
        return Result.success(stats);
    }

    /**
     * 获取本周统计数据
     *
     * @param authHeader Authorization header
     * @return 本周统计
     */
    @GetMapping("/week")
    public Result<StatisticsDTO> getWeekStatistics(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        StatisticsDTO stats = statisticsService.getWeekStatistics(userId);
        return Result.success(stats);
    }

    /**
     * 获取本月统计数据
     *
     * @param authHeader Authorization header
     * @return 本月统计
     */
    @GetMapping("/month")
    public Result<StatisticsDTO> getMonthStatistics(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        StatisticsDTO stats = statisticsService.getMonthStatistics(userId);
        return Result.success(stats);
    }

    /**
     * 获取自定义日期范围统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param authHeader Authorization header
     * @return 统计数据
     */
    @GetMapping("/custom")
    public Result<StatisticsDTO> getCustomRangeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String authHeader) {

        if (startDate.isAfter(endDate)) {
            return Result.error("开始日期不能晚于结束日期");
        }

        Long userId = getUserIdFromToken(authHeader);
        StatisticsDTO stats = statisticsService.getCustomRangeStatistics(userId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new com.paolua.tomatoclockbackend.common.exception.BusinessException(
                    com.paolua.tomatoclockbackend.common.response.ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}
