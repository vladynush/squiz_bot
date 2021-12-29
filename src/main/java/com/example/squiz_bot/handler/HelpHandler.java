package com.example.squiz_bot.handler;

import com.example.squiz_bot.State;
import com.example.squiz_bot.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.example.squiz_bot.handler.QuizHandler.QUIZ_START;
import static com.example.squiz_bot.handler.RegistrationHandler.NAME_CHANGE;
import static com.example.squiz_bot.util.TelegramUtil.createInlineKeyboardButton;
import static com.example.squiz_bot.util.TelegramUtil.createMessageTemplate;


@Component
public class HelpHandler implements Handler {

    @Override
    public List<SendMessage> handle(User user, String message) {
        // Создаем кнопку для смены имени
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Изменить имя", NAME_CHANGE),
                createInlineKeyboardButton("Запустить испытание", QUIZ_START));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);
        finalMessage.setText(String.format("" +
                "Помощь вызывали %s? Нет? Но  я всё равно уже тут. Ну, выкладывай, что там у тебя...", user.getName()));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);

    }

    @Override
    public State operatedBotState() {
        return State.NONE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
