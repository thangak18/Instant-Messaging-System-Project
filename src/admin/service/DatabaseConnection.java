package admin.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton class quản lý kết nối database cho Admin module
 * Hỗ trợ cả Supabase PostgreSQL và MySQL
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    private DatabaseConnection() {
        try {
            loadConfig();
            // Load driver động dựa vào config
            Class.forName(driver);
            System.out.println("[Admin] Database Driver loaded: " + driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[Admin] Database driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[Admin] Error initializing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load cấu hình từ config.properties
     */
    private void loadConfig() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("release/config.properties")) {
            props.load(fis);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
            this.driver = props.getProperty("db.driver", "org.postgresql.Driver");
            
            System.out.println("[Admin] Config loaded successfully");
            System.out.println("[Admin] Database URL: " + url);
            System.out.println("[Admin] Database User: " + username);
        } catch (IOException e) {
            System.err.println("[Admin] Failed to load config.properties: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Lấy instance singleton
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Lấy connection tới database
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("[Admin] Database connection established");
            return conn;
        } catch (SQLException e) {
            System.err.println("[Admin] Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Đóng connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[Admin] Database connection closed");
            } catch (SQLException e) {
                System.err.println("[Admin] Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[Admin] Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reload configuration (useful khi đổi database)
     */
    public void reloadConfig() {
        try {
            loadConfig();
            System.out.println("[Admin] Configuration reloaded successfully");
        } catch (IOException e) {
            System.err.println("[Admin] Failed to reload configuration: " + e.getMessage());
        }
    }
    
    // Getters
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getDriver() {
        return driver;
    }
    
    /**
     * Main method để test connection
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Testing Admin Database Connection...");
        System.out.println("========================================");
        
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        
        if (dbConn.testConnection()) {
            System.out.println("✅ Connection test SUCCESSFUL!");
            System.out.println("Database: " + dbConn.getUrl());
            System.out.println("Driver: " + dbConn.getDriver());
        } else {
            System.out.println("❌ Connection test FAILED!");
            System.out.println("Please check your config.properties file");
        }
        
        System.out.println("========================================");
    }
}
