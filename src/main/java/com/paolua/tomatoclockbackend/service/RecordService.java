package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.pojo.TomatoRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 番茄记录服务接口
 */
public interface RecordService {

    /**
     * 根据用户ID获取番茄记录列表
     *
     * @param userId 用户ID
     * @return 番茄记录列表
     */
    List<TomatoRecord> getRecordsByUserId(Long userId);

    /**
     * 根据任务ID获取番茄记录列表
     *
     * @param taskId 任务ID
     * @return 番茄记录列表
     */
    List<TomatoRecord> getRecordsByTaskId(Long taskId);

    /**
     * 根据日期范围获取番茄记录列表
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 番茄记录列表
     */
    List<TomatoRecord> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期时间范围获取番茄记录列表
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 番茄记录列表
     */
    List<TomatoRecord> getRecordsByDateTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID获取番茄记录
     *
     * @param id 记录ID
     * @return 番茄记录
     */
    TomatoRecord getRecordById(Long id);

    /**
     * 创建番茄记录
     *
     * @param record 番茄记录
     * @return 创建的记录
     */
    TomatoRecord createRecord(TomatoRecord record);

    /**
     * 删除番茄记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteRecord(Long id);
}
