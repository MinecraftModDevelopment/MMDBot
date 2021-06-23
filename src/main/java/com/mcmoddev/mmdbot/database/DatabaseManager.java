package com.mcmoddev.mmdbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The type Database manager.
 *
 * @author Antoine Gagnon
 */
public class DatabaseManager {

    /**
     * Gets connection.
     *
     * @return the connection
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:./data.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
