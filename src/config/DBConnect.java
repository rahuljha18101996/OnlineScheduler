package config;

import constants.DBConstant;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    private Connection connection = null;
    private static DBConnect instace = new DBConnect();
    public static DBConnect getInstance() {
        return instace;
    }
    private DBConnect() {}
    public void connect() {
        try {
            Class.forName(DBConstant.DB_DRIVER);
            connection = DriverManager.getConnection(
                    DBConstant.DB_URL,
                    DBConstant.DB_USER,
                    DBConstant.DB_PASSWORD
            );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
