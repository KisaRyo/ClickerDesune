package generators;

public class GoldMine extends Generator {
    public GoldMine() { super("Mine", 100); }
    
    @Override
    public double calculateBaseProduction() { return (level * 5.0) * 1.2; }
}