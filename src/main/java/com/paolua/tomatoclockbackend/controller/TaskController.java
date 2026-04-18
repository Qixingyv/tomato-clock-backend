package com.paolua.tomatoclockbackend.controller;

import com.paolua.tomatoclockbackend.common.response.Result;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.TaskCreateRequest;
import com.paolua.tomatoclockbackend.dto.TaskDTO;
import com.paolua.tomatoclockbackend.dto.TaskUpdateRequest;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务控制器
 * 处理任务相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取任务列表
     *
     * @param status    状态过滤（可选）
     * @param taskType  任务类型过滤（可选）
     * @param authHeader Authorization header
     * @return 任务列表
     */
    @GetMapping
    public Result<List<TaskDTO>> getTasks(
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Byte taskType,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        List<Task> tasks;
        if (status != null && taskType != null) {
            // 同时按状态和类型过滤
            tasks = taskService.getTasksByStatus(userId, status);
            tasks = tasks.stream()
                    .filter(t -> t.getTaskType().equals(taskType))
                    .collect(Collectors.toList());
        } else if (status != null) {
            tasks = taskService.getTasksByStatus(userId, status);
        } else if (taskType != null) {
            tasks = taskService.getTasksByTaskType(userId, taskType);
        } else {
            tasks = taskService.getTasks(userId);
        }

        List<TaskDTO> dtos = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Result.success(dtos);
    }

    /**
     * 获取单个任务
     *
     * @param id        任务ID
     * @param authHeader Authorization header
     * @return 任务详情
     */
    @GetMapping("/{id}")
    public Result<TaskDTO> getTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        Task task = taskService.getTaskById(id);

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权访问此任务");
        }

        return Result.success(convertToDTO(task));
    }

    /**
     * 创建任务
     *
     * @param request   创建请求
     * @param authHeader Authorization header
     * @return 创建的任务
     */
    @PostMapping
    public Result<TaskDTO> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        Task task = taskService.createTask(request, userId);

        log.info("创建任务成功: userId={}, taskId={}", userId, task.getId());
        return Result.success(convertToDTO(task));
    }

    /**
     * 更新任务
     *
     * @param id        任务ID
     * @param request   更新请求
     * @param authHeader Authorization header
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        request.setId(id);
        boolean success = taskService.updateTask(request, userId);

        if (success) {
            log.info("更新任务成功: userId={}, taskId={}", userId, id);
            return Result.success();
        } else {
            return Result.error("更新任务失败");
        }
    }

    /**
     * 删除任务
     *
     * @param id        任务ID
     * @param authHeader Authorization header
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        boolean success = taskService.deleteTask(id, userId);

        if (success) {
            log.info("删除任务成功: userId={}, taskId={}", userId, id);
            return Result.success();
        } else {
            return Result.error("删除任务失败");
        }
    }

    /**
     * 完成番茄
     *
     * @param id        任务ID
     * @param request   完成请求
     * @param authHeader Authorization header
     * @return 成功响应
     */
    @PostMapping("/{id}/complete")
    public Result<TaskDTO> completeTomato(
            @PathVariable Long id,
            @RequestBody CompleteTomatoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        boolean success = taskService.completeTomato(id, userId, request.getActualDuration());

        if (success) {
            Task task = taskService.getTaskById(id);
            log.info("完成番茄成功: userId={}, taskId={}", userId, id);
            return Result.success(convertToDTO(task));
        } else {
            return Result.error("完成番茄失败");
        }
    }

    /**
     * 恢复已取消的任务
     *
     * @param id        任务ID
     * @param authHeader Authorization header
     * @return 成功响应
     */
    @PostMapping("/{id}/restore")
    public Result<Void> restoreCancelledTask(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        boolean success = taskService.restoreCancelledTask(id, userId);

        if (success) {
            log.info("恢复任务成功: userId={}, taskId={}", userId, id);
            return Result.success();
        } else {
            return Result.error("恢复任务失败");
        }
    }

    /**
     * 兼容旧接口
     * @deprecated 使用 GET /api/v1/tasks 替代
     */
    @Deprecated
    @GetMapping("/by-user/{userId}")
    public Result<List<Task>> getTomatoListByUserId(@PathVariable Integer userId) {
        return Result.success(taskService.getTomatoTaskListByUserId(userId));
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new com.paolua.tomatoclockbackend.common.exception.BusinessException(
                    com.paolua.tomatoclockbackend.common.response.ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 转换为DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setTaskType(task.getTaskType().intValue());
        dto.setTomatoDuration(task.getTomatoDuration());
        dto.setRestDuration(task.getRestDuration());
        dto.setTotalTomatoNum(task.getTotalTomatoNum());
        dto.setCompletedTomatoNum(task.getCompletedTomatoNum());
        dto.setDeadline(task.getDeadline());
        dto.setStatus(task.getStatus().intValue());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        return dto;
    }

    /**
     * 完成番茄请求内部类
     */
    @lombok.Data
    public static class CompleteTomatoRequest {
        /**
         * 实际完成时长（秒）
         */
        private Integer actualDuration;
    }
}
