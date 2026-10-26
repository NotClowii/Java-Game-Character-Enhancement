import java.awt.Color;
import java.util.List;

class Warrior extends GameCharacter {
    public Warrior(String name) {
        super(name, 120, 40);
    }

    @Override
    public String attack(GameCharacter t) {
        int d = t.takeDamage(roll(12, 20));
        return getName() + " swings a mighty sword for " + d + " damage!";
    }

    @Override
    public String specialMove(GameCharacter t, List<GameCharacter> allies) {
        spendMana(getSpecialCost());
        int d = t.takeDamage(roll(22, 30));
        defend();
        return getName() + " uses Shield Bash for " + d + " damage and raises their guard!";
    }

    @Override
    public String getTypeName() {
        return "Warrior";
    }

    @Override
    public Color getThemeColor() {
        return new Color(200, 70, 60);
    }

    @Override
    public int getSpecialCost() {
        return 20;
    }
}