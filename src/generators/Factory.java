package generators;


public class Factory extends Generator {
    public Factory() { super("Factory", 1000); }
    
    @Override
    public double calculateBaseProduction() { 
        // Factories are powerful! Base 50 production.
        return (level * 50.0); 
    }
}