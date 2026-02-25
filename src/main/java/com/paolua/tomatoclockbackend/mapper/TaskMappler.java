package com.paolua.tomatoclockbackend.mapper;

import com.paolua.tomatoclockbackend.pojo.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMappler {


    public List<Task> getTomatoTaskListByUserId(Integer userId);
}
