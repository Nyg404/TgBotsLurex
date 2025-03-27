package io.github.Nyg404.Utils;


import io.github.Nyg404.Bd.DataServer;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class DataBaseManager {
    private static final ConcurrentHashMap<Long, DataServer> serverCache = new ConcurrentHashMap<>();

    

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
