package io.github.Nyg404.Command;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

import io.github.Nyg404.Bd.DataServer;
import io.github.Nyg404.Bd.DataUser;
import io.github.Nyg404.Permission.DataServerPermission;
import io.github.Nyg404.Permission.PermissionType;

@Slf4j
public class PrefixUtils {

    public void handlePrefixCommand(CommandContext context) {
        long serverId = context.getChat().getId();
        List<String> args = context.getArgs();
        String command = context.getCommand();
        long userId = context.getUserId().getUserId();
        int userPermission = DataUser.selectPermission(userId, serverId);
        
        if(!command.equals("setprefix")){
            return;
        }
        if(DataServerPermission.selectPermission(serverId).hasPermission(PermissionType.SETPREFIX, userPermission)){
            if (args.isEmpty()) {
                context.sendMessage("Укажите новый префикс!");  // Если нет аргументов
                return;
            }
    
            String newPrefix = args.get(0);  // Извлекаем новый префикс
            DataServer.updatePrefix(serverId, newPrefix);  // Обновляем префикс в базе данных
            context.sendMessage("✅ Новый префикс: " + newPrefix);  // Отправляем подтверждение
            log.info("Для сервера: {} Был изменен префикс на {}", serverId, newPrefix);
        } else {
            context.sendMessage("У вас не достаточно прав!");
        }
        
    }
}