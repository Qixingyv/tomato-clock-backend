package com.paolua.tomatoclockbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 番茄完成记录实体
 * 对应数据库表：tomato_record
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TomatoRecord {

    /**
     * 记录唯一标识（自增）
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 关联任务ID
     */
    private Long taskId;

    /**
     * 任务快照：任务名称
     */
    private String taskName;

    /**
     * 任务快照：任务模式
     */
    private Integer taskMode;

    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 实际完成时长（秒）
     */
    private Integer actualDuration;

    /**
     * 是否提前结束
     */
    private Boolean isEarlyEnded;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
