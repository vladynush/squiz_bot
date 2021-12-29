package com.example.squiz_bot.handler;

import com.example.squiz_bot.State;
import com.example.squiz_bot.model.User;
import com.example.squiz_bot.repository.JpaUserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;

import static com.example.squiz_bot.handler.QuizHandler.QUIZ_START;
import static com.example.squiz_bot.util.TelegramUtil.createInlineKeyboardButton;
import static com.example.squiz_bot.util.TelegramUtil.createMessageTemplate;


@Component
public class RegistrationHandler implements Handler {
    //Храним поддерживаемые CallBackQuery в виде констант
    public static final String NAME_ACCEPT = "/enter_name_accept";
    public static final String NAME_CHANGE = "/enter_name";
    public static final String NAME_CHANGE_CANCEL = "/enter_name_cancel";

    private final JpaUserRepository userRepository;

    public RegistrationHandler(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<SendMessage> handle(User user, String message) {
        // Проверяем тип полученного события
        if (message.equalsIgnoreCase(NAME_ACCEPT) || message.equalsIgnoreCase(NAME_CHANGE_CANCEL)) {
            return accept(user);
        } else if (message.equalsIgnoreCase(NAME_CHANGE)) {
            return changeName(user);
        }
        return checkName(user, message);

    }

    private List<SendMessage> accept(User user) {
        // Если пользователь принял имя - меняем статус и сохраняем
        user.setBotState(State.NONE);
        userRepository.save(user);

        // Создаем кнопку для начала игры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Запустить испытание", QUIZ_START));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);
        finalMessage.setText(String.format("Я тебя запомнил: *%s*%nХочешь сыграть?", user.getName()));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);
    }

    private List<SendMessage> checkName(User user, String message) {
        // При проверке имени мы превентивно сохраняем пользователю новое имя в базе
        // идея для рефакторинга - добавить временное хранение имени
        user.setName(message);
        userRepository.save(user);

        // Делаем кнопку для применения изменений
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Верно", NAME_ACCEPT));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);
        finalMessage.setText(String.format("Как говоришь? *%s*?%nЕсли я правильно услышал - жмакай по кнопке", user.getName()));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);
    }

    private List<SendMessage> changeName(User user) {
        // При запросе изменения имени мы меняем State
        user.setBotState(State.ENTER_NAME);
        userRepository.save(user);

        // Создаем кнопку для отмены операции
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Отмена", NAME_CHANGE_CANCEL));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);
        finalMessage.setText(String.format("Вроде тебя звали: *%s*%nКак тебя теперь звать? Или жми Отмена и забудем об этом", user.getName()));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);
    }

    @Override
    public State operatedBotState() {
        return State.ENTER_NAME;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(NAME_ACCEPT, NAME_CHANGE, NAME_CHANGE_CANCEL);
    }
}
