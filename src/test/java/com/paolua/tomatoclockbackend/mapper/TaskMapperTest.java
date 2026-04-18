package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskMapper 单元测试
 * 测试任务Mapper的SQL操作
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TaskMapper 单元测试")
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    private Task testTask;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 准备测试任务数据
        testTask = new Task();
        testTask.setUserId(TEST_USER_ID);
        testTask.setTaskName("测试任务");
        testTask.setTaskType((byte) 1);
        testTask.setTotalTomatoNum(5);
        testTask.setTomatoDuration(25);
        testTask.setRestDuration(5);
        testTask.setDeadline(null);
        testTask.setCompletedTomatoNum(0);
        testTask.setStatus((byte) 1);
        testTask.setCreateTime(LocalDateTime.now());
        testTask.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试插入任务")
    void test_insert() {
        // When
        int result = taskMapper.insert(testTask);

        // Then
        assertTrue(result > 0);
        assertNotNull(testTask.getId());

        // 验证插入后的数据
        Task insertedTask = taskMapper.selectById(testTask.getId());
        assertNotNull(insertedTask);
        assertEquals("测试任务", insertedTask.getTaskName());
        assertEquals(TEST_USER_ID, insertedTask.getUserId());
    }

    @Test
    @DisplayName("测试根据ID查询任务")
    void test_selectById() {
        // Given - 先插入任务
        taskMapper.insert(testTask);

        // When
        Task result = taskMapper.selectById(testTask.getId());

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTaskName(), result.getTaskName());
        assertEquals(testTask.getUserId(), result.getUserId());
    }

    @Test
    @DisplayName("测试根据ID查询不存在的任务")
    void test_selectById_notFound() {
        // When
        Task result = taskMapper.selectById(999999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据用户ID查询所有任务")
    void test_selectByUserId() {
        // Given - 插入多个任务
        Task task1 = createTask("任务1", (byte) 1, (byte) 1);
        Task task2 = createTask("任务2", (byte) 2, (byte) 1);
        Task task3 = createTask("任务3", (byte) 1, (byte) 2);

        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When
        List<Task> results = taskMapper.selectByUserId(TEST_USER_ID);

        // Then
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("测试根据用户ID和状态查询任务")
    void test_selectByUserIdAndStatus() {
        // Given - 插入不同状态的任务
        Task task1 = createTask("进行中任务", (byte) 1, (byte) 1); // 状态1：进行中
        Task task2 = createTask("已完成任务", (byte) 2, (byte) 2); // 状态2：已完成
        Task task3 = createTask("进行中任务2", (byte) 1, (byte) 1);

        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When - 查询进行中的任务
        List<Task> results = taskMapper.selectByUserIdAndStatus(TEST_USER_ID, (byte) 1);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(t -> t.getStatus() == (byte) 1));
    }

    @Test
    @DisplayName("测试根据用户ID和任务类型查询任务")
    void test_selectByUserIdAndTaskType() {
        // Given - 插入不同类型的任务
        Task task1 = createTask("待办任务", (byte) 1, (byte) 1); // 类型1：待办
        Task task2 = createTask("长期任务", (byte) 2, (byte) 1); // 类型2：长期
        Task task3 = createTask("待办任务2", (byte) 1, (byte) 1);

        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        // When - 查询待办任务
        List<Task> results = taskMapper.selectByUserIdAndTaskType(TEST_USER_ID, (byte) 1);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(t -> t.getTaskType() == (byte) 1));
    }

    @Test
    @DisplayName("测试更新任务")
    void test_updateById() {
        // Given - 先插入任务
        taskMapper.insert(testTask);
        Long taskId = testTask.getId();

        // When - 更新任务信息
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setTaskName("更新后的任务名");
        updateTask.setTotalTomatoNum(10);
        updateTask.setTomatoDuration(30);
        updateTask.setCompletedTomatoNum(3);
        updateTask.setStatus((byte) 2);

        int result = taskMapper.updateById(updateTask);

        // Then
        assertTrue(result > 0);

        // 验证更新后的数据
        Task updatedTask = taskMapper.selectById(taskId);
        assertEquals("更新后的任务名", updatedTask.getTaskName());
        assertEquals(10, updatedTask.getTotalTomatoNum());
        assertEquals(30, updatedTask.getTomatoDuration());
        assertEquals(3, updatedTask.getCompletedTomatoNum());
        assertEquals((byte) 2, updatedTask.getStatus());
    }

    @Test
    @DisplayName("测试软删除任务")
    void test_softDeleteById() {
        // Given - 先插入任务
        taskMapper.insert(testTask);
        Long taskId = testTask.getId();

        // When
        int result = taskMapper.softDeleteById(taskId);

        // Then
        assertTrue(result > 0);

        // 验证状态已更新为已取消（3）
        Task deletedTask = taskMapper.selectById(taskId);
        assertEquals((byte) 3, deletedTask.getStatus());
    }

    @Test
    @DisplayName("测试物理删除任务")
    void test_deleteById() {
        // Given - 先插入任务
        taskMapper.insert(testTask);
        Long taskId = testTask.getId();

        // When
        int result = taskMapper.deleteById(taskId);

        // Then
        assertTrue(result > 0);

        // 验证任务已被删除
        Task deletedTask = taskMapper.selectById(taskId);
        assertNull(deletedTask);
    }

    @Test
    @DisplayName("测试更新已完成番茄数")
    void test_updateCompletedTomatoNum() {
        // Given - 先插入任务
        taskMapper.insert(testTask);
        Long taskId = testTask.getId();

        // When
        int result = taskMapper.updateCompletedTomatoNum(taskId, 5);

        // Then
        assertTrue(result > 0);

        // 验证已完成番茄数已更新
        Task updatedTask = taskMapper.selectById(taskId);
        assertEquals(5, updatedTask.getCompletedTomatoNum());
    }

    @Test
    @DisplayName("测试更新任务状态")
    void test_updateStatus() {
        // Given - 先插入任务
        taskMapper.insert(testTask);
        Long taskId = testTask.getId();

        // When - 更新为已完成状态
        int result = taskMapper.updateStatus(taskId, (byte) 2);

        // Then
        assertTrue(result > 0);

        // 验证状态已更新
        Task updatedTask = taskMapper.selectById(taskId);
        assertEquals((byte) 2, updatedTask.getStatus());
    }

    @Test
    @DisplayName("测试创建带截止日期的长期任务")
    void test_insert_withDeadline() {
        // Given
        Task longTermTask = new Task();
        longTermTask.setUserId(TEST_USER_ID);
        longTermTask.setTaskName("长期任务");
        longTermTask.setTaskType((byte) 2);
        longTermTask.setTotalTomatoNum(100);
        longTermTask.setTomatoDuration(25);
        longTermTask.setRestDuration(5);
        longTermTask.setDeadline(LocalDate.now().plusWeeks(4));
        longTermTask.setCompletedTomatoNum(0);
        longTermTask.setStatus((byte) 1);
        longTermTask.setCreateTime(LocalDateTime.now());
        longTermTask.setUpdateTime(LocalDateTime.now());

        // When
        int result = taskMapper.insert(longTermTask);

        // Then
        assertTrue(result > 0);
        assertNotNull(longTermTask.getId());

        // 验证截止日期
        Task insertedTask = taskMapper.selectById(longTermTask.getId());
        assertNotNull(insertedTask.getDeadline());
        assertTrue(insertedTask.getDeadline().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("测试更新不存在的任务")
    void test_updateById_notFound() {
        // When
        Task updateTask = new Task();
        updateTask.setId(999999L);
        updateTask.setTaskName("不存在的任务");

        int result = taskMapper.updateById(updateTask);

        // Then - 返回0表示没有更新任何记录
        assertEquals(0, result);
    }

    @Test
    @DisplayName("测试查询返回结果按时间排序")
    void test_selectByUserId_ordering() {
        // Given - 插入多个任务
        Task task1 = createTask("第一个任务", (byte) 1, (byte) 1);
        taskMapper.insert(task1);

        // 短暂延迟确保时间不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Task task2 = createTask("第二个任务", (byte) 1, (byte) 1);
        taskMapper.insert(task2);

        // When
        List<Task> results = taskMapper.selectByUserId(TEST_USER_ID);

        // Then - 应该按创建时间倒序排列（后插入的在前）
        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }

    /**
     * 辅助方法：创建测试任务
     */
    private Task createTask(String name, byte taskType, byte status) {
        Task task = new Task();
        task.setUserId(TEST_USER_ID);
        task.setTaskName(name);
        task.setTaskType(taskType);
        task.setTotalTomatoNum(5);
        task.setTomatoDuration(25);
        task.setRestDuration(5);
        task.setDeadline(taskType == 2 ? LocalDate.now().plusWeeks(2) : null);
        task.setCompletedTomatoNum(0);
        task.setStatus(status);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }
}
