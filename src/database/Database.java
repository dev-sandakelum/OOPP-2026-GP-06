package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB = "afms_db";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB;
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws Exception{
        try {
            Connection connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println("Database connected successfully!");

            return connection;

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
        return null;
    }
}