package generators;

public class Shrine extends Generator {
    public Shrine() {
        this.name = "Grand Shrine";
        this.baseCost = 100000;
        this.level = 1;
    }

    @Override
    public double calculateBaseProduction() {
        return 1500 + (level * 100);
    }
}