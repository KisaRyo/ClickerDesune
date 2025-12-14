package generators;

public class ManualClicker extends Generator {
    public ManualClicker() { super("Cursor", 10); }
    
    @Override 
    public double calculateBaseProduction() { return level * 1.0; }
}