package com.example.multidatacommunity;

import java.sql.*;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:chat_history.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS chat_history (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "message TEXT NOT NULL," +
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);

            String audioSql = "CREATE TABLE IF NOT EXISTS audio_history (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                              "audio BLOB NOT NULL," +
                              "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(audioSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveMessage(String message) {
        String sql = "INSERT INTO chat_history (message) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveAudio(String base64Audio) {
        String sql = "INSERT INTO audio_history (audio) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, base64Audio);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getAudio(int id) {
        String sql = "SELECT audio FROM audio_history WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("audio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}