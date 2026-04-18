package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.impl.RecordServiceImpl;
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
 * RecordService 单元测试
 * 测试番茄记录服务的各项功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecordService 单元测试")
class RecordServiceTest {

    @Mock
    private TomatoRecordMapper recordMapper;

    @InjectMocks
    private RecordServiceImpl recordService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TASK_ID = 100L;
    private static final Long TEST_RECORD_ID = 1000L;

    private TomatoRecord testRecord;

    @BeforeEach
    void setUp() {
        // 准备测试记录数据
        testRecord = new TomatoRecord();
        testRecord.setId(TEST_RECORD_ID);
        testRecord.setUserId(TEST_USER_ID);
        testRecord.setTaskId(TEST_TASK_ID);
        testRecord.setTaskName("测试任务");
        testRecord.setTaskMode(1);
        testRecord.setPlannedStartTime(LocalDateTime.now());
        testRecord.setActualStartTime(LocalDateTime.now());
        testRecord.setActualEndTime(LocalDateTime.now().plusMinutes(25));
        testRecord.setActualDuration(1500);
        testRecord.setIsEarlyEnded(false);
        testRecord.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试根据用户ID获取记录")
    void test_getRecordsByUserId() {
        // Given
        List<TomatoRecord> records = Arrays.asList(
                createMockRecord(1L, "记录1"),
                createMockRecord(2L, "记录2")
        );

        when(recordMapper.selectByUserId(TEST_USER_ID)).thenReturn(records);

        // When
        List<TomatoRecord> result = recordService.getRecordsByUserId(TEST_USER_ID);

        // Then
        assertEquals(2, result.size());
        assertEquals("记录1", result.get(0).getTaskName());
        assertEquals("记录2", result.get(1).getTaskName());

        verify(recordMapper).selectByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试根据用户ID获取记录 - 返回空列表")
    void test_getRecordsByUserId_empty() {
        // Given
        when(recordMapper.selectByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // When
        List<TomatoRecord> result = recordService.getRecordsByUserId(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试根据任务ID获取记录")
    void test_getRecordsByTaskId() {
        // Given
        List<TomatoRecord> records = Arrays.asList(
                createMockRecord(1L, "番茄1"),
                createMockRecord(2L, "番茄2"),
                createMockRecord(3L, "番茄3")
        );

        when(recordMapper.selectByTaskId(TEST_TASK_ID)).thenReturn(records);

        // When
        List<TomatoRecord> result = recordService.getRecordsByTaskId(TEST_TASK_ID);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(r -> r.getTaskId().equals(TEST_TASK_ID)));

        verify(recordMapper).selectByTaskId(TEST_TASK_ID);
    }

    @Test
    @DisplayName("测试根据日期范围获取记录")
    void test_getRecordsByDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<TomatoRecord> records = Arrays.asList(
                createMockRecord(1L, "本周记录1"),
                createMockRecord(2L, "本周记录2")
        );

        when(recordMapper.selectByDateRange(TEST_USER_ID, startDate, endDate))
                .thenReturn(records);

        // When
        List<TomatoRecord> result = recordService.getRecordsByDateRange(TEST_USER_ID, startDate, endDate);

        // Then
        assertEquals(2, result.size());

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(recordMapper).selectByDateRange(eq(TEST_USER_ID), startCaptor.capture(), endCaptor.capture());

        assertEquals(startDate, startCaptor.getValue());
        assertEquals(endDate, endCaptor.getValue());
    }

    @Test
    @DisplayName("测试根据日期时间范围获取记录")
    void test_getRecordsByDateTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        List<TomatoRecord> records = Collections.singletonList(
                createMockRecord(1L, "24小时内记录")
        );

        when(recordMapper.selectByDateTimeRange(TEST_USER_ID, startTime, endTime))
                .thenReturn(records);

        // When
        List<TomatoRecord> result = recordService.getRecordsByDateTimeRange(TEST_USER_ID, startTime, endTime);

        // Then
        assertEquals(1, result.size());

        verify(recordMapper).selectByDateTimeRange(TEST_USER_ID, startTime, endTime);
    }

    @Test
    @DisplayName("测试根据ID获取记录")
    void test_getRecordById() {
        // Given
        when(recordMapper.selectById(TEST_RECORD_ID)).thenReturn(testRecord);

        // When
        TomatoRecord result = recordService.getRecordById(TEST_RECORD_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        assertEquals("测试任务", result.getTaskName());

        verify(recordMapper).selectById(TEST_RECORD_ID);
    }

    @Test
    @DisplayName("测试根据ID获取不存在的记录")
    void test_getRecordById_notFound() {
        // Given
        when(recordMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            recordService.getRecordById(999L);
        });

        assertEquals(ResultCode.TOMATO_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("测试创建番茄记录")
    void test_createRecord() {
        // Given
        TomatoRecord newRecord = new TomatoRecord();
        newRecord.setUserId(TEST_USER_ID);
        newRecord.setTaskId(TEST_TASK_ID);
        newRecord.setTaskName("新完成的番茄");
        newRecord.setTaskMode(1);
        newRecord.setActualStartTime(LocalDateTime.now());
        newRecord.setActualEndTime(LocalDateTime.now().plusMinutes(25));
        newRecord.setActualDuration(1500);
        newRecord.setIsEarlyEnded(false);

        when(recordMapper.insert(any(TomatoRecord.class))).thenReturn(1);

        // When
        TomatoRecord result = recordService.createRecord(newRecord);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreateTime());

        ArgumentCaptor<TomatoRecord> recordCaptor = ArgumentCaptor.forClass(TomatoRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());

        TomatoRecord capturedRecord = recordCaptor.getValue();
        assertEquals("新完成的番茄", capturedRecord.getTaskName());
        assertNotNull(capturedRecord.getCreateTime());
    }

    @Test
    @DisplayName("测试创建番茄记录失败")
    void test_createRecord_failure() {
        // Given
        when(recordMapper.insert(any(TomatoRecord.class))).thenReturn(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            recordService.createRecord(testRecord);
        });

        assertEquals("创建番茄记录失败", exception.getMessage());
    }

    @Test
    @DisplayName("测试删除番茄记录")
    void test_deleteRecord() {
        // Given
        when(recordMapper.deleteById(TEST_RECORD_ID)).thenReturn(1);

        // When
        boolean result = recordService.deleteRecord(TEST_RECORD_ID);

        // Then
        assertTrue(result);
        verify(recordMapper).deleteById(TEST_RECORD_ID);
    }

    @Test
    @DisplayName("测试删除不存在的番茄记录")
    void test_deleteRecord_notFound() {
        // Given
        when(recordMapper.deleteById(999L)).thenReturn(0);

        // When
        boolean result = recordService.deleteRecord(999L);

        // Then
        assertFalse(result);
        verify(recordMapper).deleteById(999L);
    }

    @Test
    @DisplayName("测试创建提前结束的番茄记录")
    void test_createRecord_earlyEnded() {
        // Given
        TomatoRecord earlyEndedRecord = new TomatoRecord();
        earlyEndedRecord.setUserId(TEST_USER_ID);
        earlyEndedRecord.setTaskId(TEST_TASK_ID);
        earlyEndedRecord.setTaskName("提前结束的番茄");
        earlyEndedRecord.setTaskMode(1);
        earlyEndedRecord.setActualDuration(900); // 只完成了15分钟
        earlyEndedRecord.setIsEarlyEnded(true);

        when(recordMapper.insert(any(TomatoRecord.class))).thenReturn(1);

        // When
        TomatoRecord result = recordService.createRecord(earlyEndedRecord);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsEarlyEnded());
        assertEquals(900, result.getActualDuration());

        ArgumentCaptor<TomatoRecord> recordCaptor = ArgumentCaptor.forClass(TomatoRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());

        assertTrue(recordCaptor.getValue().getIsEarlyEnded());
    }

    @Test
    @DisplayName("测试获取今日记录")
    void test_getTodayRecords() {
        // Given
        LocalDate today = LocalDate.now();
        List<TomatoRecord> todayRecords = Arrays.asList(
                createMockRecord(1L, "今天第一个番茄"),
                createMockRecord(2L, "今天第二个番茄")
        );

        when(recordMapper.selectByDateRange(TEST_USER_ID, today, today))
                .thenReturn(todayRecords);

        // When
        List<TomatoRecord> result = recordService.getRecordsByDateRange(TEST_USER_ID, today, today);

        // Then
        assertEquals(2, result.size());
        verify(recordMapper).selectByDateRange(TEST_USER_ID, today, today);
    }

    @Test
    @DisplayName("测试获取本周记录")
    void test_getWeekRecords() {
        // Given
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now();

        List<TomatoRecord> weekRecords = Collections.singletonList(
                createMockRecord(1L, "本周番茄记录")
        );

        when(recordMapper.selectByDateRange(TEST_USER_ID, weekStart, weekEnd))
                .thenReturn(weekRecords);

        // When
        List<TomatoRecord> result = recordService.getRecordsByDateRange(TEST_USER_ID, weekStart, weekEnd);

        // Then
        assertEquals(1, result.size());
    }

    /**
     * 辅助方法：创建模拟番茄记录
     */
    private TomatoRecord createMockRecord(Long id, String taskName) {
        TomatoRecord record = new TomatoRecord();
        record.setId(id);
        record.setUserId(TEST_USER_ID);
        record.setTaskId(TEST_TASK_ID);
        record.setTaskName(taskName);
        record.setTaskMode(1);
        record.setPlannedStartTime(LocalDateTime.now());
        record.setActualStartTime(LocalDateTime.now());
        record.setActualEndTime(LocalDateTime.now().plusMinutes(25));
        record.setActualDuration(1500);
        record.setIsEarlyEnded(false);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }
}
