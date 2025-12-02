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
            
            // Kiểm tra xem config có placeholder không
            if (url == null || url.contains("YOUR_PROJECT_REF") || url.contains("YOUR_")) {
                throw new IOException("❌ Database configuration chưa được cấu hình!\n" +
                    "Vui lòng cập nhật file release/config.properties với thông tin Supabase thực tế.\n" +
                    "Chạy: ./configure_db.sh để cấu hình tự động.");
            }
            
            if (password == null || password.contains("YOUR_PASSWORD") || password.contains("YOUR_")) {
                throw new IOException("❌ Database password chưa được cấu hình!\n" +
                    "Vui lòng cập nhật db.password trong file release/config.properties.\n" +
                    "Chạy: ./configure_db.sh để cấu hình tự động.");
            }
            
            if (url == null || username == null || password == null) {
                throw new IOException("❌ Thiếu thông tin cấu hình database!\n" +
                    "Cần có: db.url, db.username, db.password trong file release/config.properties");
            }
            
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
        // Kiểm tra config trước khi kết nối
        if (url == null || url.contains("YOUR_") || password == null || password.contains("YOUR_")) {
            throw new SQLException("Database configuration chưa được cấu hình. " +
                "Vui lòng cập nhật file release/config.properties hoặc chạy ./configure_db.sh");
        }
        
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("[Admin] Database connection established");
            return conn;
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            System.err.println("[Admin] Failed to connect to database: " + errorMsg);
            
            // Đưa ra gợi ý cụ thể dựa trên lỗi
            if (errorMsg != null) {
                if (errorMsg.contains("password authentication failed")) {
                    throw new SQLException("❌ Sai mật khẩu database!\n" +
                        "Vui lòng kiểm tra lại db.password trong config.properties", e);
                } else if (errorMsg.contains("No route to host") || errorMsg.contains("Connection refused")) {
                    throw new SQLException("❌ Không thể kết nối đến database!\n" +
                        "Kiểm tra:\n" +
                        "1. Project Reference có đúng không?\n" +
                        "2. Supabase project có bị pause không? (Resume trong Dashboard)\n" +
                        "3. Network/Firewall có chặn không?", e);
                } else if (errorMsg.contains("The connection attempt failed")) {
                    throw new SQLException("❌ Kết nối thất bại!\n" +
                        "Kiểm tra:\n" +
                        "1. File config.properties đã được cấu hình đúng chưa?\n" +
                        "2. Chạy: ./configure_db.sh để cấu hình\n" +
                        "3. Hoặc kiểm tra Supabase Dashboard xem project có đang hoạt động không", e);
                }
            }
            
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
