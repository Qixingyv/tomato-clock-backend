package com.paolua.tomatoclockbackend.service.impl;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.dto.TaskCreateRequest;
import com.paolua.tomatoclockbackend.dto.TaskUpdateRequest;
import com.paolua.tomatoclockbackend.mapper.TaskMapper;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务服务实现类
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TomatoRecordMapper recordMapper;

    public TaskServiceImpl(TaskMapper taskMapper, TomatoRecordMapper recordMapper) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public List<Task> getTasks(Long userId) {
        return taskMapper.selectByUserId(userId);
    }

    @Override
    public List<Task> getTasksByStatus(Long userId, Byte status) {
        return taskMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Task> getTasksByTaskType(Long userId, Byte taskType) {
        return taskMapper.selectByUserIdAndTaskType(userId, taskType);
    }

    @Override
    public Task getTaskById(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }

    @Override
    @Transactional
    public Task createTask(TaskCreateRequest request, Long userId) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTaskName(request.getTaskName());
        task.setTaskType(request.getTaskType().byteValue());
        task.setTomatoDuration(request.getTomatoDuration());
        task.setRestDuration(request.getRestDuration());
        task.setStatus((byte) 1); // 未完成状态
        task.setCompletedTomatoNum(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        if (request.getTaskType() == 1) {
            // 待办模式
            task.setTotalTomatoNum(request.getTotalTomatoNum());
            task.setDeadline(null);
        } else {
            // 长期模式
            task.setTotalTomatoNum(request.getTotalTomatoNum());
            task.setDeadline(request.getDeadline());
        }

        int result = taskMapper.insert(task);
        if (result <= 0) {
            throw new BusinessException("任务创建失败");
        }

        log.info("创建任务成功: taskId={}, userId={}, name={}", task.getId(), userId, task.getTaskName());
        return task;
    }

    @Override
    @Transactional
    public boolean updateTask(TaskUpdateRequest request, Long userId) {
        Task task = taskMapper.selectById(request.getId());
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (request.getTaskName() != null) {
            task.setTaskName(request.getTaskName());
        }
        if (request.getTomatoDuration() != null) {
            task.setTomatoDuration(request.getTomatoDuration());
        }
        if (request.getRestDuration() != null) {
            task.setRestDuration(request.getRestDuration());
        }
        if (request.getTotalTomatoNum() != null) {
            task.setTotalTomatoNum(request.getTotalTomatoNum());
        }
        if (request.getDeadline() != null && task.getTaskType() == 2) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus().byteValue());
        }

        task.setUpdateTime(LocalDateTime.now());
        int result = taskMapper.updateById(task);

        log.info("更新任务成功: taskId={}, userId={}", task.getId(), userId);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 软删除 - 设置状态为已取消
        int result = taskMapper.softDeleteById(taskId);

        log.info("删除任务成功: taskId={}, userId={}", taskId, userId);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean completeTomato(Long taskId, Long userId, Integer actualDuration) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (task.getStatus() != 1) {
            throw new BusinessException(ResultCode.TASK_ALREADY_COMPLETED);
        }

        // 更新任务计数
        task.setCompletedTomatoNum(task.getCompletedTomatoNum() + 1);
        task.setUpdateTime(LocalDateTime.now());

        // 检查是否完成
        if (task.getCompletedTomatoNum() >= task.getTotalTomatoNum()) {
            task.setStatus((byte) 2); // 已完成
            log.info("任务已完成: taskId={}, userId={}", taskId, userId);
        }

        // 创建番茄记录
        TomatoRecord record = new TomatoRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setTaskName(task.getTaskName());
        record.setTaskMode(task.getTaskType().intValue());
        record.setActualStartTime(LocalDateTime.now());
        record.setActualEndTime(LocalDateTime.now());
        record.setActualDuration(actualDuration);
        record.setIsEarlyEnded(false);
        record.setCreateTime(LocalDateTime.now());

        recordMapper.insert(record);
        taskMapper.updateById(task);

        log.info("完成番茄成功: taskId={}, userId={}, duration={}", taskId, userId, actualDuration);
        return true;
    }

    @Override
    public void checkTodayReset(Long userId) {
        // 检查长期任务是否需要每日重置
        // 可以添加最后重置时间的字段来判断
        // 这里简化处理，实际应用中可以根据需求优化
        log.debug("检查今日重置: userId={}", userId);
    }

    @Override
    @Transactional
    public boolean restoreCancelledTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (task.getStatus() != 3) {
            throw new BusinessException("只能恢复已取消的任务");
        }

        task.setStatus((byte) 1); // 恢复为未完成状态
        task.setUpdateTime(LocalDateTime.now());

        int result = taskMapper.updateById(task);

        log.info("恢复任务成功: taskId={}, userId={}", taskId, userId);
        return result > 0;
    }

    @Override
    @Deprecated
    public List<Task> getTomatoTaskListByUserId(Integer userId) {
        return taskMapper.selectByUserId(userId.longValue());
    }
}
