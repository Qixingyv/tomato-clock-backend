package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.TomatoRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 番茄记录Mapper接口
 * 处理番茄记录相关的数据库操作
 */
@Mapper
public interface TomatoRecordMapper {

    /**
     * 根据用户ID查询所有番茄记录
     *
     * @param userId 用户ID
     * @return 番茄记录列表
     */
    @Select("SELECT * FROM tomato_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TomatoRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据任务ID查询番茄记录
     *
     * @param taskId 任务ID
     * @return 番茄记录列表
     */
    @Select("SELECT * FROM tomato_record WHERE task_id = #{taskId} ORDER BY create_time DESC")
    List<TomatoRecord> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据日期范围查询番茄记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 番茄记录列表
     */
    @Select("SELECT * FROM tomato_record WHERE user_id = #{userId} " +
            "AND DATE(create_time) BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY create_time DESC")
    List<TomatoRecord> selectByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据日期时间范围查询番茄记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 番茄记录列表
     */
    @Select("SELECT * FROM tomato_record WHERE user_id = #{userId} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time DESC")
    List<TomatoRecord> selectByDateTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据记录ID查询
     *
     * @param id 记录ID
     * @return 番茄记录
     */
    @Select("SELECT * FROM tomato_record WHERE id = #{id}")
    TomatoRecord selectById(@Param("id") Long id);

    /**
     * 插入番茄记录
     *
     * @param record 番茄记录
     * @return 影响行数
     */
    @Insert("INSERT INTO tomato_record (user_id, task_id, task_name, task_mode, " +
            "planned_start_time, actual_start_time, actual_end_time, actual_duration, is_early_ended, create_time) " +
            "VALUES (#{userId}, #{taskId}, #{taskName}, #{taskMode}, " +
            "#{plannedStartTime}, #{actualStartTime}, #{actualEndTime}, #{actualDuration}, #{isEarlyEnded}, NOW())")
    int insert(TomatoRecord record);

    /**
     * 删除番茄记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    @Delete("DELETE FROM tomato_record WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户在指定日期范围内的番茄记录数
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM tomato_record WHERE user_id = #{userId} " +
            "AND DATE(create_time) BETWEEN #{startDate} AND #{endDate}")
    int countByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计用户在指定日期范围内的总专注时长（秒）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总时长（秒）
     */
    @Select("SELECT COALESCE(SUM(actual_duration), 0) FROM tomato_record WHERE user_id = #{userId} " +
            "AND DATE(create_time) BETWEEN #{startDate} AND #{endDate}")
    int sumDurationByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
