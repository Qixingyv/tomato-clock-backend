package com.paolua.tomatoclockbackend.service.impl;

import com.paolua.tomatoclockbackend.dto.StatisticsDTO;
import com.paolua.tomatoclockbackend.mapper.TaskMapper;
import com.paolua.tomatoclockbackend.mapper.TomatoRecordMapper;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import com.paolua.tomatoclockbackend.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final TaskMapper taskMapper;
    private final TomatoRecordMapper recordMapper;

    public StatisticsServiceImpl(TaskMapper taskMapper, TomatoRecordMapper recordMapper) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public StatisticsDTO getTodayStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        return getStatistics(userId, today, today, "today");
    }

    @Override
    public StatisticsDTO getWeekStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return getStatistics(userId, weekStart, today, "week");
    }

    @Override
    public StatisticsDTO getMonthStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return getStatistics(userId, monthStart, today, "month");
    }

    @Override
    public StatisticsDTO getCustomRangeStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        return getStatistics(userId, startDate, endDate, "custom");
    }

    /**
     * 获取统计数据的核心方法
     */
    private StatisticsDTO getStatistics(Long userId, LocalDate startDate, LocalDate endDate, String period) {
        // 查询番茄记录
        List<TomatoRecord> records = recordMapper.selectByDateRange(userId, startDate, endDate);

        // 计算统计数据
        long completedTomatoes = records.size();
        long focusMinutes = records.stream()
                .mapToLong(r -> r.getActualDuration() != null ? r.getActualDuration() / 60 : 0)
                .sum();

        // 查询任务统计
        List<Task> allTasks = taskMapper.selectByUserId(userId);
        long completedTasks = allTasks.stream()
                .filter(t -> t.getStatus() == 2)
                .count();
        long totalTasks = allTasks.stream()
                .filter(t -> t.getStatus() == 1 || t.getStatus() == 2)
                .count();

        int completionRate = totalTasks > 0 ? (int) (completedTasks * 100 / totalTasks) : 0;

        // 生成每日数据
        List<StatisticsDTO.DailyDataDTO> dailyData = generateDailyData(userId, startDate, endDate);

        // 生成任务排行
        List<StatisticsDTO.TaskRankingDTO> taskRanking = generateTaskRanking(records);

        StatisticsDTO stats = new StatisticsDTO();
        stats.setPeriod(period);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        stats.setCompletedTomatoes((int) completedTomatoes);
        stats.setFocusMinutes((int) focusMinutes);
        stats.setCompletedTasks((int) completedTasks);
        stats.setTotalTasks((int) totalTasks);
        stats.setCompletionRate(completionRate);
        stats.setDailyData(dailyData);
        stats.setTaskRanking(taskRanking);

        log.debug("统计数据生成成功: userId={}, period={}, tomatoes={}", userId, period, completedTomatoes);

        return stats;
    }

    /**
     * 生成每日数据
     */
    private List<StatisticsDTO.DailyDataDTO> generateDailyData(Long userId, LocalDate startDate, LocalDate endDate) {
        List<StatisticsDTO.DailyDataDTO> dailyData = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate date = current;
            List<TomatoRecord> dayRecords = recordMapper.selectByDateRange(userId, date, date);

            int dayTomatoes = dayRecords.size();
            long dayMinutes = dayRecords.stream()
                    .mapToLong(r -> r.getActualDuration() != null ? r.getActualDuration() / 60 : 0)
                    .sum();

            StatisticsDTO.DailyDataDTO dayData = new StatisticsDTO.DailyDataDTO();
            dayData.setDate(date);
            dayData.setCompletedTomatoes(dayTomatoes);
            dayData.setFocusMinutes((int) dayMinutes);

            dailyData.add(dayData);
            current = current.plusDays(1);
        }

        return dailyData;
    }

    /**
     * 生成任务排行
     */
    private List<StatisticsDTO.TaskRankingDTO> generateTaskRanking(List<TomatoRecord> records) {
        // 按任务ID分组统计
        Map<Long, List<TomatoRecord>> groupedByTask = records.stream()
                .collect(Collectors.groupingBy(TomatoRecord::getTaskId));

        List<StatisticsDTO.TaskRankingDTO> ranking = new ArrayList<>();

        for (Map.Entry<Long, List<TomatoRecord>> entry : groupedByTask.entrySet()) {
            List<TomatoRecord> taskRecords = entry.getValue();
            int completedTomatoes = taskRecords.size();
            long totalMinutes = taskRecords.stream()
                    .mapToLong(r -> r.getActualDuration() != null ? r.getActualDuration() / 60 : 0)
                    .sum();

            if (!taskRecords.isEmpty()) {
                StatisticsDTO.TaskRankingDTO rank = new StatisticsDTO.TaskRankingDTO();
                rank.setTaskId(entry.getKey());
                rank.setTaskName(taskRecords.get(0).getTaskName());
                rank.setTaskMode(taskRecords.get(0).getTaskMode());
                rank.setCompletedTomatoes(completedTomatoes);
                rank.setTotalCompletedTomatoes(completedTomatoes);

                ranking.add(rank);
            }
        }

        // 按完成番茄数降序排序
        ranking.sort((a, b) -> b.getCompletedTomatoes().compareTo(a.getCompletedTomatoes()));

        return ranking;
    }
}
