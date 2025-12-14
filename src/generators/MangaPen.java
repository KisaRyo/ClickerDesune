package generators;

public class MangaPen extends Generator {
    public MangaPen() {
        this.name = "Manga Pen";
        this.baseCost = 15;
        this.level = 1;
    }

    @Override
    public double calculateBaseProduction() {
        return 0.5 * level; // Base click power
    }
}