package io.github.Nyg404.Bd;

import io.github.Nyg404.Utils.DataBaseConnect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
public class DataUser {

    private final Long userId;
    private final Integer level;

    public DataUser(long userId, int level) {
        this.userId = userId;
        this.level = level;
    }

    private static final ConcurrentHashMap<String, DataUser> cache = new ConcurrentHashMap<>();

    public static DataUser of(long userId, long serverId) {
        String cacheKey = generateCacheKey(userId, serverId);
        DataUser user = cache.get(cacheKey);

        // Если пользователь найден в кэше, возвращаем его
        if (user != null) {
            return user;
        }

        // Если не найден в кэше, выполняем запрос в базу данных
        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            String query = "SELECT * FROM Users WHERE user_id = ? AND server_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, userId);
                statement.setLong(2, serverId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        // Создаем пользователя и кэшируем его
                        user = new DataUser(rs.getLong("user_id"), rs.getInt("level"));
                        cache.put(cacheKey, user);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при выполнении запроса: " + e.getMessage(), e);
        }

        return user;
    }

    // Генерация ключа для кэша
    private static String generateCacheKey(long userId, long serverId) {
        return userId + ":" + serverId;
    }

    // Метод для проверки, зарегистрирован ли пользователь
    public static boolean isRegistered(long userId, long serverId) {
        return of(userId, serverId) != null;
    }

    // Метод для добавления пользователя в базу данных и кэш
    public static void addUser(long userId, long serverId, int level) {
        String insertQuery = "INSERT INTO users(user_id, server_id, level) VALUES (?,?,?)";
        String cacheKey = generateCacheKey(userId, serverId);

        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, serverId);
                stmt.setInt(3, level);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Добавляем нового пользователя в кэш
                    DataUser newUser = new DataUser(userId, level);
                    cache.put(cacheKey, newUser);
                    log.info("Пользователь добавлен в базу данных и кэш.");
                } else {
                    log.warn("Не удалось добавить пользователя в базу данных.");
                }
            }
        } catch (SQLException e) {
            log.error("SQL Ошибка при добавлении пользователя: " + e.getMessage(), e);
        }
    }
}
