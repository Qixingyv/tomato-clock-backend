package com.paolua.tomatoclockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务排行DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRankingDTO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 完成的番茄数
     */
    private Integer completedTomatoes;

    /**
     * 专注时长（分钟）
     */
    private Long focusMinutes;
}
