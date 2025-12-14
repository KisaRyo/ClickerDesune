package generators;

public class GachaMachine extends Generator {
    public GachaMachine() {
        this.name = "Gacha Machine";
        this.baseCost = 100;
        this.level = 1;
    }

    @Override
    public double calculateBaseProduction() {
        return 5 + (level * 1.2);
    }
}