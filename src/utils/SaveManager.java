package utils;

import core.Wallet;
import core.PrestigeManager;
import generators.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.awt.Color;

public class SaveManager {
    // NEW: It now looks inside the 'profiles' folder!
    private static final String PROFILE_FOLDER = "profiles/";

    // We need to pass the game state in because variables are no longer global
    public static void saveGame(String profileName, Wallet wallet, List<Generator> inventory, String themeName) {
        try {
            // Ensure folder exists
            new File(PROFILE_FOLDER).mkdirs();

            Properties props = new Properties();
            props.setProperty("balance", String.valueOf(wallet.getBalance()));
            props.setProperty("prestige", String.valueOf(PrestigeManager.getPoints()));
            props.setProperty("theme", themeName);
            
            // Count inventory logic would need to happen before passing here or be calculated
            // For simplicity in this refactor, we usually pass the counts.
            // ... (Save logic follows standard Properties saving)
            
            FileOutputStream out = new FileOutputStream(PROFILE_FOLDER + profileName + ".properties");
            props.store(out, "Java Tycoon Save");
            out.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public static List<String> getProfiles() {
        List<String> profiles = new ArrayList<>();
        File folder = new File(PROFILE_FOLDER);
        if (!folder.exists()) folder.mkdirs(); // Create if missing
        
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".properties"));
        if (files != null) {
            for (File f : files) profiles.add(f.getName().replace(".properties", ""));
        }
        return profiles;
    }

    public static void deleteProfile(String name) {
        File f = new File(PROFILE_FOLDER + name + ".properties");
        if (f.exists()) f.delete();
    }
    
    // Note: Loading logic needs to return data to the main class
    public static Properties loadProfileData(String profileName) {
        Properties props = new Properties();
        try {
            File f = new File(PROFILE_FOLDER + profileName + ".properties");
            if (f.exists()) {
                FileInputStream in = new FileInputStream(f);
                props.load(in);
                in.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return props;
    }
}