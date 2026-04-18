package com.paolua.tomatoclockbackend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务数据传输对象
 * 用于向前端返回任务数据
 */
@Data
public class TaskDTO {

    /**
     * 任务唯一标识
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型：1=待办模式，2=长期模式
     */
    private Integer taskType;

    /**
     * 总需番茄个数
     */
    private Integer totalTomatoNum;

    /**
     * 单个番茄时长（分钟）
     */
    private Integer tomatoDuration;

    /**
     * 休息时长（分钟）
     */
    private Integer restDuration;

    /**
     * 截止日期（长期模式）
     */
    private LocalDate deadline;

    /**
     * 已完成番茄个数
     */
    private Integer completedTomatoNum;

    /**
     * 任务状态：1=未完成，2=已完成，3=已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 完成进度（计算属性）
     */
    public Integer getProgress() {
        if (totalTomatoNum == null || totalTomatoNum == 0) {
            return 0;
        }
        if (completedTomatoNum == null) {
            return 0;
        }
        return (int) ((completedTomatoNum * 100) / totalTomatoNum);
    }
}
