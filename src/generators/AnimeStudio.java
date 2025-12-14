package generators;

public class AnimeStudio extends Generator {
    public AnimeStudio() {
        this.name = "Anime Studio";
        this.baseCost = 15000;
        this.level = 1;
    }

    @Override
    public double calculateBaseProduction() {
        return 350 + (level * 25);
    }
}