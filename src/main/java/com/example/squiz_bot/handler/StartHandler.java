package com.example.squiz_bot.handler;

import com.example.squiz_bot.State;
import com.example.squiz_bot.model.User;
import com.example.squiz_bot.repository.JpaUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.example.squiz_bot.util.TelegramUtil.createMessageTemplate;


@Component
public class StartHandler implements Handler {
    @Value("${spring.bot.name}")
    private String botUsername;

    private final JpaUserRepository userRepository;

    public StartHandler(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        // Приветствуем пользователя
        SendMessage welcomeMessage = createMessageTemplate(user);
        welcomeMessage.setText(String.format(
                        "Hola! I'm *%s*%nI am here to help you learn Java", botUsername
                ));
        // Просим назваться
        SendMessage registrationMessage = createMessageTemplate(user);
        registrationMessage.setText("In order to start our journey tell me your name");
        // Меняем пользователю статус на - "ожидание ввода имени"
        user.setBotState(State.ENTER_NAME);
        userRepository.save(user);

        return List.of(welcomeMessage, registrationMessage);
    }

    @Override
    public State operatedBotState() {
        return State.START;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
