package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务Mapper接口
 * 处理任务相关的数据库操作
 */
@Mapper
public interface TaskMapper {

    /**
     * 根据用户ID查询所有任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM task WHERE user_id = #{userId} ORDER BY status ASC, create_time DESC")
    List<Task> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询任务
     *
     * @param userId 用户ID
     * @param status 任务状态
     * @return 任务列表
     */
    @Select("SELECT * FROM task WHERE user_id = #{userId} AND status = #{status} ORDER BY create_time DESC")
    List<Task> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Byte status);

    /**
     * 根据用户ID和任务类型查询任务
     *
     * @param userId 用户ID
     * @param taskType 任务类型
     * @return 任务列表
     */
    @Select("SELECT * FROM task WHERE user_id = #{userId} AND task_type = #{taskType} ORDER BY create_time DESC")
    List<Task> selectByUserIdAndTaskType(@Param("userId") Long userId, @Param("taskType") Byte taskType);

    /**
     * 根据任务ID查询任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    @Select("SELECT * FROM task WHERE id = #{id}")
    Task selectById(@Param("id") Long id);

    /**
     * 插入任务
     *
     * @param task 任务信息
     * @return 影响行数
     */
    @Insert("INSERT INTO task (user_id, task_name, task_type, total_tomato_num, tomato_duration, rest_duration, " +
            "deadline, completed_tomato_num, status, create_time, update_time) " +
            "VALUES (#{userId}, #{taskName}, #{taskType}, #{totalTomatoNum}, #{tomatoDuration}, #{restDuration}, " +
            "#{deadline}, #{completedTomatoNum}, #{status}, NOW(), NOW())")
    int insert(Task task);

    /**
     * 更新任务
     *
     * @param task 任务信息
     * @return 影响行数
     */
    @Update("UPDATE task SET task_name = #{taskName}, task_type = #{taskType}, total_tomato_num = #{totalTomatoNum}, " +
            "tomato_duration = #{tomatoDuration}, rest_duration = #{restDuration}, deadline = #{deadline}, " +
            "completed_tomato_num = #{completedTomatoNum}, status = #{status}, update_time = NOW() " +
            "WHERE id = #{id}")
    int updateById(Task task);

    /**
     * 删除任务（软删除 - 更新状态为已取消）
     *
     * @param id 任务ID
     * @return 影响行数
     */
    @Update("UPDATE task SET status = 3, update_time = NOW() WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    /**
     * 物理删除任务
     *
     * @param id 任务ID
     * @return 影响行数
     */
    @Delete("DELETE FROM task WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新任务已完成番茄数
     *
     * @param id 任务ID
     * @param completedNum 已完成番茄数
     * @return 影响行数
     */
    @Update("UPDATE task SET completed_tomato_num = #{completedNum}, update_time = NOW() WHERE id = #{id}")
    int updateCompletedTomatoNum(@Param("id") Long id, @Param("completedNum") Integer completedNum);

    /**
     * 更新任务状态
     *
     * @param id 任务ID
     * @param status 任务状态
     * @return 影响行数
     */
    @Update("UPDATE task SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Byte status);
}
