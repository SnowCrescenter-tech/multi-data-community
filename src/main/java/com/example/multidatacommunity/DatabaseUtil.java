package com.example.multidatacommunity;

import java.sql.*;

public class DatabaseUtil {
    // SQLite数据库文件URL
    private static final String DB_URL = "jdbc:sqlite:chat_history.db";

    /**
     * 初始化数据库
     * - 创建聊天记录表
     * - 创建音频记录表
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            // 创建聊天记录表：包含ID、消息内容和时间戳
            String sql = "CREATE TABLE IF NOT EXISTS chat_history (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," + // 自增主键
                         "message TEXT NOT NULL," +               // 消息内容
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"; // 自动记录时间戳
            stmt.execute(sql);

            // 创建音频记录表：存储Base64编码的音频数据
            String audioSql = "CREATE TABLE IF NOT EXISTS audio_history (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                              "audio BLOB NOT NULL," +           // 使用BLOB类型存储音频数据
                              "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(audioSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存消息到数据库
     * 实现步骤：
     * 1. 准备INSERT SQL语句
     * 2. 创建PreparedStatement避免SQL注入
     * 3. 设置参数并执行插入
     * 4. 自动关闭数据库连接（使用try-with-resources）
     */
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

    /**
     * 保存音频数据到数据库
     * 实现步骤：
     * 1. 将Base64编码的音频数据作为BLOB存储
     * 2. 使用PreparedStatement设置大文本数据
     * 3. 自动生成时间戳记录保存时间
     */
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

    /**
     * 根据ID查询音频数据
     * 实现步骤：
     * 1. 准备SELECT SQL语句
     * 2. 使用PreparedStatement设置查询参数
     * 3. 获取结果集并提取音频数据
     * 4. 自动关闭数据库资源
     */
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