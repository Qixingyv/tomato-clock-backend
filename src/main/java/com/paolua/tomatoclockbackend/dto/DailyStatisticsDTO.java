package com.paolua.tomatoclockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 每日统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatisticsDTO {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 完成的番茄数
     */
    private Integer completedTomatoes;

    /**
     * 专注时长（分钟）
     */
    private Long focusMinutes;
}
