package io.github.Nyg404.Bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import io.github.Nyg404.Permission.DataServerPermission;
import io.github.Nyg404.Utils.DataBaseConnect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Getter
public class DataServer {
    private final Long serverId;
    private final String prefix;

    public DataServer(Long serverId, String prefix) {
        this.serverId = serverId;
        this.prefix = prefix;
    }

    private static final ConcurrentHashMap<Long, DataServer> Cache = new ConcurrentHashMap<>();

    public static DataServer of(long serverId){
        DataServer dataServer = Cache.get(serverId);
        if(dataServer != null){
            return dataServer;
        }
        String selectServer = "SELECT * FROM servers WHERE server_id = ?";
        try(Connection connection = DataBaseConnect.getInstance().connection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(selectServer)){
                preparedStatement.setLong(1, serverId);
                try(ResultSet rs = preparedStatement.executeQuery()){
                    if(rs.next()){
                        dataServer = new DataServer(rs.getLong("server_id"), rs.getString("prefix"));
                        Cache.put(serverId, dataServer);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получение сервера" + e.getMessage(), e);
        } 
        return dataServer;
    }

    public static boolean isRegistered(long serverId){
        return of(serverId) != null;
    }

    public static boolean addServer(long serverId, String prefix) {
        String insertServer = "INSERT INTO servers (server_id, prefix) VALUES (?, ?)";
        try (Connection connection = DataBaseConnect.getInstance().connection();
             PreparedStatement stmt = connection.prepareStatement(insertServer)) {
            stmt.setLong(1, serverId);
            stmt.setString(2, prefix);
    
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Сначала добавляем сервер в кэш
                DataServer dataServer = new DataServer(serverId, prefix);
                Cache.put(serverId, dataServer);
    
                // Затем добавляем права
                
                log.info("Сервер добавлен в базу данных и кэш.");
                return true;
            } else {
                log.error("Ошибка при добавлении сервера. Нет затронутых строк.");
                return false;
            }
        } catch (SQLException e) {
            log.error("Ошибка при добавлении сервера: " + e.getMessage(), e);
            return false;
        }
    }
    

    public static boolean updatePrefix(long serverId, String newPrefix) {
        String updatePrefix = "UPDATE servers SET prefix = ? WHERE server_id = ?";
        try (Connection connection = DataBaseConnect.getInstance().connection();
             PreparedStatement stmt = connection.prepareStatement(updatePrefix)) {
            stmt.setString(1, newPrefix);
            stmt.setLong(2, serverId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Обновляем кэш
                DataServer updatedServer = new DataServer(serverId, newPrefix);
                Cache.put(serverId, updatedServer);
                log.info("Префикс для сервера {} обновлён на {}", serverId, newPrefix);
                return true;
            } else {
                log.error("Не удалось обновить префикс для сервера {}.", serverId);
                return false;
            }
        } catch (SQLException e) {
            log.error("Ошибка при обновлении префикса: " + e.getMessage(), e);
            return false;
        }
    }
    
    public static String selectPrefix(Long serverId) {
        if (Cache.containsKey(serverId)) {
            return Cache.get(serverId).getPrefix();
        }
        
        String selectPrefix = "SELECT prefix FROM servers WHERE server_id = ?";
        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            try (PreparedStatement stmt = connection.prepareStatement(selectPrefix)) {
                stmt.setLong(1, serverId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String prefix = rs.getString("prefix");
                        // Кэшируем результат
                        Cache.put(serverId, new DataServer(serverId, prefix));
                        return prefix;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка получения префикса: " + e.getMessage(), e);
        }
        return null;
    }
    

}
