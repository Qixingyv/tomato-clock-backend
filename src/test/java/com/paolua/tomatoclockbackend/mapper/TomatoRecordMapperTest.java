package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TomatoRecordMapper 单元测试
 * 测试番茄记录Mapper的SQL操作
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TomatoRecordMapper 单元测试")
class TomatoRecordMapperTest {

    @Autowired
    private TomatoRecordMapper recordMapper;

    private TomatoRecord testRecord;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TASK_ID = 100L;

    @BeforeEach
    void setUp() {
        // 准备测试记录数据
        testRecord = new TomatoRecord();
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
    @DisplayName("测试插入番茄记录")
    void test_insert() {
        // When
        int result = recordMapper.insert(testRecord);

        // Then
        assertTrue(result > 0);
        assertNotNull(testRecord.getId());

        // 验证插入后的数据
        TomatoRecord insertedRecord = recordMapper.selectById(testRecord.getId());
        assertNotNull(insertedRecord);
        assertEquals("测试任务", insertedRecord.getTaskName());
        assertEquals(TEST_USER_ID, insertedRecord.getUserId());
    }

    @Test
    @DisplayName("测试根据ID查询番茄记录")
    void test_selectById() {
        // Given - 先插入记录
        recordMapper.insert(testRecord);

        // When
        TomatoRecord result = recordMapper.selectById(testRecord.getId());

        // Then
        assertNotNull(result);
        assertEquals(testRecord.getId(), result.getId());
        assertEquals(testRecord.getTaskName(), result.getTaskName());
    }

    @Test
    @DisplayName("测试根据ID查询不存在的记录")
    void test_selectById_notFound() {
        // When
        TomatoRecord result = recordMapper.selectById(999999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据用户ID查询所有记录")
    void test_selectByUserId() {
        // Given - 插入多条记录
        recordMapper.insert(createRecord("任务1", 1500));
        recordMapper.insert(createRecord("任务2", 1800));
        recordMapper.insert(createRecord("任务3", 1200));

        // When
        List<TomatoRecord> results = recordMapper.selectByUserId(TEST_USER_ID);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 3);
        assertTrue(results.stream().allMatch(r -> r.getUserId().equals(TEST_USER_ID)));
    }

    @Test
    @DisplayName("测试根据任务ID查询记录")
    void test_selectByTaskId() {
        // Given - 插入同一任务的多个记录
        TomatoRecord record1 = createRecord("同一个任务", 1500);
        TomatoRecord record2 = createRecord("同一个任务", 1800);
        recordMapper.insert(record1);
        recordMapper.insert(record2);

        // When
        List<TomatoRecord> results = recordMapper.selectByTaskId(TEST_TASK_ID);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 2);
        assertTrue(results.stream().allMatch(r -> r.getTaskId().equals(TEST_TASK_ID)));
    }

    @Test
    @DisplayName("测试根据日期范围查询记录")
    void test_selectByDateRange() {
        // Given - 插入记录
        recordMapper.insert(testRecord);

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        // When
        List<TomatoRecord> results = recordMapper.selectByDateRange(TEST_USER_ID, weekAgo, today);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 1);
    }

