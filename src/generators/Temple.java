package generators;

public class Temple extends Generator {
    public Temple() { super("Temple", 100000); } // Cost: $100,000
    
    @Override
    public double calculateBaseProduction() { 
        return level * 1500.0; // Massive production
    }
}