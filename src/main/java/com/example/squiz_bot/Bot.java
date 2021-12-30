package com.example.squiz_bot;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@Getter
public class Bot extends TelegramLongPollingBot {

    @Value("${spring.bot.name}")
    private String botUsername;
    @Value("${spring.bot.token}")
    private String botToken;

    private final UpdateReceiver updateReceiver;

    public Bot(UpdateReceiver updateReceiver) {
        this.updateReceiver = updateReceiver;
    }

    @Override
    public void onUpdateReceived(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        //удаление сообщения бота
        if (update.getCallbackQuery() != null) {

            deleteMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        } else {
            deleteMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
            deleteMessage.setMessageId(update.getMessage().getMessageId() - 1);
        }

        List<SendMessage> messagesToSend = updateReceiver.handle(update);
        if (messagesToSend != null && !messagesToSend.isEmpty()) {
            messagesToSend.forEach(response -> {
                if (response != null) {
                    executeWithExceptionCheck(response, deleteMessage);
                }
            });
        }
    }


    public void executeWithExceptionCheck(SendMessage sendMessage, DeleteMessage deleteMessage) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            execute(sendMessage);


        } catch (TelegramApiException e) {
            log.error("oops");
        }
    }

    public void executeWithExceptionCheck(SendMessage sendMessage) {
        try {
            execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error("oops");
        }
    }
}
