package io.github.Nyg404;

import io.github.Nyg404.Bd.DataServer;
import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.PrefixUtils;
import io.github.Nyg404.Permission.DataServerPermission;
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
        this.client = new OkHttpTelegramClient(token);
        this.telegramClient = this.client; // Один объект на два поля
        this.updatee = new io.github.Nyg404.Update(telegramClient);
        this.prefixUtils = new PrefixUtils();
        
        // Создаем таблицы при старте
        DataBaseConnect.createTables();
    }

    @Override
    public void consume(Update update) {
        updatee.processUpdate(update);
        Message message = update.getMessage();
        if (message == null || !message.hasText()) {
            return; // Игнорируем обновления без текста
        }

        long chatId = message.getChatId();
        if (!DataBaseManager.isServerRegistered(chatId)) {
            DataBaseManager.addServerToDB(chatId, "/");
            DataServerPermission.addDefaultPermissions(update.getMessage().getChatId());
        }

        String prefix = DataServer.selectPrefix(chatId);
        if (prefix == null || message.getText() == null || !message.getText().startsWith(prefix)) {
            return; // Игнорируем сообщения без префикса
        }

        CommandContext commandContext = new CommandContext(message, prefix);
        prefixUtils.handlePrefixCommand(commandContext);
    }

    public OkHttpTelegramClient getClient() {
        return client;
    }
}
