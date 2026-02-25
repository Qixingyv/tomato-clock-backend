package com.paolua.tomatoclockbackend.service.impl;

import com.paolua.tomatoclockbackend.mapper.TaskMappler;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMappler taskMappler;

    @Override
    public List<Task> getTomatoTaskListByUserId(Integer userId) {
        return taskMappler.getTomatoTaskListByUserId(userId);
    }
}
