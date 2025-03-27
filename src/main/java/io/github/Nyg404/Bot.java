package io.github.Nyg404;

import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.PrefixUtils;
import io.github.Nyg404.Utils.DataBaseConnect;
import io.github.Nyg404.Utils.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Slf4j
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final OkHttpTelegramClient client;
    private final io.github.Nyg404.Update updatee;
    private final PrefixUtils prefixUtils;
    public Bot(String token) {
        // Предполагаем, что конструктор OkHttpTelegramClient принимает токен:
        this.telegramClient = new OkHttpTelegramClient(token);
        this.client = new OkHttpTelegramClient(token);
        this.updatee = new io.github.Nyg404.Update(telegramClient);
        this.prefixUtils = new PrefixUtils();
    }

    @Override
    public void consume(Update update) {

        DataBaseConnect.createTables();

        Message message = update.getMessage();
        if (message == null || !message.hasText()) {
            return; // Игнорируем обновления без текста
        }

        long chatId = message.getChatId();
        String prefix = DataBaseManager.selectPrefix(chatId);  // Получаем префикс для чата
        if (prefix == null || !message.getText().startsWith(prefix)) {
            return;  // Игнорируем сообщения, не начинающиеся с префикса
        }

        CommandContext commandContext = new CommandContext(message, prefix);  // Создаем CommandContext

        // Обрабатываем команду
        prefixUtils.handlePrefixCommand(commandContext);
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            DataBaseConnect.createTables();
//
//            long chatId = update.getMessage().getChatId();
//            long userId = update.getMessage().getFrom().getId();
//
//            // Регистрация сервера
            if (!DataBaseManager.isServerRegistered(chatId)) {
                DataBaseManager.addServerToDB(chatId, "/");
            }
//
//            // Регистрация пользователя
//            if (!DataBaseManager.isRegister(userId, chatId)) {
//                DataBaseManager.addUser(userId, chatId, 1);
//            }
//
//            System.out.printf("Автор: %s Сообщение: %s Сервер: %d Пользователь добавлен? %b%n",
//                    update.getMessage().getFrom().getUserName(),
//                    update.getMessage().getText(),
//                    chatId,
//                    DataBaseManager.isRegister(userId, chatId));
//
//            InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
//                    .keyboardRow(new InlineKeyboardRow(
//                            InlineKeyboardButton.builder().text("Кррр").callbackData("i").build()
//                    ))
//                    .build();
//
//            SendMessage sendMessage = SendMessage.builder()
//                    .chatId(String.valueOf(chatId))
//                    .text("Вы геи")
//                    .replyMarkup(keyboardMarkup)
//                    .build();
//
//            try {
//                telegramClient.execute(sendMessage);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public OkHttpTelegramClient getClient() {
        return client;
    }
}
