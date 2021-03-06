package com.example.squiz_bot.handler;

import com.example.squiz_bot.State;
import com.example.squiz_bot.model.Question;
import com.example.squiz_bot.model.User;
import com.example.squiz_bot.repository.JpaQuestionRepository;
import com.example.squiz_bot.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.squiz_bot.util.TelegramUtil.createInlineKeyboardButton;
import static com.example.squiz_bot.util.TelegramUtil.createMessageTemplate;


@Slf4j
@Component
public class QuizHandler implements Handler {
    //Храним поддерживаемые CallBackQuery в виде констант
    public static final String QUIZ_CORRECT = "/quiz_correct";
    public static final String QUIZ_INCORRECT = "/quiz_incorrect";
    public static final String QUIZ_START = "/quiz_start";
    public static final String QUIZ_END = "/quiz_end";
    public static final String QUIZ_THEME = "/quiz_theme";


    //Храним варианты ответа
    private static final List<String> OPTIONS = List.of("A", "B", "C", "D");
    private static ArrayList<Integer> answeredQuestions = new ArrayList<>();

    private final JpaUserRepository userRepository;
    private final JpaQuestionRepository questionRepository;
    private String correctAnswerForQuestion;


    public QuizHandler(JpaUserRepository userRepository, JpaQuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;

    }

    @Override
    public List<SendMessage> handle(User user, String message) {
        if (message.startsWith(QUIZ_CORRECT)) {
            // действие на коллбек с правильным ответом
            return correctAnswer(user, message);
        } else if (message.startsWith(QUIZ_INCORRECT)) {

            // действие на коллбек с неправильным ответом
            answeredQuestions = new ArrayList<>();
            return incorrectAnswer(user);
        } else if (message.startsWith(QUIZ_END)) {

            // действие на окончание теста

            return endOfQuiz(user);
        } else if (message.startsWith(QUIZ_THEME)) {
            return chooseTheme(user);
        } else {
            try {
                if ((null != message.split(QUIZ_START)[1])) {
                    user.setTheme(message.split(QUIZ_START)[1]);
                }
            } catch (IndexOutOfBoundsException ignored) {

            }


            return startNewQuiz(user);
        }
    }

    private List<SendMessage> correctAnswer(User user, String message) {
        log.info("correct");
        final int currentScore = user.getScore() + 1;
        user.setScore(currentScore);
        userRepository.save(user);

        return nextQuestion(user);
    }

    private List<SendMessage> endOfQuiz(User user) {
        final int currentScore = user.getScore();
        String newHighScore = "";
        // Обновляем лучший итог
        if (user.getHighScore() < currentScore) {
            user.setHighScore(currentScore);
            newHighScore = "Это новый рекорд!";
        }
        // Меняем статус пользователя
        user.setScore(0);
        user.setBotState(State.NONE);
        userRepository.save(user);

        // Создаем кнопку для повторного начала игры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Повторим?", QUIZ_START),
                createInlineKeyboardButton("Сменить тему?", QUIZ_THEME));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);

        finalMessage.setText(String.format("Вопросики кончились!%nТебе удалось набрать *%d* из *%d!*%n *%s*%n",currentScore, answeredQuestions.size(), newHighScore));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);
        answeredQuestions = new ArrayList<>();
        return List.of(finalMessage);
    }

    private List<SendMessage> incorrectAnswer(User user) {
        final int currentScore = user.getScore();
        String newHighScore = "";
        // Обновляем лучший итог
        if (user.getHighScore() < currentScore) {
            user.setHighScore(currentScore);
            newHighScore = "Это новый рекорд!";
        }
        // Меняем статус пользователя
        user.setScore(0);
        user.setBotState(State.NONE);
        userRepository.save(user);

        // Создаем кнопку для повторного начала игры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Повторим?", QUIZ_START),
                createInlineKeyboardButton("Сменить тему?", QUIZ_THEME));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage finalMessage = createMessageTemplate(user);

        finalMessage.setText(String.format("Эх, не угадал! Со всеми бывает%nПравильный ответ: *%s*%nТебе удалось набрать *%d*!%n%s", correctAnswerForQuestion, currentScore, newHighScore));
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);
    }

    private List<SendMessage> chooseTheme(User user) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Случайные", QUIZ_START + "general"),
                createInlineKeyboardButton("География", QUIZ_START + "geography"));
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = List.of(
                createInlineKeyboardButton("История", QUIZ_START + "historical"),
                createInlineKeyboardButton("Кино", QUIZ_START + "cinema"));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne, inlineKeyboardButtonsRowTwo));
        SendMessage finalMessage = createMessageTemplate(user);
        finalMessage.setText("Выбери тему для битвы");
        finalMessage.setReplyMarkup(inlineKeyboardMarkup);

        return List.of(finalMessage);
    }

    private List<SendMessage> startNewQuiz(User user) {
        user.setBotState(State.PLAYING_QUIZ);
        userRepository.save(user);

        return nextQuestion(user);
    }

    private List<SendMessage> nextQuestion(User user) {

        try {
            int[] answers = new int[answeredQuestions.size() + 1];
            answers[0] = 0;
            for (int i = 0; i < answeredQuestions.size(); i++) {
                answers[i + 1] = answeredQuestions.get(i);
            }

            Question question = questionRepository.getRandomByTheme(user.getTheme(), answers);
            answeredQuestions.add(question.getId());

            // Собираем список возможных вариантов ответа
            List<String> options = new ArrayList<>(List.of(question.getCorrectAnswer(), question.getOptionOne(), question.getOptionTwo(), question.getOptionThree()));
            // Перемешиваем
            Collections.shuffle(options);
            correctAnswerForQuestion = question.getCorrectAnswer();

            // Начинаем формировать сообщение с вопроса
            StringBuilder sb = new StringBuilder();
            sb.append('*')
                    .append(question.getQuestion())
                    .append("*\n\n");


            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

            // Создаем два ряда кнопок
            List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = new ArrayList<>();
            List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = new ArrayList<>();

            // Формируем сообщение и записываем CallBackData на кнопки
            for (int i = 0; i < options.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                final String callbackData = options.get(i).equalsIgnoreCase(question.getCorrectAnswer()) ? QUIZ_CORRECT : QUIZ_INCORRECT;

                button.setText(OPTIONS.get(i));
                button.setCallbackData(String.format("%s %d", callbackData, question.getId()));

                if (i < 2) {
                    inlineKeyboardButtonsRowOne.add(button);
                } else {
                    inlineKeyboardButtonsRowTwo.add(button);
                }
                sb.append(OPTIONS.get(i)).append(". ").append(options.get(i));
                sb.append("\n");
            }


            inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne, inlineKeyboardButtonsRowTwo));
            SendMessage finalMessage = createMessageTemplate(user);
            finalMessage.setText(sb.toString());
            finalMessage.setReplyMarkup(inlineKeyboardMarkup);
            return List.of(finalMessage);
        } catch (NullPointerException e) {
            return handle(user, QUIZ_END);
        }


    }

    @Override
    public State operatedBotState() {
        return null;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(QUIZ_START, QUIZ_CORRECT, QUIZ_INCORRECT, QUIZ_THEME, QUIZ_END);
    }
}