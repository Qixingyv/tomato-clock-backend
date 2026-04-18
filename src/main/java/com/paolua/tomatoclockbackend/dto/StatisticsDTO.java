package com.paolua.tomatoclockbackend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 统计数据DTO
 */
@Data
public class StatisticsDTO {

    /**
     * 统计周期：today/week/month
     */
    private String period;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 完成的番茄总数
     */
    private Integer completedTomatoes;

    /**
     * 专注总时长（分钟）
     */
    private Integer focusMinutes;

    /**
     * 完成的任务数
     */
    private Integer completedTasks;

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 完成率（百分比）
     */
    private Integer completionRate;

    /**
     * 每日数据列表
     */
    private List<DailyDataDTO> dailyData;

    /**
     * 任务排名列表
     */
    private List<TaskRankingDTO> taskRanking;

    /**
     * 每日数据DTO
     */
    @Data
    public static class DailyDataDTO {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 当日完成番茄数
         */
        private Integer completedTomatoes;

        /**
         * 当日专注时长（分钟）
         */
        private Integer focusMinutes;
    }

    /**
     * 任务排名DTO
     */
    @Data
    public static class TaskRankingDTO {
        /**
         * 任务ID
         */
        private Long taskId;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 任务类型
         */
        private Integer taskMode;

        /**
         * 完成番茄数
         */
        private Integer completedTomatoes;

        /**
         * 总完成番茄数
         */
        private Integer totalCompletedTomatoes;
    }
}
