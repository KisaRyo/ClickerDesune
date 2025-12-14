package generators;

public class MaidCafe extends Generator {
    public MaidCafe() {
        this.name = "Maid Cafe";
        this.baseCost = 2000;
        this.level = 1;
    }

    @Override
    public double calculateBaseProduction() {
        return 50 + (level * 5);
    }
}