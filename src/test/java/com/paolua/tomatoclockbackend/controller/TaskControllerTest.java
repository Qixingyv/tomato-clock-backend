package com.paolua.tomatoclockbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import com.paolua.tomatoclockbackend.dto.TaskCreateRequest;
import com.paolua.tomatoclockbackend.dto.TaskDTO;
import com.paolua.tomatoclockbackend.dto.TaskUpdateRequest;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskController 单元测试
 * 测试任务控制器的HTTP接口
 */
@WebMvcTest(TaskController.class)
@DisplayName("TaskController 单元测试")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtUtil jwtUtil;

    private static final String TEST_TOKEN = "Bearer test_jwt_token";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TASK_ID = 100L;

    @BeforeEach
    void setUp() {
        // Mock JWT工具类行为
        when(jwtUtil.getUserIdFromToken("test_jwt_token")).thenReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取任务列表")
    void test_getTasks() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(
                createMockTask(1L, "任务1", (byte) 1),
                createMockTask(2L, "任务2", (byte) 2)
        );
        when(taskService.getTasks(TEST_USER_ID)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(taskService).getTasks(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试获取任务列表 - 带状态过滤")
    void test_getTasks_withStatus() throws Exception {
        // Given
        List<Task> tasks = Collections.singletonList(
                createMockTask(1L, "进行中任务", (byte) 1)
        );
        when(taskService.getTasksByStatus(TEST_USER_ID, (byte) 1)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", TEST_TOKEN)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(taskService).getTasksByStatus(TEST_USER_ID, (byte) 1);
    }

    @Test
    @DisplayName("测试获取任务列表 - 带类型过滤")
    void test_getTasks_withType() throws Exception {
        // Given
        List<Task> tasks = Collections.singletonList(
                createMockTask(1L, "待办任务", (byte) 1)
        );
        when(taskService.getTasksByTaskType(TEST_USER_ID, (byte) 1)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", TEST_TOKEN)
                        .param("taskType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).getTasksByTaskType(TEST_USER_ID, (byte) 1);
    }

    @Test
    @DisplayName("测试获取单个任务")
    void test_getTask() throws Exception {
        // Given
        Task task = createMockTask(TEST_TASK_ID, "测试任务", (byte) 1);
        when(taskService.getTaskById(TEST_TASK_ID)).thenReturn(task);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_TASK_ID))
                .andExpect(jsonPath("$.data.taskName").value("测试任务"));

        verify(taskService).getTaskById(TEST_TASK_ID);
    }

    @Test
    @DisplayName("测试获取其他用户的任务 - 返回403")
    void test_getTask_forbidden() throws Exception {
        // Given
        Task otherUserTask = new Task();
        otherUserTask.setId(TEST_TASK_ID);
        otherUserTask.setUserId(999L); // 不同用户
        otherUserTask.setTaskName("其他用户的任务");

        when(taskService.getTaskById(TEST_TASK_ID)).thenReturn(otherUserTask);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("测试创建任务")
    void test_createTask() throws Exception {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName("新任务");
        request.setTaskType(1);
        request.setTomatoDuration(25);
        request.setRestDuration(5);
        request.setTotalTomatoNum(5);

        Task createdTask = createMockTask(TEST_TASK_ID, "新任务", (byte) 1);
        when(taskService.createTask(any(TaskCreateRequest.class), eq(TEST_USER_ID)))
                .thenReturn(createdTask);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_TASK_ID))
                .andExpect(jsonPath("$.data.taskName").value("新任务"));

        verify(taskService).createTask(any(TaskCreateRequest.class), eq(TEST_USER_ID));
    }

    @Test
    @DisplayName("测试创建任务 - 参数校验失败")
    void test_createTask_validationFailed() throws Exception {
        // Given - 缺少必填字段
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName(""); // 空任务名

        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(), anyLong());
    }

    @Test
    @DisplayName("测试更新任务")
    void test_updateTask() throws Exception {
        // Given
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTaskName("更新后的任务名");
        request.setTomatoDuration(30);

        when(taskService.updateTask(any(TaskUpdateRequest.class), eq(TEST_USER_ID)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<TaskUpdateRequest> requestCaptor = ArgumentCaptor.forClass(TaskUpdateRequest.class);
        verify(taskService).updateTask(requestCaptor.capture(), eq(TEST_USER_ID));

        TaskUpdateRequest capturedRequest = requestCaptor.getValue();
        assertEquals(TEST_TASK_ID, capturedRequest.getId());
    }

    @Test
    @DisplayName("测试更新任务失败")
    void test_updateTask_failure() throws Exception {
        // Given
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTaskName("更新任务");

        when(taskService.updateTask(any(TaskUpdateRequest.class), eq(TEST_USER_ID)))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试删除任务")
    void test_deleteTask() throws Exception {
        // Given
        when(taskService.deleteTask(TEST_TASK_ID, TEST_USER_ID)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).deleteTask(TEST_TASK_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("测试删除任务失败")
    void test_deleteTask_failure() throws Exception {
        // Given
        when(taskService.deleteTask(TEST_TASK_ID, TEST_USER_ID)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/" + TEST_TASK_ID)
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试完成番茄")
    void test_completeTomato() throws Exception {
        // Given
        String requestJson = "{\"actualDuration\":1500}";

        Task updatedTask = createMockTask(TEST_TASK_ID, "测试任务", (byte) 1);
        updatedTask.setCompletedTomatoNum(3);

        when(taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500))
                .thenReturn(true);
        when(taskService.getTaskById(TEST_TASK_ID)).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/" + TEST_TASK_ID + "/complete")
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500);
    }

    @Test
    @DisplayName("测试完成番茄失败")
    void test_completeTomato_failure() throws Exception {
        // Given
        String requestJson = "{\"actualDuration\":1500}";

        when(taskService.completeTomato(TEST_TASK_ID, TEST_USER_ID, 1500))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/" + TEST_TASK_ID + "/complete")
                        .header("Authorization", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试恢复已取消的任务")
    void test_restoreCancelledTask() throws Exception {
        // Given
        when(taskService.restoreCancelledTask(TEST_TASK_ID, TEST_USER_ID))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/" + TEST_TASK_ID + "/restore")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).restoreCancelledTask(TEST_TASK_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("测试恢复任务失败")
    void test_restoreCancelledTask_failure() throws Exception {
        // Given
        when(taskService.restoreCancelledTask(TEST_TASK_ID, TEST_USER_ID))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks/" + TEST_TASK_ID + "/restore")
                        .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试缺少Authorization header")
    void test_missingAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试无效的Authorization格式")
    void test_invalidAuthorizationFormat() throws Exception {
        // Given
        when(jwtUtil.getUserIdFromToken("invalid_token"))
                .thenThrow(new BusinessException(ResultCode.UNAUTHORIZED));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "invalid_token"))
                .andExpect(status().isBadRequest());
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
        task.setDeadline(taskType == 2 ? LocalDate.now().plusWeeks(2) : null);
        return task;
    }
}
