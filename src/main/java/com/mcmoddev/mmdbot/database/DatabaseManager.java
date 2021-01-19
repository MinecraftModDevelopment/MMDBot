package com.mcmoddev.mmdbot.database;

import com.mcmoddev.mmdbot.database.models.PersonModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:./data.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

//            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void insertPerson(PersonModel person) {
        String sql = "INSERT INTO PERSON(ID,NAME) VALUES(?,?)";

        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, person.getId());
            pstmt.setString(2, person.getName());
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<PersonModel> getPeople() {
        String sql = "SELECT * FROM PERSON";
        List<PersonModel> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new PersonModel(rs.getInt("id"), rs.getString("name")));
            }

            conn.close();

            return list;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
}
