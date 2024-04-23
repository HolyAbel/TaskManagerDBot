package org.hsai.server.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Repository
public interface TaskRepo extends ReactiveCrudRepository<TaskRepo.Task, Long> {

    @Table("task")
    record Task(
            @Id
            Long id,
            Long idUser,
            String message,
            Integer type,
            Instant deadline,
            Boolean isdone
    ){}
    @Query("SELECT * " +
            "FROM task " +
            "WHERE task.type = :type AND task.id_user = :chatId " +
            "ORDER BY task.deadline")
    Flux<TaskRepo.Task>findByType(Integer type, Long chatId);
    Flux<TaskRepo.Task> findByDeadline(Instant deadline);
    @Query("SELECT * " +
            "FROM task " +
            "WHERE task.deadline < :datetime AND task.id_user = :chatId " +
            "ORDER BY task.deadline")
    Flux<TaskRepo.Task> findAllByDeadlineLessThan(Instant deadline, Long chatId); //Для поиска в пределах дня, недели
}