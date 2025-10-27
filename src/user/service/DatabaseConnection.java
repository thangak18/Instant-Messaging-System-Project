package user.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối cơ sở dữ liệu MySQL
 * Đọc cấu hình từ release/config.properties
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    private DatabaseConnection() {
        loadConfiguration();
    }
    
    /**
     * Singleton pattern - chỉ có 1 instance duy nhất
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
     * Đọc cấu hình từ file config.properties
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("release/config.properties")) {
            props.load(fis);
            
            this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password", "");
            
            // Load driver
            Class.forName(this.driver);
            
            System.out.println("✅ Database configuration loaded successfully");
            System.out.println("   URL: " + url);
            System.out.println("   Username: " + username);
            System.out.println("   Password: " + (password.isEmpty() ? "(empty)" : "***"));
            
        } catch (IOException e) {
            System.err.println("❌ ERROR: Không thể đọc file config.properties");
            e.printStackTrace();
            
            // Fallback to default values
            setDefaultConfiguration();
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: Không tìm thấy MySQL JDBC Driver");
            System.err.println("   Hướng dẫn: Thêm mysql-connector-java.jar vào classpath");
            e.printStackTrace();
        }
    }
    
    /**
     * Cấu hình mặc định nếu không đọc được file
     */
    private void setDefaultConfiguration() {
        this.driver = "com.mysql.cj.jdbc.Driver";
        this.url = "jdbc:mysql://localhost:3306/chat_system?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
        this.username = "root";
        this.password = "";
        
        System.out.println("⚠️  Using default database configuration");
    }
    
    /**
     * Lấy connection mới từ database
     * @return Connection object hoặc null nếu lỗi
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to database successfully");
            return conn;
            
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Không thể kết nối database");
            System.err.println("   Chi tiết: " + e.getMessage());
            
            // Hướng dẫn khắc phục
            System.err.println("\n📌 HƯỚNG DẪN KHẮC PHỤC:");
            System.err.println("   1. Kiểm tra MySQL Server đang chạy");
            System.err.println("   2. Chạy script: script/database/create_database.sql");
            System.err.println("   3. Kiểm tra username/password trong config.properties");
            System.err.println("   4. Kiểm tra port 3306 có đang dùng không");
            
            return null;
        }
    }
    
    /**
     * Test kết nối database
     * @return true nếu kết nối thành công
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test: SUCCESS");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test: FAILED");
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Đóng connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Connection closed");
            } catch (SQLException e) {
                System.err.println("❌ Error closing connection: " + e.getMessage());
            }
        }
    }
}
