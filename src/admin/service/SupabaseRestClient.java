package admin.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * REST Client để gọi Supabase API thay vì direct database connection
 * Giải quyết vấn đề IPv6 incompatibility
 */
public class SupabaseRestClient {
    private static SupabaseRestClient instance;
    private String supabaseUrl;
    private String apiKey;
    private String serviceKey;
    
    // Timeout settings
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds
    
    private SupabaseRestClient() {
        loadConfig();
    }
    
    private void loadConfig() {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("release/config.properties")) {
                props.load(fis);
                this.supabaseUrl = props.getProperty("supabase.url");
                this.apiKey = props.getProperty("supabase.anon.key");
                this.serviceKey = props.getProperty("supabase.service.key");
                
                System.out.println("[Supabase REST] Config loaded");
                System.out.println("[Supabase REST] URL: " + supabaseUrl);
            }
        } catch (IOException e) {
            System.err.println("[Supabase REST] Failed to load config: " + e.getMessage());
        }
    }
    
    public static SupabaseRestClient getInstance() {
        if (instance == null) {
            synchronized (SupabaseRestClient.class) {
                if (instance == null) {
                    instance = new SupabaseRestClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * GET request - Lấy dữ liệu từ table
     * @param table Tên table (vd: "users", "login_history")
     * @param query Query parameters (vd: "select=*&limit=10")
     * @return JSON response string
     */
    public String get(String table, String query) throws IOException {
        String endpoint = supabaseUrl + "/rest/v1/" + table;
        if (query != null && !query.isEmpty()) {
            endpoint += "?" + query;
        }
        
        System.out.println("[Supabase REST] GET: " + endpoint);
        
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        System.out.println("[Supabase REST] Response code: " + responseCode);
        
        if (responseCode == 200) {
            return readResponse(conn);
        } else {
            String error = readError(conn);
            throw new IOException("GET failed [" + responseCode + "]: " + error);
        }
    }
    
    /**
     * POST request - Thêm dữ liệu mới
     * @param table Tên table
     * @param jsonBody JSON body
     * @return JSON response
     */
    public String post(String table, String jsonBody) throws IOException {
        String endpoint = supabaseUrl + "/rest/v1/" + table;
        
        System.out.println("[Supabase REST] POST: " + endpoint);
        System.out.println("[Supabase REST] Body: " + jsonBody);
        
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=representation");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        System.out.println("[Supabase REST] Response code: " + responseCode);
        
        if (responseCode == 201 || responseCode == 200) {
            return readResponse(conn);
        } else {
            String error = readError(conn);
            throw new IOException("POST failed [" + responseCode + "]: " + error);
        }
    }
    
    /**
     * PATCH request - Cập nhật dữ liệu
     */
    public String patch(String table, String query, String jsonBody) throws IOException {
        String endpoint = supabaseUrl + "/rest/v1/" + table + "?" + query;
        
        System.out.println("[Supabase REST] PATCH: " + endpoint);
        
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=representation");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        System.out.println("[Supabase REST] Response code: " + responseCode);
        
        if (responseCode == 200 || responseCode == 204) {
            return readResponse(conn);
        } else {
            String error = readError(conn);
            throw new IOException("PATCH failed [" + responseCode + "]: " + error);
        }
    }
    
    /**
     * DELETE request - Xóa dữ liệu
     */
    public String delete(String table, String query) throws IOException {
        String endpoint = supabaseUrl + "/rest/v1/" + table + "?" + query;
        
        System.out.println("[Supabase REST] DELETE: " + endpoint);
        
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        
        int responseCode = conn.getResponseCode();
        System.out.println("[Supabase REST] Response code: " + responseCode);
        
        if (responseCode == 204 || responseCode == 200) {
            return "Delete successful";
        } else {
            String error = readError(conn);
            throw new IOException("DELETE failed [" + responseCode + "]: " + error);
        }
    }
    
    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    private String readError(HttpURLConnection conn) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return "Unable to read error: " + e.getMessage();
        }
    }
    
    /**
     * Test connection với Supabase
     */
    public boolean testConnection() {
        System.out.println("[Supabase REST] Testing connection...");
        
        try {
            // Thử gọi endpoint health check hoặc list tables
            String response = get("users", "select=id&limit=1");
            System.out.println("[Supabase REST] Response: " + response);
            System.out.println("[Supabase REST] ✅ Connection successful!");
            return true;
        } catch (IOException e) {
            System.err.println("[Supabase REST] ❌ Connection failed: " + e.getMessage());
            
            // Kiểm tra lỗi cụ thể
            if (e.getMessage().contains("404")) {
                System.err.println("\n⚠️  Table 'users' not found!");
                System.err.println("You need to create tables in Supabase:");
                System.err.println("1. Go to Supabase Dashboard → SQL Editor");
                System.err.println("2. Run script: script/database/create_database_supabase.sql\n");
            } else if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                System.err.println("\n⚠️  Authentication failed!");
                System.err.println("Check your API keys in config.properties\n");
            } else if (e.getMessage().contains("timeout")) {
                System.err.println("\n⚠️  Connection timeout!");
                System.err.println("Check your network connection\n");
            }
            
            return false;
        }
    }
    
    /**
     * Ping test - chỉ kiểm tra URL có accessible không
     */
    public boolean pingSupabase() {
        System.out.println("[Supabase REST] Pinging Supabase...");
        
        try {
            URL url = new URL(supabaseUrl + "/rest/v1/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", apiKey);
            
            int responseCode = conn.getResponseCode();
            System.out.println("[Supabase REST] Ping response: " + responseCode);
            
            if (responseCode == 200 || responseCode == 404 || responseCode == 401) {
                // 200 = OK, 404 = not found (but reachable), 401 = unauthorized (but reachable)
                System.out.println("[Supabase REST] ✅ Supabase is reachable!");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("[Supabase REST] ❌ Ping failed: " + e.getMessage());
            return false;
        }
    }
    
    public String getSupabaseUrl() {
        return supabaseUrl;
    }
    
    /**
     * Main method để test
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Testing Supabase REST API Connection...");
        System.out.println("========================================\n");
        
        SupabaseRestClient client = SupabaseRestClient.getInstance();
        
        // Test 1: Ping Supabase
        System.out.println("Test 1: Ping Supabase URL");
        System.out.println("----------------------------------------");
        boolean pingOk = client.pingSupabase();
        System.out.println();
        
        if (!pingOk) {
            System.out.println("❌ Cannot reach Supabase!");
            System.out.println("Possible reasons:");
            System.out.println("  - No internet connection");
            System.out.println("  - Firewall blocking HTTPS");
            System.out.println("  - Wrong Supabase URL in config\n");
            System.out.println("========================================");
            return;
        }
        
        // Test 2: Query users table
        System.out.println("Test 2: Query 'users' table");
        System.out.println("----------------------------------------");
        boolean tableOk = client.testConnection();
        System.out.println();
        
        if (tableOk) {
            System.out.println("========================================");
            System.out.println("✅ ALL TESTS PASSED!");
            System.out.println("Supabase REST API is ready to use!");
            System.out.println("========================================");
        } else {
            System.out.println("========================================");
            System.out.println("⚠️  Supabase reachable but tables not setup");
            System.out.println("Please create tables in SQL Editor");
            System.out.println("========================================");
        }
    }
}
