package com.paolua.tomatoclockbackend.common.constant;

/**
 * 任务相关常量定义
 */
public class TaskConstant {

    /**
     * 默认番茄时长（分钟）
     */
    public static final int DEFAULT_TOMATO_MINUTES = 25;

    /**
     * 默认休息时长（分钟）
     */
    public static final int DEFAULT_REST_MINUTES = 5;

    /**
     * 默认每日目标番茄数
     */
    public static final int DEFAULT_DAILY_TARGET = 5;

    /**
     * 任务模式：待办模式
     */
    public static final int TASK_MODE_TODO = 1;

    /**
     * 任务模式：计划模式
     */
    public static final int TASK_MODE_PLAN = 2;

    /**
     * 任务状态：进行中
     */
    public static final int TASK_STATUS_WORKING = 1;

    /**
     * 任务状态：已完成
     */
    public static final int TASK_STATUS_COMPLETED = 2;

    /**
     * 任务状态：已过期
     */
    public static final int TASK_STATUS_EXPIRED = 4;

    /**
     * 任务状态：已取消
     */
    public static final int TASK_STATUS_CANCELLED = 5;

    private TaskConstant() {
        // 防止实例化
    }
}
