package generators;

import core.PrestigeManager;

public abstract class Generator {
    protected String name;
    protected int level;
    protected double baseCost;

    public Generator(String name, double baseCost) {
        this.name = name;
        this.baseCost = baseCost;
        this.level = 1;
    }
    
    public abstract double calculateBaseProduction();

    public double getFinalProduction() {
        return calculateBaseProduction() * PrestigeManager.getMultiplier();
    }

    public void upgrade() {
        level++;
        baseCost = baseCost * 1.5; 
    }
    
    public double getCost() { return baseCost; }
    public String getName() { return name; }
    public int getLevel() { return level; }
}