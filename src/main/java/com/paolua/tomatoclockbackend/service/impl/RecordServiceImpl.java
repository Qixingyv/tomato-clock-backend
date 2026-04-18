package com.paolua.tomatoclockbackend.service.impl;

import com.paolua.tomatoclockbackend.common.exception.BusinessException;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 番茄记录服务实现类
 */
@Service
@Slf4j
public class RecordServiceImpl implements RecordService {

    private final TomatoRecordMapper recordMapper;

    public RecordServiceImpl(TomatoRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public List<TomatoRecord> getRecordsByUserId(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    @Override
    public List<TomatoRecord> getRecordsByTaskId(Long taskId) {
        return recordMapper.selectByTaskId(taskId);
    }

    @Override
    public List<TomatoRecord> getRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return recordMapper.selectByDateRange(userId, startDate, endDate);
    }

    @Override
    public List<TomatoRecord> getRecordsByDateTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return recordMapper.selectByDateTimeRange(userId, startTime, endTime);
    }

    @Override
    public TomatoRecord getRecordById(Long id) {
        TomatoRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.TOMATO_NOT_FOUND);
        }
        return record;
    }

    @Override
    public TomatoRecord createRecord(TomatoRecord record) {
        record.setCreateTime(LocalDateTime.now());
        int result = recordMapper.insert(record);
        if (result <= 0) {
            throw new BusinessException("创建番茄记录失败");
        }
        log.info("创建番茄记录成功: recordId={}, userId={}", record.getId(), record.getUserId());
        return record;
    }

    @Override
    public boolean deleteRecord(Long id) {
        int result = recordMapper.deleteById(id);
        if (result > 0) {
            log.info("删除番茄记录成功: recordId={}", id);
            return true;
        }
        return false;
    }
}
