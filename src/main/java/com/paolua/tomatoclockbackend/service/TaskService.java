package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.dto.TaskCreateRequest;
import com.paolua.tomatoclockbackend.dto.TaskUpdateRequest;
import com.paolua.tomatoclockbackend.pojo.Task;

import java.util.List;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 获取用户的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<Task> getTasks(Long userId);

    /**
     * 根据状态获取用户任务列表
     *
     * @param userId 用户ID
     * @param status 任务状态
     * @return 任务列表
     */
    List<Task> getTasksByStatus(Long userId, Byte status);

    /**
     * 根据任务类型获取用户任务列表
     *
     * @param userId 用户ID
     * @param taskType 任务类型
     * @return 任务列表
     */
    List<Task> getTasksByTaskType(Long userId, Byte taskType);

    /**
     * 根据ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    Task getTaskById(Long taskId);

    /**
     * 创建任务
     *
     * @param request 创建请求
     * @param userId 用户ID
     * @return 创建的任务
     */
    Task createTask(TaskCreateRequest request, Long userId);

    /**
     * 更新任务
     *
     * @param request 更新请求
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateTask(TaskUpdateRequest request, Long userId);

    /**
     * 删除任务（软删除）
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteTask(Long taskId, Long userId);

    /**
     * 完成番茄
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param actualDuration 实际完成时长（秒）
     * @return 是否成功
     */
    boolean completeTomato(Long taskId, Long userId, Integer actualDuration);

    /**
     * 检查今日重置（长期任务每日完成数重置）
     *
     * @param userId 用户ID
     */
    void checkTodayReset(Long userId);

    /**
     * 恢复已取消的任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean restoreCancelledTask(Long taskId, Long userId);

    /**
     * 兼容旧方法
     * @deprecated 使用 getTasks 替代
     */
    @Deprecated
    List<Task> getTomatoTaskListByUserId(Integer userId);
}
