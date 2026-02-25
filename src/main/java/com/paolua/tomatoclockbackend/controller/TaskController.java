package com.paolua.tomatoclockbackend.controller;

import com.paolua.tomatoclockbackend.pojo.Result;
import com.paolua.tomatoclockbackend.pojo.Task;
import com.paolua.tomatoclockbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // 根据用户id查询任务列表
    @GetMapping("/{userId}")
    public Result getTomatoListByUserId(@PathVariable Integer userId) {

        List<Task> data = taskService.getTomatoTaskListByUserId(userId);
        System.out.println(data);
        return Result.success(data);
    }

}
