import java.awt.Color;
import java.util.List;

/** The boss enemy. Also a GameCharacter, so it reuses the same abstraction. */
class Dragon extends GameCharacter {
    public Dragon() {
        super("Ember", 400, 100);
    }

    @Override
    public String attack(GameCharacter t) {
        int d = t.takeDamage(roll(15, 25));
        return "Ember the Dragon claws " + t.getName() + " for " + d + " damage!";
    }

    /** Inferno hits every living hero. */
    @Override
    public String specialMove(GameCharacter t, List<GameCharacter> allies) {
        spendMana(getSpecialCost());
        int total = 0;
        for (GameCharacter a : allies)
            if (a.isAlive())
                total += a.takeDamage(roll(12, 18));
        return "Ember breathes INFERNO on the party for " + total + " total damage!";
    }

    @Override
    public String getTypeName() {
        return "Dragon";
    }

    @Override
    public Color getThemeColor() {
        return new Color(170, 40, 40);
    }

    @Override
    public int getSpecialCost() {
        return 30;
    }
}