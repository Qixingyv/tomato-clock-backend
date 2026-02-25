package com.paolua.tomatoclockbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办/长期任务配置表
 * 对应数据库表：task
 *
 * @author PAOLUA
 * @date 2026-02-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    /**
     * 任务唯一标识（自增）
     */
    private Long id;

    /**
     * 所属用户ID，关联user.id
     */
    private Long userId; // 数据库字段user_id → Java驼峰命名userId

    /**
     * 任务名称（如：学习高数）
     */
    private String taskName; // 数据库字段task_name → Java驼峰命名taskName

    /**
     * 任务类型：1=待办模式，2=长期模式
     */
    private Byte taskType;

    /**
     * 总需番茄个数
     */
    private Integer totalTomatoNum; // 数据库字段total_tomato_num → 驼峰命名

    /**
     * 单个番茄时长（分钟）
     */
    private Integer tomatoDuration; // 数据库字段tomato_duration → 驼峰命名

    /**
     * 休息时长（分钟）
     */
    private Integer restDuration; // 数据库字段rest_duration → 驼峰命名

    /**
     * 截止日期（长期模式必填，用于日期倒数）
     */
    private LocalDate deadline; // 数据库date类型对应Java LocalDate

    /**
     * 已完成番茄个数，默认0
     */
    private Integer completedTomatoNum; // 数据库字段completed_tomato_num → 驼峰命名

    /**
     * 任务状态：1=未完成，2=已完成，3=已取消
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime; // 数据库datetime类型对应Java LocalDateTime

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}