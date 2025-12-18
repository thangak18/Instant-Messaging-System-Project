package user.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * AI Service - Tích hợp Google Gemini API để gợi ý tin nhắn
 * Sử dụng Gemini 1.5 Flash (miễn phí)
 */
public class AIService {
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String apiKey = loadApiKeyFromConfig();
    
    /**
     * Đọc Gemini API key từ release/config.properties
     */
    private static String loadApiKeyFromConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("release/config.properties")) {
            props.load(fis);
            String key = props.getProperty("gemini.api.key", "").trim();
            if (!key.isEmpty()) {
                System.out.println("✅ Gemini API key loaded from config.properties");
                return key;
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not load gemini.api.key from config.properties: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * Gợi ý tin nhắn dựa vào prompt của user
     */
    public String generateSuggestion(String userPrompt, String chatContext) {
        // Kiểm tra API key
        if (apiKey == null || apiKey.isEmpty()) {
            return generateOfflineSuggestion(userPrompt);
        }
        
        try {
            String systemPrompt = buildSystemPrompt(chatContext);
            String response = callGeminiAPI(systemPrompt, userPrompt);
            return response;
        } catch (Exception e) {
            System.err.println("❌ Lỗi gọi Gemini API: " + e.getMessage());
            e.printStackTrace();
            return generateOfflineSuggestion(userPrompt);
        }
    }
    
    /**
     * Tạo system prompt cho AI
     */
    private String buildSystemPrompt(String chatContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý AI giúp soạn tin nhắn chat tiếng Việt. ");
        sb.append("Hãy tạo tin nhắn ngắn gọn, tự nhiên, phù hợp với ngữ cảnh. ");
        sb.append("Chỉ trả lời nội dung tin nhắn, không giải thích thêm. ");
        sb.append("Có thể dùng emoji phù hợp. ");
        
        if (chatContext != null && !chatContext.isEmpty()) {
            sb.append("\n\nNgữ cảnh cuộc trò chuyện:\n").append(chatContext);
        }
        
        return sb.toString();
    }
    
    /**
     * Gọi Gemini API
     */
    private String callGeminiAPI(String systemPrompt, String userPrompt) throws Exception {
        URL url = new URL(API_URL + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        
        // Build JSON request body
        String jsonBody = buildRequestBody(systemPrompt, userPrompt);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return parseResponse(response.toString());
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                System.err.println("API Error: " + error.toString());
            }
            throw new Exception("API returned code: " + responseCode);
        }
    }
    
    /**
     * Build JSON request body cho Gemini API
     */
    private String buildRequestBody(String systemPrompt, String userPrompt) {
        // Escape special characters trong JSON
        String escapedSystem = escapeJson(systemPrompt);
        String escapedUser = escapeJson(userPrompt);
        
        return "{"
            + "\"contents\": [{"
            + "\"parts\": [{"
            + "\"text\": \"" + escapedSystem + "\\n\\nYêu cầu: " + escapedUser + "\""
            + "}]"
            + "}],"
            + "\"generationConfig\": {"
            + "\"temperature\": 0.7,"
            + "\"maxOutputTokens\": 256"
            + "}"
            + "}";
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    /**
     * Parse response từ Gemini API
     */
    private String parseResponse(String jsonResponse) {
        try {
            // Simple JSON parsing (không dùng thư viện)
            // Tìm "text": "..." trong response
            int textIndex = jsonResponse.indexOf("\"text\"");
            if (textIndex == -1) {
                return "Không thể tạo gợi ý. Vui lòng thử lại!";
            }
            
            int colonIndex = jsonResponse.indexOf(":", textIndex);
            int startQuote = jsonResponse.indexOf("\"", colonIndex + 1);
            int endQuote = findEndQuote(jsonResponse, startQuote + 1);
            
            if (startQuote == -1 || endQuote == -1) {
                return "Không thể tạo gợi ý. Vui lòng thử lại!";
            }
            
            String text = jsonResponse.substring(startQuote + 1, endQuote);
            // Unescape JSON
            return text
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
                
        } catch (Exception e) {
            e.printStackTrace();
            return "Không thể tạo gợi ý. Vui lòng thử lại!";
        }
    }
    
    /**
     * Tìm vị trí end quote (bỏ qua escaped quotes)
     */
    private int findEndQuote(String str, int startPos) {
        for (int i = startPos; i < str.length(); i++) {
            if (str.charAt(i) == '"' && str.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gợi ý offline khi không có API key
     */
    private String generateOfflineSuggestion(String prompt) {
        String lower = prompt.toLowerCase();
        
        // Xin lỗi
        if (lower.contains("xin lỗi") || lower.contains("sorry") || lower.contains("lỗi")) {
            return pickRandom(new String[]{
                "Mình thật sự xin lỗi về điều đó. Mình không cố ý, mong bạn thông cảm nhé! 🙏",
                "Mình xin lỗi bạn nhiều lắm. Lần sau mình sẽ cẩn thận hơn. Bạn tha lỗi cho mình nhé!",
                "Mình biết mình sai rồi, thành thật xin lỗi bạn. Hy vọng bạn không giận mình. 😔"
            });
        }
        
        // Cảm ơn
        if (lower.contains("cảm ơn") || lower.contains("thank") || lower.contains("biết ơn")) {
            return pickRandom(new String[]{
                "Cảm ơn bạn rất nhiều! Mình thực sự trân trọng sự giúp đỡ của bạn! 😊",
                "Thank you so much! Bạn tuyệt vời lắm! 🙏💕",
                "Mình không biết nói gì hơn ngoài lời cảm ơn chân thành. Bạn thật tốt! ❤️"
            });
        }
        
        // Chúc mừng
        if (lower.contains("chúc mừng") || lower.contains("congrat")) {
            return pickRandom(new String[]{
                "Chúc mừng bạn nhé! 🎉🎊 Mình thật sự vui cho thành công của bạn!",
                "Tuyệt vời quá! Chúc mừng bạn! Bạn xứng đáng được điều này! 🏆✨",
                "Wow! Chúc mừng nha! Cố lên, còn nhiều thành công nữa đang chờ bạn! 🎈🎁"
            });
        }
        
        // Sinh nhật
        if (lower.contains("sinh nhật") || lower.contains("birthday")) {
            return pickRandom(new String[]{
                "Chúc mừng sinh nhật bạn! 🎂🎉 Chúc bạn một tuổi mới thật nhiều niềm vui và hạnh phúc!",
                "Happy Birthday! 🎈🎁 Chúc bạn luôn vui vẻ, khỏe mạnh và gặp nhiều may mắn!",
                "Sinh nhật vui vẻ nhé! 🥳🎊 Hy vọng mọi ước mơ của bạn đều thành hiện thực!"
            });
        }
        
        // Hẹn gặp / Mời
        if (lower.contains("hẹn gặp") || lower.contains("meet") || lower.contains("mời") || lower.contains("invite")) {
            return pickRandom(new String[]{
                "Bạn có rảnh cuối tuần này không? Mình muốn mời bạn đi cà phê, lâu rồi không gặp! ☕",
                "Chúng ta hẹn gặp nhau lúc [thời gian] tại [địa điểm] nhé! Mình rất mong được gặp bạn! 🤝",
                "Mình muốn mời bạn tham gia [sự kiện]. Bạn đi cùng mình được không? 😊"
            });
        }
        
        // Hỏi thăm
        if (lower.contains("hỏi thăm") || lower.contains("sức khỏe") || lower.contains("khỏe không")) {
            return pickRandom(new String[]{
                "Lâu rồi không gặp, dạo này bạn có khỏe không? Công việc/học tập thế nào rồi? 😊",
                "Hey! Mình nhớ bạn quá. Bạn dạo này sao rồi? Có gì vui kể mình nghe với!",
                "Hi bạn! Hôm nay bạn thế nào? Hy vọng mọi thứ đều ổn với bạn nhé! 💪"
            });
        }
        
        // Từ chối lịch sự
        if (lower.contains("từ chối") || lower.contains("không đi được") || lower.contains("bận")) {
            return pickRandom(new String[]{
                "Cảm ơn bạn đã mời, nhưng tiếc quá mình có việc bận rồi. Hẹn dịp khác nhé! 😅",
                "Mình rất muốn đi nhưng lịch mình hôm đó kín rồi. Lần sau nhất định mình sẽ có mặt! 🙏",
                "Xin lỗi bạn, mình không thể tham gia được. Hy vọng các bạn vui vẻ nhé! 💕"
            });
        }
        
        // Động viên
        if (lower.contains("động viên") || lower.contains("an ủi") || lower.contains("buồn")) {
            return pickRandom(new String[]{
                "Đừng buồn nữa bạn ơi! Mọi chuyện rồi sẽ ổn thôi. Mình luôn ở đây nếu bạn cần! 💪❤️",
                "Cố lên bạn nhé! Sau cơn mưa trời lại sáng. Bạn mạnh mẽ hơn bạn nghĩ đấy! ⭐",
                "Mình hiểu cảm giác của bạn. Hãy nhớ rằng bạn không đơn độc, mình luôn ủng hộ bạn! 🤗"
            });
        }
        
        // Xác nhận
        if (lower.contains("ok") || lower.contains("đồng ý") || lower.contains("xác nhận")) {
            return pickRandom(new String[]{
                "OK bạn! Mình đã ghi nhận rồi nhé! 👍",
                "Được thôi, không vấn đề gì! ✅",
                "Okie, mình hiểu rồi! Cứ yên tâm nhé! 👌"
            });
        }
        
        // Làm quen
        if (lower.contains("làm quen") || lower.contains("kết bạn") || lower.contains("chào")) {
            return pickRandom(new String[]{
                "Xin chào! Mình là [tên]. Rất vui được làm quen với bạn! 😊👋",
                "Hi! Mình thấy profile bạn hay quá nên muốn làm quen. Hy vọng được trò chuyện với bạn!",
                "Chào bạn! Chúng ta có thể kết bạn được không? Mình rất muốn được biết thêm về bạn! 🤝"
            });
        }
        
        // Default
        return pickRandom(new String[]{
            "Dựa vào yêu cầu \"" + prompt + "\":\n\nMình hiểu ý bạn rồi. Chúng ta có thể thảo luận thêm về vấn đề này nhé! 😊",
            "Cảm ơn bạn đã chia sẻ! Mình sẽ suy nghĩ về điều này và phản hồi sớm nhé! 🤔",
            "OK mình nhận được rồi! Để mình xem xét và trả lời bạn sau nhé! 👍"
        });
    }
    
    /**
     * Chọn ngẫu nhiên 1 phần tử từ mảng
     */
    private String pickRandom(String[] options) {
        int index = (int) (Math.random() * options.length);
        return options[index];
    }
    
    /**
     * Kiểm tra API key đã được cấu hình chưa
     */
    public boolean isAPIConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
