package core;

public class IsekaiManager {
    // Static variable shared across the whole game
    private static int worldLevel = 1; // Start at World 1

    public static void addPoints(int points) {
        worldLevel += points;
    }

    // Called by SaveManager to load data
    public static void setPoints(int level) {
        worldLevel = level;
        if(worldLevel < 1) worldLevel = 1; // Safety check
    }

    public static int getLevel() {
        return worldLevel;
    }

    public static double getMultiplier() {
        // Base 1.0x + 10% per World Level above 1
        return 1.0 + ((worldLevel - 1) * 0.10);
    }
    
    public static void reset() {
        worldLevel = 1;
    }
}