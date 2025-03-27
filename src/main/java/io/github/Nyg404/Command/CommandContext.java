package io.github.Nyg404.Command;

import io.github.Nyg404.Bd.DataUser;
import io.github.Nyg404.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
@Getter
@Slf4j
public class CommandContext {
    private final Chat chat;
    private final DataUser userId;
    private final String command;
    private final List<String> args;

    // Конструктор для извлечения данных из Message
    public CommandContext(Message message, String prefix){
        this.chat = message.getChat();
        this.userId = DataUser.of(message.getFrom().getId(), message.getChatId());

        // Проверка на наличие префикса в начале текста сообщения
        String text = message.getText();
        if (!text.startsWith(prefix)) {
            this.command = "";  // Если префикса нет, можно сделать команду пустой
            this.args = List.of();  // И аргументы пустыми
            return;  // Выходим, чтобы дальше не продолжать
        }

        // Если префикс есть, продолжаем извлечение команды и аргументов
        text = text.substring(prefix.length()).trim();  // Убираем префикс и пробелы

        String[] parts = text.split("\\s+");
        this.command = parts.length > 0 ? parts[0] : "";
        this.args = parts.length > 1 ? Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)) : List.of();
    }


    // Метод отправки сообщений
    public void sendMessage(String text){
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chat.getId().toString())
                .text(text)
                .build();
        try {
            Main.getBot().getClient().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
