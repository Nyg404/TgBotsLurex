package io.github.Nyg404.Utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DataBaseConnect {
    private static final String url = "jdbc:postgresql://localhost:5432/bib1";
    private static final String user = "postgres";
    private static final String password = "7777";
    private static Connection connection;

    private static volatile DataBaseConnect instance;

    // Статический блок инициализации синглтона
    static {
        instance = new DataBaseConnect();
    }

    // Метод для получения экземпляра синглтона с использованием double-checked locking
    public static DataBaseConnect getInstance() {
        if (instance == null) {
            synchronized (DataBaseConnect.class) {
                if (instance == null) {
                    instance = new DataBaseConnect();
                }
            }
        }
        return instance;
    }

    // Приватный конструктор
    private DataBaseConnect() {
        // Инициализируем соединение
        connection = connection();
    }

    // Метод для получения соединения с базой данных
    public Connection connection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Метод для закрытия соединения с базой данных
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // SQL-запрос для создания таблиц
    public static void createTables() {
        String createServersTable = "CREATE TABLE IF NOT EXISTS servers ("
                + "server_id BIGINT PRIMARY KEY, "
                + "prefix VARCHAR(50) NOT NULL, "
                + "server_name VARCHAR(100));";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id BIGINT, "
                + "server_id BIGINT, "
                + "level INT DEFAULT 1, "
                + "permissionlevel INT DEFAULT 0, "
                + "PRIMARY KEY (user_id, server_id), "
                + "FOREIGN KEY (server_id) REFERENCES servers(server_id));";

        String createServerPermisson = "CREATE TABLE IF NOT EXISTS servers_permission ("
                + "server_id BIGINT PRIMARY KEY,"
                + "setprefix INT DEFAULT 3,"
                + "FOREIGN KEY (server_id) REFERENCES servers(server_id));";

        try (Connection conn = getInstance().connection(); 
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createServersTable); // Создание таблицы servers
            stmt.executeUpdate(createUsersTable); 
            stmt.executeUpdate(createServerPermisson);  // Создание таблицы users
            log.info("Таблицы успешно созданы!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
