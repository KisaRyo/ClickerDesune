package utils;

import core.Wallet;
import core.IsekaiManager; // Updated Import
import generators.*;       // Imports MangaPen, MaidCafe, etc.
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SaveManager {
    private static final String PROFILE_FOLDER = "profiles/";

    public static void saveGame(String profileName, Wallet wallet, List<Generator> inventory, String themeName) {
        try {
            // Ensure folder exists
            new File(PROFILE_FOLDER).mkdirs();

            Properties props = new Properties();
            props.setProperty("balance", String.valueOf(wallet.getBalance()));
            
            // SAVE ISEKAI LEVEL (Was Prestige)
            props.setProperty("isekai_points", String.valueOf(IsekaiManager.getLevel())); 
            
            props.setProperty("theme", themeName);
            
            // COUNT NEW ANIME ITEMS
            props.setProperty("count_pen", String.valueOf(countOwned(inventory, MangaPen.class)));
            props.setProperty("count_gacha", String.valueOf(countOwned(inventory, GachaMachine.class)));
            props.setProperty("count_cafe", String.valueOf(countOwned(inventory, MaidCafe.class)));
            props.setProperty("count_studio", String.valueOf(countOwned(inventory, AnimeStudio.class)));
            props.setProperty("count_shrine", String.valueOf(countOwned(inventory, Shrine.class)));

            FileOutputStream out = new FileOutputStream(PROFILE_FOLDER + profileName + ".properties");
            props.store(out, "Clicker Desu Ne Save Data");
            out.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // Helper to count specific items in the list
    private static long countOwned(List<Generator> inventory, Class<? extends Generator> type) {
        return inventory.stream().filter(type::isInstance).count();
    }

    public static List<String> getProfiles() {
        List<String> profiles = new ArrayList<>();
        File folder = new File(PROFILE_FOLDER);
        if (!folder.exists()) folder.mkdirs(); 
        
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