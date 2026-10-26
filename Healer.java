import java.awt.Color;
import java.util.List;

class Healer extends GameCharacter {
    public Healer(String name) {
        super(name, 85, 70);
    }

    @Override
    public String attack(GameCharacter t) {
        int d = t.takeDamage(roll(6, 10));
        return getName() + " strikes with a holy staff for " + d + " damage.";
    }

    @Override
    public String specialMove(GameCharacter t, List<GameCharacter> allies) {
        spendMana(getSpecialCost());
        GameCharacter weakest = null;
        for (GameCharacter a : allies) {
            if (!a.isAlive())
                continue;
            if (weakest == null || (double) a.getHp() / a.getMaxHp() < (double) weakest.getHp() / weakest.getMaxHp())
                weakest = a;
        }
        if (weakest == null)
            weakest = this;
        int healed = weakest.heal(40);
        return getName() + " casts Holy Light, restoring " + healed + " HP to " + weakest.getName() + "!";
    }

    @Override
    public String getTypeName() {
        return "Healer";
    }

    @Override
    public Color getThemeColor() {
        return new Color(230, 190, 70);
    }

    @Override
    public int getSpecialCost() {
        return 25;
    }
}
