package org.hsai.server.controller;

import org.hsai.server.abstraction.service.TaskService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController
public record TaskController(
        TaskService taskService
) {
    @GetMapping("/getTaskI/{id}")
    public Mono<TaskService.TaskDto> findById(@PathVariable Long id){
        return taskService.getById(id);
    }

    @PostMapping("/addTask")
    public Mono<Long> findById(@RequestBody TaskService.AddTaskDto addTaskDto) {return taskService.addTask(addTaskDto);}

    @DeleteMapping("/deleteTask/{id}")
    public Mono<Void> deleteById(@PathVariable Long id) {return taskService.deleteTask(id);}

    @PostMapping("/updateTask/{id}")
    public Mono<Long> updateById(@RequestBody TaskService.EditTaskDto editEventDto, @PathVariable Long id) {return taskService.updateTask(editEventDto, id);}

    @GetMapping("/getTasksT/{type}")
    public Mono<List<TaskService.TaskDto>> findByType(@PathVariable Integer type, @PathVariable Long id){
        return taskService.getByType(type, id);
    }

    @GetMapping("/getTasksD/{deadline}")
    public Flux<TaskService.TaskDto> findByDeadline(@PathVariable Instant deadline){
        return taskService.getByDeadline(deadline);
    }

    @GetMapping("/getDay/{id}")
    public Mono<List<TaskService.TaskDto>> findDay(@PathVariable Long id){
        return taskService.getDay(Instant.now(), id);
    }

    @GetMapping("/getWeek/{id}")
    public Mono<List<TaskService.TaskDto>> findWeek(@PathVariable Long id){
        return taskService.getWeek(Instant.now(), id);
    }

}
