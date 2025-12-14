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

    public static ImageIcon getIcon(String urlStr, int size) {
        String key = urlStr + "_" + size;
        if (cache.containsKey(key)) return cache.get(key);
        try {
            URL url = new URI(urlStr).toURL();
            BufferedImage raw = ImageIO.read(url);
            Image sc = raw.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(sc);
            cache.put(key, icon);
            return icon;
        } catch (Exception e) { return createFallback(size); }
    }
    
    // Add preload method here if you wish, or handle it dynamically
    public static void preload() { /* List your URLs here to load them early */ }

    private static ImageIcon createFallback(int size) {
        BufferedImage img = new BufferedImage(size, size, 2);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.MAGENTA);
        g2.fillRect(0,0,size,size);
        g2.dispose();
        return new ImageIcon(img);
    }
}