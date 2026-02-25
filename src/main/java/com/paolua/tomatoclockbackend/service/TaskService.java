package com.paolua.tomatoclockbackend.service;

import com.paolua.tomatoclockbackend.pojo.Task;

import java.util.List;

public interface TaskService {

    public List<Task> getTomatoTaskListByUserId(Integer userId);
}
