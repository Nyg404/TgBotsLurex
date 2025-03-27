package io.github.Nyg404.Utils;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Db {
    private static final String url = "jdbc:postgresql://localhost:5432/bib";
    private static final String user = "postgres";
    private static final String password = "7777";
    private static Connection connection;

    @Getter
    private static Db instance = null;

    static {
        instance = new Db();
    }

    private Db(){

    }

    public static Connection getConnection() throws SQLException{
        if(connection != null && connection.isClosed()){
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // SQL-запрос для создания таблицы servers
    public static void createTables() {
        String createServersTable = "CREATE TABLE IF NOT EXISTS servers ("
                + "server_id BIGINT PRIMARY KEY, "
                + "prefix VARCHAR(50) NOT NULL, "
                + "server_name VARCHAR(100));";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id BIGINT PRIMARY KEY, "
                + "server_id BIGINT, "
                + "username VARCHAR(100), "
                + "level INT DEFAULT 1, "
                + "balance INT DEFAULT 0, "
                + "FOREIGN KEY (server_id) REFERENCES servers(server_id));";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createServersTable); // Выполнение создания таблицы servers
            stmt.executeUpdate(createUsersTable);   // Выполнение создания таблицы users
            System.out.println("Таблицы успешно созданы!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
