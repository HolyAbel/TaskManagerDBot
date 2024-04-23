package org.hsai.server.abstraction.serviceimpl;

import org.hsai.server.abstraction.service.TaskService;
import org.hsai.server.repository.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    TaskRepo taskRepo;

    @Override
    public Mono<TaskDto> getById(Long id) {
        return taskRepo
                .findById(id)
                .map(TaskDto::fromDbEntity);
    }

    @Override
    public Mono<Long> addTask(AddTaskDto addTaskDto) {
        return taskRepo
                .save(AddTaskDto.toDbEntity(addTaskDto))
                .map(TaskRepo.Task::id);
    }

    @Override
    public Mono<Void> deleteTask(Long id) {
        return taskRepo
                .deleteById(id);
    }

    @Override
    public Mono<Long> updateTask(EditTaskDto editTaskDto, Long id) {
        return taskRepo
                .save(EditTaskDto.toDbEntity(editTaskDto, id))
                .map(TaskRepo.Task::id);
    }

    @Override
    public Mono<List<TaskDto>> getByType(Integer type, Long id) {
        return taskRepo
                .findByType(type, id)
                .map(TaskDto::fromDbEntity)
                .collectList();
    }
    @Override
    public Flux<TaskDto> getByDeadline(Instant datetime) {
        return taskRepo
                .findByDeadline(datetime)
                .map(TaskDto::fromDbEntity);
    }

    @Override
    public Mono<List<TaskDto>> getDay(Instant datetime, Long id) {
        return taskRepo
                .findAllByDeadlineLessThan(datetime.plusSeconds(86400), id)
                .map(TaskDto::fromDbEntity)
                .collectList();
    }

    @Override
    public Mono<List<TaskDto>> getWeek(Instant datetime, Long id) {
        return taskRepo
                .findAllByDeadlineLessThan(datetime.plusSeconds(604800), id)
                .map(TaskDto::fromDbEntity)
                .collectList();
    }

}
