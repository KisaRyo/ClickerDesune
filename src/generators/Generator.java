package generators;

public abstract class Generator {
    protected String name;
    protected double baseCost;
    protected int level;

    public void upgrade() {
        level++;
    }

    public double getCost() {
        // Simple math: Cost increases by 15% per level
        return baseCost * Math.pow(1.15, level - 1);
    }

    public int getLevel() {
        return level;
    }

    // This calculates the raw number (e.g., 50 per sec)
    public abstract double calculateBaseProduction();

    // This adds the "Isekai" multiplier to it
    public double getFinalProduction() {
        return calculateBaseProduction() * core.IsekaiManager.getMultiplier();
    }
}