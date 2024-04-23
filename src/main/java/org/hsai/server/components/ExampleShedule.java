package org.hsai.server.components;

import org.hsai.server.abstraction.service.TaskService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public record ExampleShedule(TaskService taskService) {
    @Scheduled(cron = "0 * * * * *")
    public void sendNotification() {
        //userSubscriptionService.sendSubscription().subscribe();
        //Метод, проверяющий каждую минуту дедлайны отправляющий в телегу уведомления
    }
}
