package db;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = util.EnvLoader.getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/marketdb");
    private static final String USER = util.EnvLoader.getOrDefault("DB_USER", "postgres");
    private static final String PASSWORD = util.EnvLoader.getOrDefault("DB_PASSWORD", "");

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println("Bağlantı Hatası: " + e.getMessage());
        }
        return conn;
    }

    public static void logEkle(String kullanici, String islem) {
        try (Connection conn = connect()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO logs (kullanici, islem) VALUES (?, ?)")) {
                pstmt.setString(1, kullanici);
                pstmt.setString(2, islem);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Log kaydedilemedi: " + e.getMessage());
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn == null) return;
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "personel", null);
            if (!tables.next()) {
                System.out.println("Veritabanı tabloları eksik, kurulum başlatılıyor...");
                java.nio.file.Path path = java.nio.file.Paths.get("database_setup.txt");
                String sql = new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("Veritabanı kurulumu başarıyla tamamlandı.");
                }
            }
        } catch (Exception e) {
            System.err.println("Veritabanı ilklendirme hatası: " + e.getMessage());
        }
    }
}
