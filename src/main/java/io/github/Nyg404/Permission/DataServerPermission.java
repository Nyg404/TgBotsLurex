package io.github.Nyg404.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import io.github.Nyg404.Utils.DataBaseConnect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
/**
 * Класс, представляющий права сервера.
 * Содержит методы для получения и проверки прав на выполнение различных действий.
 */
@Getter
@Slf4j
public class DataServerPermission {

    private final Long serverId;
    private final int setPrefix;  
      

    private static final ConcurrentHashMap<Long, DataServerPermission> cache = new ConcurrentHashMap<>();

    // Конструктор с несколькими правами
    public DataServerPermission(long serverId, int setPrefix) {
        this.serverId = serverId;
        this.setPrefix = setPrefix;
    }

    public static DataServerPermission of(Long serverId) {
        DataServerPermission permission = cache.get(serverId);
        if (permission != null) {
            return permission;
        }
        String selectServer = "SELECT * FROM servers_permission WHERE server_id = ?";
        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectServer)) {
                preparedStatement.setLong(1, serverId);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        int setPrefix = rs.getInt("setprefix");
                        permission = new DataServerPermission(serverId, setPrefix);
                        cache.put(serverId, permission);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении прав сервера: " + e.getMessage(), e);
        }
        return permission;
    }

    

    public boolean hasPermission(PermissionType action, int userPermission) {
        switch (action) {
            case SETPREFIX:
                return userPermission >= this.setPrefix;
            default:
                return false;
        }
    }

    public static DataServerPermission selectPermission(Long serverId) {
        return of(serverId);
    }

    public static void addDefaultPermissions(long serverId) {
        // Проверяем, есть ли уже права для этого сервера
        DataServerPermission currentPermissions = of(serverId);
        if (currentPermissions != null) {
            log.info("Для сервера {} уже установлены права", serverId);
            return;  // Если права уже есть, выходим
        }

        // Вставляем стандартные права в базу
        String insertPermissions = "INSERT INTO servers_permission (server_id, setprefix) VALUES (?, ?)";
        try (Connection connection = DataBaseConnect.getInstance().connection();
             PreparedStatement stmt = connection.prepareStatement(insertPermissions)) {

            stmt.setLong(1, serverId);
            stmt.setInt(2, 3); // Стандартное право для setprefix = 0 (права для всех пользователей)

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                log.info("Стандартные права были добавлены для сервера {}", serverId);
                cache.put(serverId, new DataServerPermission(serverId, 3));
            } else {
                log.error("Ошибка при добавлении стандартных прав для сервера {}", serverId);
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении стандартных прав для сервера: " + e.getMessage(), e);
        }
    }
}
