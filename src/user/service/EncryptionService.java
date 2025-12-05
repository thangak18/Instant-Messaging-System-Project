package user.service;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service mã hóa đầu cuối (End-to-End Encryption) cho nhóm chat
 * 
 * Đặc điểm:
 * - Sử dụng AES-256-GCM để mã hóa tin nhắn
 * - Mỗi nhóm có một khóa mã hóa riêng (Group Key)
 * - Khóa được lưu cục bộ trên client, KHÔNG GỬI LÊN SERVER
 * - Server chỉ lưu trữ tin nhắn đã mã hóa, không thể giải mã
 * 
 * Lưu ý: Đây là mã hóa E2E thực sự - ngay cả server cũng không thể đọc tin nhắn
 */
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;  // AES-256
    private static final int GCM_IV_LENGTH = 12;  // 96 bits
    private static final int GCM_TAG_LENGTH = 128;  // 128 bits auth tag
    
    // Cache lưu trữ khóa nhóm cục bộ (groupId -> groupKey)
    // Trong thực tế, khóa này sẽ được lưu an toàn trong Keychain/Keystore của device
    private static final Map<Integer, SecretKey> groupKeyCache = new ConcurrentHashMap<>();
    
    // Cache lưu trữ group key dạng Base64 để chia sẻ với thành viên mới
    private static final Map<Integer, String> groupKeyBase64Cache = new ConcurrentHashMap<>();
    
    private static EncryptionService instance;
    
    public static EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }
    
    /**
     * TẠO KHÓA MỚI CHO NHÓM MÃ HÓA
     * Khóa này chỉ được tạo 1 lần khi nhóm được tạo
     * 
     * @param groupId ID của nhóm
     * @return Base64 encoded key để chia sẻ với thành viên
     */
    public String generateGroupKey(int groupId) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            
            // Lưu vào cache
            groupKeyCache.put(groupId, secretKey);
            
            // Encode thành Base64 để lưu trữ/chia sẻ
            String keyBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            groupKeyBase64Cache.put(groupId, keyBase64);
            
            System.out.println("🔐 Đã tạo khóa mã hóa cho nhóm " + groupId);
            
            return keyBase64;
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Lỗi tạo khóa mã hóa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * LOAD KHÓA NHÓM TỪ BASE64 STRING
     * Được gọi khi user tham gia nhóm mã hóa đã có sẵn
     * 
     * @param groupId ID nhóm
     * @param keyBase64 Khóa dạng Base64
     * @return true nếu load thành công
     */
    public boolean loadGroupKey(int groupId, String keyBase64) {
        if (keyBase64 == null || keyBase64.isEmpty()) {
            return false;
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            groupKeyCache.put(groupId, secretKey);
            groupKeyBase64Cache.put(groupId, keyBase64);
            
            System.out.println("🔓 Đã load khóa mã hóa cho nhóm " + groupId);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi load khóa: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * LẤY KHÓA NHÓM DẠNG BASE64
     * Dùng để chia sẻ cho thành viên mới
     * 
     * @param groupId ID nhóm
     * @return Base64 encoded key hoặc null
     */
    public String getGroupKeyBase64(int groupId) {
        return groupKeyBase64Cache.get(groupId);
    }
    
    /**
     * KIỂM TRA ĐÃ CÓ KHÓA NHÓM CHƯA
     */
    public boolean hasGroupKey(int groupId) {
        return groupKeyCache.containsKey(groupId);
    }
    
    /**
     * MÃ HÓA TIN NHẮN
     * Tin nhắn sẽ được mã hóa với khóa của nhóm
     * 
     * @param groupId ID nhóm
     * @param plainText Tin nhắn gốc
     * @return Tin nhắn đã mã hóa (Base64) hoặc null nếu lỗi
     */
    public String encryptMessage(int groupId, String plainText) {
        SecretKey key = groupKeyCache.get(groupId);
        if (key == null) {
            System.err.println("❌ Không tìm thấy khóa cho nhóm " + groupId);
            return null;
        }
        
        try {
            // Tạo IV ngẫu nhiên
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Cấu hình cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            
            // Mã hóa
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            // Ghép IV + cipherText
            byte[] combined = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, combined, GCM_IV_LENGTH, cipherText.length);
            
            // Encode Base64
            String encrypted = Base64.getEncoder().encodeToString(combined);
            
            System.out.println("🔒 Đã mã hóa tin nhắn cho nhóm " + groupId);
            return encrypted;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi mã hóa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * GIẢI MÃ TIN NHẮN
     * Chỉ có thể giải mã nếu có khóa nhóm
     * 
     * @param groupId ID nhóm
     * @param encryptedText Tin nhắn đã mã hóa (Base64)
     * @return Tin nhắn gốc hoặc "[Không thể giải mã]" nếu lỗi
     */
    public String decryptMessage(int groupId, String encryptedText) {
        SecretKey key = groupKeyCache.get(groupId);
        if (key == null) {
            System.err.println("❌ Không tìm thấy khóa cho nhóm " + groupId);
            return "🔒 [Không thể giải mã - Thiếu khóa]";
        }
        
        try {
            // Decode Base64
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // Tách IV và cipherText
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            
            // Cấu hình cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            // Giải mã
            byte[] plainText = cipher.doFinal(cipherText);
            
            return new String(plainText, "UTF-8");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi giải mã: " + e.getMessage());
            return "🔒 [Không thể giải mã]";
        }
    }
    
    /**
     * XÓA KHÓA NHÓM KHỎI CACHE
     * Gọi khi user rời nhóm hoặc logout
     */
    public void removeGroupKey(int groupId) {
        groupKeyCache.remove(groupId);
        groupKeyBase64Cache.remove(groupId);
        System.out.println("🗑️ Đã xóa khóa nhóm " + groupId + " khỏi cache");
    }
    
    /**
     * XÓA TẤT CẢ KHÓA
     * Gọi khi user logout
     */
    public void clearAllKeys() {
        groupKeyCache.clear();
        groupKeyBase64Cache.clear();
        System.out.println("🗑️ Đã xóa tất cả khóa mã hóa");
    }
    
    /**
     * HASH MẬT KHẨU VỚI SALT (utility function)
     */
    public String hashWithSalt(String input, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hash = md.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    /**
     * TẠO SALT NGẪU NHIÊN
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
