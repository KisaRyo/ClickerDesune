package core;

public class Wallet {
    private double balance; 

    public Wallet() { this.balance = 0; }
    
    public double getBalance() { return balance; }
    
    public boolean spend(double amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    public void earn(double amount) {
        if (amount > 0) balance += amount;
    }
}