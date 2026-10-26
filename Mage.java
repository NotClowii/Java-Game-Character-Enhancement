import java.awt.Color;
import java.util.List;

class Mage extends GameCharacter {
    public Mage(String name) {
        super(name, 80, 80);
    }

    @Override
    public String attack(GameCharacter t) {
        int d = t.takeDamage(roll(14, 22));
        return getName() + " casts a fireball for " + d + " damage!";
    }

    @Override
    public String specialMove(GameCharacter t, List<GameCharacter> allies) {
        spendMana(getSpecialCost());
        int d = t.takeDamage(roll(38, 52));
        return getName() + " unleashes Lightning Storm for " + d + " damage!";
    }

    @Override
    public String getTypeName() {
        return "Mage";
    }

    @Override
    public Color getThemeColor() {
        return new Color(110, 80, 210);
    }

    @Override
    public int getSpecialCost() {
        return 30;
    }
}