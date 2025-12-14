package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ImageLoader {
    private static Map<String, ImageIcon> cache = new HashMap<>();

    public static ImageIcon getIcon(String urlStr, int size, String fallbackEmoji) {
        String key = urlStr + "_" + size;
        if (cache.containsKey(key)) return cache.get(key);
        
        try {
            // Attempt to download image
            URL url = new URI(urlStr).toURL();
            BufferedImage raw = ImageIO.read(url);
            
            ImageIcon finalIcon;
            if (size <= 0) {
                finalIcon = new ImageIcon(raw);
            } else {
                Image sc = raw.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                finalIcon = new ImageIcon(sc);
            }
            cache.put(key, finalIcon);
            return finalIcon;

        } catch (Exception e) {
            // Download failed? Generate High-Contrast Emoji
            ImageIcon fallback = createEmojiIcon(fallbackEmoji, size);
            cache.put(key, fallback);
            return fallback;
        }
    }

    private static ImageIcon createEmojiIcon(String emoji, int size) {
        int safeSize = (size <= 0) ? 64 : size;
        BufferedImage img = new BufferedImage(safeSize, safeSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        
        // High quality text rendering
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate center position
        g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int)(safeSize * 0.75)));
        FontMetrics fm = g2.getFontMetrics();
        int x = (safeSize - fm.stringWidth(emoji)) / 2;
        int y = ((safeSize - fm.getHeight()) / 2) + fm.getAscent();
        
        // DRAW SHADOW (Dark Grey) - Offset by 2 pixels
        g2.setColor(new Color(50, 50, 50, 180));
        g2.drawString(emoji, x + 2, y + 2);
        
        // DRAW MAIN TEXT (White/Bright) - On top
        // This ensures it is visible on Dark Backgrounds (White pops)
        // And visible on Light Backgrounds (Shadow pops)
        g2.setColor(new Color(255, 255, 255)); 
        g2.drawString(emoji, x, y);
        
        g2.dispose();
        return new ImageIcon(img);
    }
}