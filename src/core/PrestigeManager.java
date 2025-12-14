package core;

public class PrestigeManager {
    // Static variable shared across the whole game
    private static int prestigePoints = 0;

    public static void addPoints(int amount) {
        prestigePoints += amount;
    }

    // --- NEW: REQUIRED FOR SAVE SYSTEM ---
    // Allows the SaveManager to force the points to a specific number
    public static void setPoints(int amount) {
        prestigePoints = amount;
    }

    public static int getPoints() {
        return prestigePoints;
    }

    public static double getMultiplier() {
        // Base 1.0x + 10% per point
        return 1.0 + (prestigePoints * 0.10);
    }
    
    public static void reset() {
        prestigePoints = 0;
    }
}