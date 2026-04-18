package com.paolua.tomatoclockbackend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * 创建任务请求DTO
 */
@Data
public class TaskCreateRequest {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 任务类型：1=待办模式，2=长期模式
     */
    @NotNull(message = "任务类型不能为空")
    private Integer taskType;

    /**
     * 总需番茄个数
     */
    @Positive(message = "番茄个数必须大于0")
    private Integer totalTomatoNum;

    /**
     * 单个番茄时长（分钟）
     */
    private Integer tomatoDuration = 25;

    /**
     * 休息时长（分钟）
     */
    private Integer restDuration = 5;

    /**
     * 截止日期（长期模式必填）
     */
    private LocalDate deadline;
}
