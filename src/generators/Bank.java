package generators;

public class Bank extends Generator {
    public Bank() { super("Bank", 20000); } // Cost: $20,000
    
    @Override
    public double calculateBaseProduction() { 
        return level * 350.0; // High passive income
    }
}