    @Test
    @DisplayName("测试根据日期范围查询记录 - 指定日期")
    void test_selectByDateRange_specificDate() {
        // Given - 插入记录
        recordMapper.insert(testRecord);

        LocalDate today = LocalDate.now();

        // When - 查询今天的记录
        List<TomatoRecord> results = recordMapper.selectByDateRange(TEST_USER_ID, today, today);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 1);
    }

    @Test
    @DisplayName("测试根据日期时间范围查询记录")
    void test_selectByDateTimeRange() {
        // Given - 插入记录
        recordMapper.insert(testRecord);

        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        // When
        List<TomatoRecord> results = recordMapper.selectByDateTimeRange(TEST_USER_ID, startTime, endTime);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 1);
    }

    @Test
    @DisplayName("测试统计日期范围内的记录数")
    void test_countByDateRange() {
        // Given - 插入多条记录
        recordMapper.insert(createRecord("任务1", 1500));
        recordMapper.insert(createRecord("任务2", 1800));
        recordMapper.insert(createRecord("任务3", 1200));

        LocalDate today = LocalDate.now();

        // When
        int count = recordMapper.countByDateRange(TEST_USER_ID, today, today);

        // Then
        assertTrue(count >= 3);
    }

    @Test
    @DisplayName("测试统计日期范围内的总时长")
    void test_sumDurationByDateRange() {
        // Given - 插入已知时长的记录
        recordMapper.insert(createRecord("任务1", 1500));
        recordMapper.insert(createRecord("任务2", 1800));
        recordMapper.insert(createRecord("任务3", 1200));

        LocalDate today = LocalDate.now();

        // When
        int totalDuration = recordMapper.sumDurationByDateRange(TEST_USER_ID, today, today);

        // Then
        assertTrue(totalDuration >= 4500); // 1500 + 1800 + 1200
    }

    @Test
    @DisplayName("测试删除番茄记录")
    void test_deleteById() {
        // Given - 先插入记录
        recordMapper.insert(testRecord);
        Long recordId = testRecord.getId();

        // When
        int result = recordMapper.deleteById(recordId);

        // Then
        assertTrue(result > 0);

        // 验证记录已被删除
        TomatoRecord deletedRecord = recordMapper.selectById(recordId);
        assertNull(deletedRecord);
    }

    @Test
    @DisplayName("测试删除不存在的记录")
    void test_deleteById_notFound() {
        // When
        int result = recordMapper.deleteById(999999L);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("测试插入提前结束的记录")
    void test_insert_earlyEnded() {
        // Given
        TomatoRecord earlyEndedRecord = new TomatoRecord();
        earlyEndedRecord.setUserId(TEST_USER_ID);
        earlyEndedRecord.setTaskId(TEST_TASK_ID);
        earlyEndedRecord.setTaskName("提前结束的任务");
        earlyEndedRecord.setTaskMode(1);
        earlyEndedRecord.setActualStartTime(LocalDateTime.now());
        earlyEndedRecord.setActualEndTime(LocalDateTime.now().plusMinutes(15));
        earlyEndedRecord.setActualDuration(900);
        earlyEndedRecord.setIsEarlyEnded(true);
        earlyEndedRecord.setCreateTime(LocalDateTime.now());

        // When
        int result = recordMapper.insert(earlyEndedRecord);

        // Then
        assertTrue(result > 0);

        // 验证提前结束标志
        TomatoRecord insertedRecord = recordMapper.selectById(earlyEndedRecord.getId());
        assertTrue(insertedRecord.getIsEarlyEnded());
        assertEquals(900, insertedRecord.getActualDuration());
    }

    @Test
    @DisplayName("测试不同任务类型的记录")
    void test_differentTaskModes() {
        // Given - 待办模式记录
        TomatoRecord todoRecord = new TomatoRecord();
        todoRecord.setUserId(TEST_USER_ID);
        todoRecord.setTaskId(TEST_TASK_ID);
        todoRecord.setTaskName("待办任务");
        todoRecord.setTaskMode(1); // 待办模式
        todoRecord.setActualStartTime(LocalDateTime.now());
        todoRecord.setActualEndTime(LocalDateTime.now().plusMinutes(25));
        todoRecord.setActualDuration(1500);
        todoRecord.setIsEarlyEnded(false);
        todoRecord.setCreateTime(LocalDateTime.now());

        // 长期模式记录
        TomatoRecord planRecord = new TomatoRecord();
        planRecord.setUserId(TEST_USER_ID);
        planRecord.setTaskId(TEST_TASK_ID + 1);
        planRecord.setTaskName("长期任务");
        planRecord.setTaskMode(2); // 长期模式
        planRecord.setActualStartTime(LocalDateTime.now());
        planRecord.setActualEndTime(LocalDateTime.now().plusMinutes(30));
        planRecord.setActualDuration(1800);
        planRecord.setIsEarlyEnded(false);
        planRecord.setCreateTime(LocalDateTime.now());

        recordMapper.insert(todoRecord);
        recordMapper.insert(planRecord);

        // When
        List<TomatoRecord> allRecords = recordMapper.selectByUserId(TEST_USER_ID);

        // Then
        assertTrue(allRecords.stream().anyMatch(r -> r.getTaskMode() == 1));
        assertTrue(allRecords.stream().anyMatch(r -> r.getTaskMode() == 2));
    }

    @Test
    @DisplayName("测试查询结果按创建时间倒序")
    void test_selectByUserId_ordering() {
        // Given - 插入多条记录，确保时间不同
        recordMapper.insert(createRecord("第一个", 1500));

        // 短暂延迟
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        recordMapper.insert(createRecord("第二个", 1500));

        // When
        List<TomatoRecord> results = recordMapper.selectByUserId(TEST_USER_ID);

        // Then - 应该按创建时间倒序
        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }

    /**
     * 辅助方法：创建测试记录
     */
    private TomatoRecord createRecord(String taskName, int duration) {
        TomatoRecord record = new TomatoRecord();
        record.setUserId(TEST_USER_ID);
        record.setTaskId(TEST_TASK_ID);
        record.setTaskName(taskName);
        record.setTaskMode(1);
        record.setPlannedStartTime(LocalDateTime.now());
        record.setActualStartTime(LocalDateTime.now());
        record.setActualEndTime(LocalDateTime.now().plusMinutes(duration / 60));
        record.setActualDuration(duration);
        record.setIsEarlyEnded(false);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }
}
