package com.paolua.tomatoclockbackend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 更新任务请求DTO
 */
@Data
public class TaskUpdateRequest {

    /**
     * 任务ID（必填）
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /**
     * 任务名称（可选）
     */
    private String taskName;

    /**
     * 总需番茄个数（可选）
     */
    private Integer totalTomatoNum;

    /**
     * 单个番茄时长（分钟，可选）
     */
    private Integer tomatoDuration;

    /**
     * 休息时长（分钟，可选）
     */
    private Integer restDuration;

    /**
     * 截止日期（可选）
     */
    private LocalDate deadline;

    /**
     * 任务状态（可选）
     */
    private Integer status;
}
