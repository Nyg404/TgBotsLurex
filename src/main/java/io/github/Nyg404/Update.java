package io.github.Nyg404;

import io.github.Nyg404.Utils.DataBaseConnect;
import io.github.Nyg404.Utils.DataBaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Update {
    private final TelegramClient telegramClient;

    public Update(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void processUpdate(org.telegram.telegrambots.meta.api.objects.Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            if (!DataBaseManager.isRegister(userId, chatId)){
                DataBaseManager.addUser(userId, chatId, 1);
            }
            // Регистрация: вынесем в RegistrationManager или прямо здесь
            String chatType = update.getMessage().getChat().getType();
            String chattypes = switch (chatType) {
                case "private" -> "личка";
                case "group", "supergroup" -> "группа";
                case "channel" -> "канал";  // Добавлено для канала

                default -> "неизвестный чат";  // Добавлено для неизвестных типов чатов

            };

            System.out.printf("Автор: %s Сообщение: %s Сервер: %d Пользователь добавлен? %b%nКакой чат? %s Префикс сервера: %s ",
                    update.getMessage().getFrom().getUserName(),
                    update.getMessage().getText(),
                    chatId,
                    DataBaseManager.isRegister(userId, chatId),
                    chattypes,
                    DataBaseManager.selectPrefix(chatId));
            ;

            // Пример формирования клавиатуры
            InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Кррр").callbackData("i").build()
                    ))
                    .build();

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(".")
                    .replyMarkup(keyboardMarkup)
                    .build();

            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
