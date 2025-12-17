package user.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class để hỗ trợ giao diện cross-platform (Windows & macOS)
 */
public class UIHelper {
    
    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    
    // Font mặc định cho từng hệ điều hành
    private static final String DEFAULT_FONT = IS_MAC ? "SF Pro Display" : "Segoe UI";
    private static final String EMOJI_FONT = IS_MAC ? "Apple Color Emoji" : "Segoe UI Emoji";
    private static final String FALLBACK_FONT = "Arial";
    
    /**
     * Thiết lập Look and Feel phù hợp với hệ điều hành
     */
    public static void setupLookAndFeel() {
        try {
            // Sử dụng system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Fix cho macOS buttons
            if (IS_MAC) {
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("Button.background", Color.WHITE);
            }
            
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Lấy font phù hợp với hệ điều hành
     */
    public static Font getFont(int style, int size) {
        // Kiểm tra font có tồn tại không
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        
        for (String fontName : fontNames) {
            if (fontName.equals(DEFAULT_FONT)) {
                return new Font(DEFAULT_FONT, style, size);
            }
        }
        
        // Fallback font
        return new Font(FALLBACK_FONT, style, size);
    }
    
    /**
     * Tạo button primary với style cross-platform
     */
    public static JButton createPrimaryButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(getFont(Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setPreferredSize(new Dimension(300, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Cross-platform settings
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        // macOS specific
        if (IS_MAC) {
            button.putClientProperty("JButton.buttonType", "roundRect");
        }
        
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    /**
     * Tạo button secondary với style cross-platform
     */
    public static JButton createSecondaryButton(String text, Color borderColor) {
        JButton button = new JButton(text);
        button.setFont(getFont(Font.PLAIN, 14));
        button.setForeground(borderColor);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Cross-platform settings
        button.setOpaque(true);
        button.setFocusPainted(false);
        
        if (IS_MAC) {
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.setBorderPainted(true);
        }
        
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        return button;
    }
    
    /**
     * Tạo link button với style cross-platform
     */
    public static JButton createLinkButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(getFont(Font.PLAIN, 12));
        button.setForeground(color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (IS_MAC) {
            button.putClientProperty("JButton.buttonType", "text");
        }
        
        return button;
    }
    
    /**
     * Tạo text field với style cross-platform
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(getFont(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }
    
    /**
     * Tạo password field với style cross-platform
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(getFont(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }
    
    /**
     * Kiểm tra hệ điều hành
     */
    public static boolean isMac() {
        return IS_MAC;
    }
    
    public static boolean isWindows() {
        return IS_WINDOWS;
    }
    
    /**
     * Lấy tên font mặc định
     */
    public static String getDefaultFontName() {
        return DEFAULT_FONT;
    }
    
    /**
     * Lấy tên font emoji
     */
    public static String getEmojiFontName() {
        return EMOJI_FONT;
    }
}
