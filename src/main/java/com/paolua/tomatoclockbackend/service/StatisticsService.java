package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.dto.StatisticsDTO;

import java.time.LocalDate;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取今日统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    StatisticsDTO getTodayStatistics(Long userId);

    /**
     * 获取本周统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    StatisticsDTO getWeekStatistics(Long userId);

    /**
     * 获取本月统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    StatisticsDTO getMonthStatistics(Long userId);

    /**
     * 获取自定义日期范围统计数据
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    StatisticsDTO getCustomRangeStatistics(Long userId, LocalDate startDate, LocalDate endDate);
}
