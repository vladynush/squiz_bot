package com.example.squiz_bot.handler;

import com.example.squiz_bot.State;
import com.example.squiz_bot.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface Handler {

// основной метод, который будет обрабатывать действия пользователя
    List<SendMessage> handle(User user, String message);
    // метод, который позволяет узнать, можем ли мы обработать текущий State у пользователя
    State operatedBotState();
    // метод, который позволяет узнать, какие команды CallBackQuery мы можем обработать в этом классе
    List<String> operatedCallBackQuery();
}
