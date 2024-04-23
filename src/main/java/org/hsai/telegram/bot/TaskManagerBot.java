package org.hsai.telegram.bot;


import org.hsai.server.abstraction.service.TaskService;
import org.hsai.server.abstraction.service.UserService;

import org.hsai.server.controller.TaskController;
import org.hsai.server.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hsai.server.abstraction.service.TaskService.AddTaskDto;
import org.hsai.server.abstraction.service.TaskService.EditTaskDto;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Component
public class TaskManagerBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManagerBot.class);

    @Autowired
    TaskService taskService;
    @Autowired
    UserService userService;
    @Autowired
    TaskController taskController;
    @Autowired
    UserController userController;
    Long user_id;
    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String SIGN_IN = "/signin";
    private static final String SIGN_UP = "/signup";
    private static final String SHOW_TASK = "/show_task";
    private static final String ADD_TASK = "/add_task";
    private static final String UPDATE_TASK = "/update_task";
    private static final String DELETE_TASK = "/delete_task";
    private static final String DAILY_TASK = "/daily_tasks";
    private static final String WEEKLY_TASK = "/weekly_tasks";
    private static final String RECUR_TASK = "/recur_tasks";


    private static final String COMMANDS = """
                Доступны следующие команды:\n
                /add_task <Название> <Тип> <Дата дедлайна> <Время дедлайна> - добавить задание. Тип: 0 для неповторяющегося, 1 для почасовой, 2 для ежедневной, 3 для еженедельной и 4 для ежемесячной.\n
                /update_task <ID> <Название> <Тип> <Дата дедлайна> <Время дедлайна> - обновить описание существующей задачи.\n
                /delete_task <ID> - удалить задание.\n
                /daily_tasks - вывести задачи на день.\n
                /weekly_tasks - вывести задачи на неделю.\n
                /recur_tasks - вывести все повторяемые задания.\n
                /help - вывести список команд
            """;

    public TaskManagerBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update){
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var messages = update.getMessage().getText().split(" ");
        var chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();
        switch (messages[0]) {
            case START -> startCommand(chatId, userName);
            case HELP -> helpCommand(chatId);
            //case SIGN_UP -> singUp(chatId, messages[1], messages[2], messages[3], messages[4]);
            case DAILY_TASK -> dailyTask(chatId);
            case WEEKLY_TASK -> weeklyTask(chatId);
            case RECUR_TASK -> recurTask(chatId);
            case ADD_TASK -> addTask(chatId, messages[1], messages[2], messages[3], messages[4]);
            case UPDATE_TASK -> updateTask(chatId, messages[1], messages[2], messages[3], messages[4], messages[5]);
            case DELETE_TASK -> deleteTask(chatId, messages[1]);
        }
    }

    @Override
    public String getBotUsername(){
        return "HSAI23_TaskManager_bot";
    }


    private void startCommand(Long chatId, String username) {
        singUp(chatId, username);
        var msg = " Доброго времени суток! " + username;
        sendMessage(chatId, msg);
    }
    private void helpCommand(Long chatId) {
        var msg = """          
                %s
                """;
        sendMessage(chatId, msg.formatted(COMMANDS));
    }
    private void singUp(Long chatId, String login) {
        //Integer id = (int) (long) chatId;
        //UserService.AddUserDto user = new UserService.AddUserDto(login);
        //userService.addUser(user);

        userService.addUser(chatId, login);

        sendMessage(chatId, chatId.toString());
    }
    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Sending message error", e);
        }
    }
    private void dailyTask(Long chatId) {
        var msg = taskService.getDay(Instant.now(), chatId)
                .block()
                .stream()
                .map(task -> "Номер: " + task.id() + " Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg = msg.replace("[",  "");
        msg = msg.replace("]",  "");
        msg = msg.replace("T",  " ");
        msg = msg.replace("Z",  " ");
        msg = msg.replaceAll(",", ",\n");

        msg = "Задачи на ближайшие 24 часа:\n" + msg;

        sendMessage(chatId, msg);
    }
    private void weeklyTask(Long chatId) {
        var msg = taskService.getWeek(Instant.now(), chatId)
                .block()
                .stream()
                .map(task -> "Номер: " + task.id() + "Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg = msg.replace("[",  "");
        msg = msg.replace("]",  "");
        msg = msg.replace("T",  " ");
        msg = msg.replace("Z",  " ");
        msg = msg.replaceAll(",", ",\n");

        msg = "Задачи на ближайшую неделю:\n" + msg;

        sendMessage(chatId, msg);
    }
    private  void recurTask(Long chatId) {
        var msg = taskService.getByType(1, chatId)
                .block()
                .stream()
                .map(task -> "Номер: " + task.id() + "Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg = msg.replace("[",  "");
        msg = msg.replace("]",  "");
        msg = msg.replace("T",  " ");
        msg = msg.replace("Z",  " ");
        msg = msg.replaceAll(",", ",\n");

        var msg2 = taskService.getByType(2, chatId)
                .block()
                .stream()
                .map(task -> "Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg2 = msg2.replace("[",  "");
        msg2 = msg2.replace("]",  "");
        msg2 = msg2.replace("T",  " ");
        msg2 = msg2.replace("Z",  " ");
        msg2 = msg2.replaceAll(",", ",\n");

        var msg3 = taskService.getByType(3, chatId)
                .block()
                .stream()
                .map(task -> "Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg3 = msg3.replace("[",  "");
        msg3 = msg3.replace("]",  "");
        msg3 = msg3.replace("T",  " ");
        msg3 = msg3.replace("Z",  " ");
        msg3 = msg3.replaceAll(",", ",\n");

        var msg4 = taskService.getByType(4, chatId)
                .block()
                .stream()
                .map(task -> "Задача: " + task.message() + " с дедлайном: " + task.deadline())
                .toList()
                .toString();
        msg4 = msg4.replace("[",  "");
        msg4 = msg4.replace("]",  "");
        msg4 = msg4.replace("T",  " ");
        msg4 = msg4.replace("Z",  " ");
        msg4 = msg4.replaceAll(",", ",\n");

        var finMsg = msg + "\n" + msg2 + "\n" + msg3 + "\n" + msg4;

        sendMessage(chatId, finMsg);
    }
    private void addTask(Long chatId, String mes, String type, String deadlineD, String deadlinedT) {
        String input = deadlineD + "; " + deadlinedT;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd; HH:mm:ss");
        try {
            Date dateTime = sf.parse(input); // 1L - временная заглушка
            AddTaskDto task = new AddTaskDto(chatId , mes, Integer.valueOf(type), dateTime.toInstant(), false);
            taskService.addTask(task).block();
            sendMessage(chatId, "Успешно добавлено!");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void updateTask(Long chatId, String id, String mes, String type, String deadlineD, String deadlinedT) {
        String input = deadlineD + "; " + deadlinedT;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd; HH:mm:ss");
        try {
            Date dateTime = sf.parse(input);
            EditTaskDto task  = new EditTaskDto(chatId , mes, Integer.valueOf(type), dateTime.toInstant(), false);
            taskService.updateTask(task,Long.valueOf(id)).block();
            sendMessage(chatId, "Успешно обновлено!");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void deleteTask(Long chatId, String id) {
        taskService.deleteTask(Long.valueOf(id)).block();
        sendMessage(chatId, "Успешно удалено!");
    }
}
