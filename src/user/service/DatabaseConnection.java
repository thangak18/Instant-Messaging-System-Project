package user.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;
    
    private DatabaseConnection() {
        try {
            loadConfig();
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL Driver loaded!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadConfig() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("release/config.properties")) {
            props.load(fis);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
            System.out.println("Config loaded - URL: " + url);
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Testing Supabase connection...");
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            System.out.println("SUCCESS! Connected to Supabase!");
            conn.close();
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}