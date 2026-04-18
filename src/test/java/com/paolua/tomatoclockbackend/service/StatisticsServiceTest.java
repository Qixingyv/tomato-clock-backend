package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.dto.StatisticsDTO;
import com.paolua.tomatoclockbackend.mapper.TaskMapper;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * StatisticsService 单元测试
 * 测试统计服务的各项功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 单元测试")
class StatisticsServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TomatoRecordMapper recordMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TASK_ID = 100L;

    private List<TomatoRecord> testRecords;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        // 准备测试记录数据
        testRecords = Arrays.asList(
                createRecord(1L, "任务1", 1500, LocalDate.now()),
                createRecord(2L, "任务2", 1800, LocalDate.now()),
                createRecord(TEST_TASK_ID, "任务3", 1200, LocalDate.now().minusDays(1))
        );

        // 准备测试任务数据
        testTasks = Arrays.asList(
                createTask(1L, "已完成任务", (byte) 2),
                createTask(2L, "进行中任务", (byte) 1),
                createTask(3L, "已取消任务", (byte) 3)
        );
    }

    @Test
    @DisplayName("测试获取今日统计")
    void test_getTodayStatistics() {
        // Given
        LocalDate today = LocalDate.now();
        List<TomatoRecord> todayRecords = Arrays.asList(
                createRecord(1L, "今日任务1", 1500, today),
                createRecord(2L, "今日任务2", 1800, today)
        );

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(todayRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals("today", result.getPeriod());
        assertEquals(2, result.getCompletedTomatoes());
        assertEquals(55, result.getFocusMinutes()); // (1500 + 1800) / 60
        assertEquals(1, result.getCompletedTasks());
        assertEquals(2, result.getTotalTasks());
        assertEquals(50, result.getCompletionRate()); // 1/2 * 100
        assertNotNull(result.getDailyData());
    }

    @Test
    @DisplayName("测试获取本周统计")
    void test_getWeekStatistics() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getWeekStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals("week", result.getPeriod());
        assertEquals(weekStart, result.getStartDate());
        assertEquals(today, result.getEndDate());
        assertEquals(3, result.getCompletedTomatoes());
    }

    @Test
    @DisplayName("测试获取本月统计")
    void test_getMonthStatistics() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getMonthStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals("month", result.getPeriod());
        assertEquals(monthStart, result.getStartDate());
        assertEquals(today, result.getEndDate());
    }

    @Test
    @DisplayName("测试获取自定义范围统计")
    void test_getCustomRangeStatistics() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        when(recordMapper.selectByDateRange(TEST_USER_ID, startDate, endDate))
                .thenReturn(testRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getCustomRangeStatistics(TEST_USER_ID, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals("custom", result.getPeriod());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
    }

    @Test
    @DisplayName("测试计算完成率")
    void test_calculateCompletionRate() {
        // Given - 有2个已完成，2个进行中，1个已取消
        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then - 完成率 = 已完成 / (已完成 + 进行中) * 100 = 1/2 * 100 = 50
        assertEquals(50, result.getCompletionRate());
    }

    @Test
    @DisplayName("测试空数据统计")
    void test_emptyData() {
        // Given
        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getCompletedTomatoes());
        assertEquals(0, result.getFocusMinutes());
        assertEquals(0, result.getCompletedTasks());
        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getCompletionRate());
        assertTrue(result.getTaskRanking().isEmpty());
        assertNotNull(result.getDailyData());
    }

    @Test
    @DisplayName("测试生成任务排行")
    void test_getTaskRanking() {
        // Given - 任务1有2个记录，任务2有1个记录
        List<TomatoRecord> records = Arrays.asList(
                createRecord(1L, "热门任务", 1500, LocalDate.now()),
                createRecord(1L, "热门任务", 1800, LocalDate.now()),
                createRecord(2L, "普通任务", 1200, LocalDate.now())
        );

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result.getTaskRanking());
        assertEquals(2, result.getTaskRanking().size());

        // 应该按完成番茄数降序排列
        assertEquals(1L, result.getTaskRanking().get(0).getTaskId());
        assertEquals(2, result.getTaskRanking().get(0).getCompletedTomatoes());

        assertEquals(2L, result.getTaskRanking().get(1).getTaskId());
        assertEquals(1, result.getTaskRanking().get(1).getCompletedTomatoes());
    }

    @Test
    @DisplayName("测试生成每日数据")
    void test_generateDailyData() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<TomatoRecord> todayRecords = Arrays.asList(
                createRecord(1L, "今日任务1", 1500, today),
                createRecord(2L, "今日任务2", 1800, today)
        );

        List<TomatoRecord> yesterdayRecords = Collections.singletonList(
                createRecord(3L, "昨日任务", 1200, yesterday)
        );

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(todayRecords);
        when(recordMapper.selectByDateRange(TEST_USER_ID, today, today))
                .thenReturn(todayRecords);
        when(recordMapper.selectByDateRange(TEST_USER_ID, yesterday, yesterday))
                .thenReturn(yesterdayRecords);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When - 获取今日统计
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result.getDailyData());
        assertTrue(result.getDailyData().size() >= 1);

        // 验证今日数据
        StatisticsDTO.DailyDataDTO todayData = result.getDailyData().get(0);
        assertEquals(today, todayData.getDate());
        assertEquals(2, todayData.getCompletedTomatoes());
        assertEquals(55, todayData.getFocusMinutes()); // (1500 + 1800) / 60
    }

    @Test
    @DisplayName("测试专注时长计算")
    void test_calculateFocusMinutes() {
        // Given - 总共4500秒 = 75分钟
        List<TomatoRecord> records = Arrays.asList(
                createRecord(1L, "任务1", 1500, LocalDate.now()),
                createRecord(2L, "任务2", 1500, LocalDate.now()),
                createRecord(3L, "任务3", 1500, LocalDate.now())
        );

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertEquals(75, result.getFocusMinutes()); // 4500 / 60
    }

    @Test
    @DisplayName("测试不同任务模式的统计")
    void test_differentTaskModes() {
        // Given - 包含待办和长期模式
        List<TomatoRecord> records = Arrays.asList(
                createRecordWithMode(1L, "待办任务", 1500, LocalDate.now(), (byte) 1),
                createRecordWithMode(2L, "长期任务", 1800, LocalDate.now(), (byte) 2)
        );

        when(recordMapper.selectByDateRange(eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(taskMapper.selectByUserId(TEST_USER_ID)).thenReturn(testTasks);

        // When
        StatisticsDTO result = statisticsService.getTodayStatistics(TEST_USER_ID);

        // Then
        assertNotNull(result.getTaskRanking());
        assertTrue(result.getTaskRanking().stream().anyMatch(r -> r.getTaskMode() == 1));
        assertTrue(result.getTaskRanking().stream().anyMatch(r -> r.getTaskMode() == 2));
    }

    /**
     * 辅助方法：创建测试记录
     */
    private TomatoRecord createRecord(Long taskId, String taskName, int duration, LocalDate date) {
        TomatoRecord record = new TomatoRecord();
        record.setUserId(TEST_USER_ID);
        record.setTaskId(taskId);
        record.setTaskName(taskName);
        record.setTaskMode(1);
        record.setActualStartTime(date.atStartOfDay());
        record.setActualEndTime(date.atStartOfDay().plusMinutes(duration / 60));
        record.setActualDuration(duration);
        record.setIsEarlyEnded(false);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private TomatoRecord createRecordWithMode(Long taskId, String taskName, int duration, LocalDate date, byte mode) {
        TomatoRecord record = createRecord(taskId, taskName, duration, date);
        record.setTaskMode(mode);
        return record;
    }

    /**
     * 辅助方法：创建测试任务
     */
    private Task createTask(Long id, String name, byte status) {
        Task task = new Task();
        task.setId(id);
        task.setUserId(TEST_USER_ID);
        task.setTaskName(name);
        task.setTaskType((byte) 1);
        task.setTotalTomatoNum(5);
        task.setCompletedTomatoNum(status == 2 ? 5 : 0);
        task.setStatus(status);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }
}
