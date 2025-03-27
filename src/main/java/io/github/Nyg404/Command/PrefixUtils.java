package io.github.Nyg404.Command;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

import io.github.Nyg404.Bd.DataServer;

@Slf4j
public class PrefixUtils {

    public void handlePrefixCommand(CommandContext context) {
        long serverId = context.getChat().getId();
        List<String> args = context.getArgs();
        String command = context.getCommand();
        if(!command.equals("setprefix")){
            return;
        }
        if (args.isEmpty()) {
            context.sendMessage("Укажите новый префикс!");  // Если нет аргументов
            return;
        }

        String newPrefix = args.get(0);  // Извлекаем новый префикс
        DataServer.updatePrefix(serverId, newPrefix);  // Обновляем префикс в базе данных
        context.sendMessage("✅ Новый префикс: " + newPrefix);  // Отправляем подтверждение
        log.info("Для сервера: {} Был изменен префикс на {}", serverId, newPrefix);
    }
}