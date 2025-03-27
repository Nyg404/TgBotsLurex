package io.github.Nyg404.Utils;


import io.github.Nyg404.Bd.DataServer;
import io.github.Nyg404.Bd.DataUser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class DataBaseManager {
    private static final ConcurrentHashMap<String, DataUser> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, DataServer> serverCache = new ConcurrentHashMap<>();

    public static DataUser of(long userId, long serverId){
        String cacheKey = userId + ":" + serverId;
        if(cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        try(Connection connection = DataBaseConnect.getInstance().connection()) {
            String selectuser = "SELECT * FROM Users WHERE user_id = ? AND server_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(selectuser)){
                statement.setLong(1, userId);
                statement.setLong(2, serverId);
                try(ResultSet rs = statement.executeQuery()) {
                    if (rs.next()){
                        DataUser dataUser = new DataUser(rs.getLong("user_id"), rs.getInt("level"));
                        cache.put(cacheKey, dataUser);
                        return dataUser;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
        return null;
    }

    public static boolean isRegister(long userId, long serverId){
        return of(userId, serverId) != null;
    }


    public static boolean addServerToDB(long serverId, String prefix) {
        String insertServer = "INSERT INTO servers (server_id, prefix) VALUES (?, ?)";
        try (Connection connection = DataBaseConnect.getInstance().connection();
             PreparedStatement stmt = connection.prepareStatement(insertServer)) {
            stmt.setLong(1, serverId);
            stmt.setString(2, prefix);

            int rowsAffected = stmt.executeUpdate(); // Сначала выполняем запрос
            if (rowsAffected > 0) {
                // Затем коммитим изменения
                DataServer dataServer = new DataServer(serverId, prefix);
                serverCache.put(serverId, dataServer);
                System.out.println("Сервер добавлен в базу данных и кэш.");
                return true;
            } else {
                System.out.println("Ошибка при добавлении сервера.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении сервера: " + e.getMessage());
            e.printStackTrace(); // Детальное логирование исключения
            return false;
        }
    }

    public static DataServer getServerFromCache(long serverId) {
        return serverCache.get(serverId);
    }

    public static boolean isServerRegistered(long serverId) {
        if (serverCache.containsKey(serverId)) {
            return true;
        }

        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            String selectServer = "SELECT * FROM servers WHERE server_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectServer)) {
                stmt.setLong(1, serverId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {

                        DataServer dataServer = new DataServer(rs.getLong("server_id"), rs.getString("prefix"));
                        serverCache.put(serverId, dataServer);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке сервера", e);
        }
        return false;
    }

    public static void addUser(long userId, long serverId, int level) {
        String insertuser = "INSERT INTO users(user_id, server_id, level) VALUES (?,?,?)";
        String cacheKey = userId + ":" + serverId;

        try (Connection connection = DataBaseConnect.getInstance().connection()) {
            try (PreparedStatement stmp = connection.prepareStatement(insertuser)) {
                stmp.setLong(1, userId);
                stmp.setLong(2, serverId);
                stmp.setInt(3, level);

                int rowsAffected = stmp.executeUpdate();
                if (rowsAffected > 0) {// Коммит после успешного выполнения
                    DataUser dataUser = new DataUser(userId, level);
                    cache.put(cacheKey, dataUser);
                    System.out.println("Пользователь добавлен в базу данных и кэш.");
                } else {
                    System.out.println("Ошибка: Не удалось добавить пользователя.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Ошибка при добавлении пользователя: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String selectPrefix(Long serverId){
        if(serverCache.equals(serverId)){
            return serverCache.get(serverId).getPrefix();
        }
        String selectPreifx = "SELECT prefix FROM servers WHERE server_id = ?";
        try(Connection connection = DataBaseConnect.getInstance().connection()) {
            try(PreparedStatement stmp = connection.prepareStatement(selectPreifx)) {
                stmp.setLong(1, serverId);
                try (ResultSet rs = stmp.executeQuery()){
                    if(rs.next()){
                        String prefix = rs.getString("prefix");

                        serverCache.put(serverId, new DataServer(serverId, prefix));

                        return prefix;
                    }
                }
            }
        } catch (SQLException e){
            log.error("Ошибка получения префикса: " + e.getMessage());
        }
        return null;
    }
    public static void updatePrefix(Long serverId, String prefix){

        String updatePrefix = ("UPDATE servers SET prefix = ? WHERE server_id = ?");
        try(Connection connection = DataBaseConnect.getInstance().connection()){
            try (PreparedStatement stmp = connection.prepareStatement(updatePrefix)){
                stmp.setString(1, prefix);
                stmp.setLong(2, serverId);
                int rowsAffect = stmp.executeUpdate();
                if(rowsAffect > 0){
                    DataServer dataServer = new DataServer(serverId, prefix);
                    serverCache.put(serverId, dataServer);
                }
            }
        } catch (SQLException e){
            log.error("Ошибка изменения префикса. " + e.getMessage());
        }
    }

}
