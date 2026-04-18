package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.dto.TaskCreateRequest;
import com.paolua.tomatoclockbackend.dto.TaskUpdateRequest;
import com.paolua.tomatoclockbackend.mapper.TaskMapper;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskService 单元测试
 * 测试任务服务的各项功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService 单元测试")
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TomatoRecordMapper recordMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TASK_ID = 100L;

    private Task testTask;

    @BeforeEach
    void setUp() {
        // 准备测试任务数据
        testTask = new Task();
        testTask.setId(TEST_TASK_ID);
        testTask.setUserId(TEST_USER_ID);
        testTask.setTaskName("测试任务");
        testTask.setTaskType((byte) 1);
        testTask.setTomatoDuration(25);
        testTask.setRestDuration(5);
        testTask.setTotalTomatoNum(5);
        testTask.setCompletedTomatoNum(2);
        testTask.setStatus((byte) 1);
        testTask.setCreateTime(LocalDateTime.now());
        testTask.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试创建待办任务")
    void test_createTask_todo() {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName("待办任务测试");
        request.setTaskType(1);
        request.setTomatoDuration(30);
        request.setRestDuration(10);
        request.setTotalTomatoNum(8);

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        // When
        Task result = taskService.createTask(request, TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals("待办任务测试", result.getTaskName());
        assertEquals(1, result.getTaskType());
        assertEquals(30, result.getTomatoDuration());
        assertEquals(10, result.getRestDuration());
        assertEquals(8, result.getTotalTomatoNum());
        assertEquals((byte) 1, result.getStatus());
        assertEquals(0, result.getCompletedTomatoNum());
        assertNull(result.getDeadline());

        verify(taskMapper).insert(any(Task.class));
    }

    @Test
    @DisplayName("测试创建计划任务")
    void test_createTask_plan() {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName("计划任务测试");
        request.setTaskType(2);
        request.setTomatoDuration(25);
        request.setRestDuration(5);
        request.setTotalTomatoNum(100);
        request.setDeadline(LocalDate.now().plusWeeks(4));

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        // When
        Task result = taskService.createTask(request, TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals("计划任务测试", result.getTaskName());
        assertEquals(2, result.getTaskType());
        assertEquals(100, result.getTotalTomatoNum());
        assertNotNull(result.getDeadline());

        verify(taskMapper).insert(any(Task.class));
    }

    @Test
    @DisplayName("测试创建任务失败")
    void test_createTask_failure() {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName("创建失败测试");
        request.setTaskType(1);
        request.setTomatoDuration(25);
        request.setRestDuration(5);
        request.setTotalTomatoNum(5);

        when(taskMapper.insert(any(Task.class))).thenReturn(0);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            taskService.createTask(request, TEST_USER_ID);
        });
    }

    @Test
    @DisplayName("测试更新任务")
    void test_updateTask() {
        // Given
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setId(TEST_TASK_ID);
        request.setTaskName("更新后的任务名");
        request.setTomatoDuration(40);

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // When
        boolean result = taskService.updateTask(request, TEST_USER_ID);

        // Then
        assertTrue(result);
        assertEquals("更新后的任务名", testTask.getTaskName());
        assertEquals(40, testTask.getTomatoDuration());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(taskCaptor.capture());
        assertNotNull(taskCaptor.getValue().getUpdateTime());
    }

    @Test
    @DisplayName("测试更新不存在的任务")
    void test_updateTask_notFound() {
        // Given
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setId(999L);

        when(taskMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.updateTask(request, TEST_USER_ID);
        });

        assertEquals(ResultCode.TASK_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试更新其他用户的任务")
    void test_updateTask_forbidden() {
        // Given
        Task otherUserTask = new Task();
        otherUserTask.setId(TEST_TASK_ID);
        otherUserTask.setUserId(999L); // 不同用户

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setId(TEST_TASK_ID);

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(otherUserTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.updateTask(request, TEST_USER_ID);
        });

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试删除任务")
    void test_deleteTask() {
        // Given
        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);
        when(taskMapper.softDeleteById(TEST_TASK_ID)).thenReturn(1);

        // When
        boolean result = taskService.deleteTask(TEST_TASK_ID, TEST_USER_ID);

        // Then
        assertTrue(result);
        verify(taskMapper).softDeleteById(TEST_TASK_ID);
    }

    @Test
    @DisplayName("测试删除不存在的任务")
    void test_deleteTask_notFound() {
        // Given
        when(taskMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.deleteTask(999L, TEST_USER_ID);
        });

        assertEquals(ResultCode.TASK_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试删除其他用户的任务")
    void test_deleteTask_forbidden() {
        // Given
        Task otherUserTask = new Task();
        otherUserTask.setId(TEST_TASK_ID);
        otherUserTask.setUserId(999L);

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(otherUserTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.deleteTask(TEST_TASK_ID, TEST_USER_ID);
        });

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试完成番茄")
    void test_completeTomato() {
        // Given
        Integer actualDuration = 1500; // 25分钟

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);
        when(recordMapper.insert(any(TomatoRecord.class))).thenReturn(1);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // When
        boolean result = taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, actualDuration);

        // Then
        assertTrue(result);
        assertEquals(3, testTask.getCompletedTomatoNum()); // 2 + 1

        ArgumentCaptor<TomatoRecord> recordCaptor = ArgumentCaptor.forClass(TomatoRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());

        TomatoRecord capturedRecord = recordCaptor.getValue();
        assertEquals(TEST_USER_ID, capturedRecord.getUserId());
        assertEquals(TEST_TASK_ID, capturedRecord.getTaskId());
        assertEquals("测试任务", capturedRecord.getTaskName());
        assertEquals(actualDuration, capturedRecord.getActualDuration());
        assertFalse(capturedRecord.getIsEarlyEnded());
    }

    @Test
    @DisplayName("测试完成番茄后任务自动完成")
    void test_completeTomato_taskAutoCompleted() {
        // Given - 任务已有4个完成番茄，总共5个
        testTask.setCompletedTomatoNum(4);
        testTask.setTotalTomatoNum(5);

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);
        when(recordMapper.insert(any(TomatoRecord.class))).thenReturn(1);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // When
        taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500);

        // Then
        assertEquals(5, testTask.getCompletedTomatoNum());
        assertEquals((byte) 2, testTask.getStatus()); // 已完成状态
    }

    @Test
    @DisplayName("测试完成其他用户的番茄")
    void test_completeTomato_forbidden() {
        // Given
        Task otherUserTask = new Task();
        otherUserTask.setId(TEST_TASK_ID);
        otherUserTask.setUserId(999L);

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(otherUserTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500);
        });

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试已完成任务的番茄")
    void test_completeTomato_alreadyCompleted() {
        // Given
        testTask.setStatus((byte) 2); // 已完成状态

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500);
        });

        assertEquals(ResultCode.TASK_ALREADY_COMPLETED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试恢复已取消的任务")
    void test_restoreCancelledTask() {
        // Given
        testTask.setStatus((byte) 3); // 已取消状态

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // When
        boolean result = taskService.restoreCancelledTask(TEST_TASK_ID, TEST_USER_ID);

        // Then
        assertTrue(result);
        assertEquals((byte) 1, testTask.getStatus()); // 恢复为未完成状态
        verify(taskMapper).updateById(testTask);
    }

    @Test
    @DisplayName("测试恢复不存在的任务")
    void test_restoreCancelledTask_notFound() {
        // Given
        when(taskMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.restoreCancelledTask(999L, TEST_USER_ID);
        });

        assertEquals(ResultCode.TASK_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试恢复未取消的任务")
    void test_restoreCancelledTask_invalidStatus() {
        // Given
        testTask.setStatus((byte) 1); // 进行中状态

        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.restoreCancelledTask(TEST_TASK_ID, TEST_USER_ID);
        });

        assertEquals("只能恢复已取消的任务", exception.getMessage());
    }

    @Test
    @DisplayName("测试根据状态获取任务")
    void test_getTasksByStatus() {
        // Given
        List<Task> tasks = Arrays.asList(
                createMockTask(1L, "任务1", (byte) 1),
                createMockTask(2L, "任务2", (byte) 1)
        );

        when(taskMapper.selectByUserIdAndStatus(TEST_USER_ID, (byte) 1))
                .thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByStatus(TEST_USER_ID, (byte) 1);

        // Then
        assertEquals(2, result.size());
        assertEquals("任务1", result.get(0).getTaskName());
        assertEquals("任务2", result.get(1).getTaskName());
    }

    @Test
    @DisplayName("测试根据类型获取任务")
    void test_getTasksByTaskType() {
        // Given
        List<Task> tasks = Collections.singletonList(
                createMockTask(1L, "待办任务", (byte) 1)
        );

        when(taskMapper.selectByUserIdAndTaskType(TEST_USER_ID, (byte) 1))
                .thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByTaskType(TEST_USER_ID, (byte) 1);

        // Then
        assertEquals(1, result.size());
        assertEquals("待办任务", result.get(0).getTaskName());
    }

    @Test
    @DisplayName("测试获取所有任务")
    void test_getTasks() {
        // Given
        List<Task> tasks = Arrays.asList(
                createMockTask(1L, "任务A", (byte) 1),
                createMockTask(2L, "任务B", (byte) 2)
        );

        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasks(TEST_USER_ID);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("测试根据ID获取任务")
    void test_getTaskById() {
        // Given
        when(taskMapper.selectById(TEST_TASK_ID)).thenReturn(testTask);

        // When
        Task result = taskService.getTaskById(TEST_TASK_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_TASK_ID, result.getId());
        assertEquals("测试任务", result.getTaskName());
    }

    @Test
    @DisplayName("测试根据ID获取不存在的任务")
    void test_getTaskById_notFound() {
        // Given
        when(taskMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskService.getTaskById(999L);
        });

        assertEquals(ResultCode.TASK_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试检查今日重置")
    void test_checkTodayReset() {
        // When - 该方法目前只是记录日志
        taskService.checkTodayReset(TEST_USER_ID);

        // Then - 不抛出异常即可
        // 实际实现中可能需要更详细的验证
    }

    /**
     * 辅助方法：创建模拟任务
     */
    private Task createMockTask(Long id, String name, byte taskType) {
        Task task = new Task();
        task.setId(id);
        task.setUserId(TEST_USER_ID);
        task.setTaskName(name);
        task.setTaskType(taskType);
        task.setTomatoDuration(25);
        task.setRestDuration(5);
        task.setTotalTomatoNum(5);
        task.setCompletedTomatoNum(0);
        task.setStatus((byte) 1);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }
}
