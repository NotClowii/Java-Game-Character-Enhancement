import java.awt.Color;
import java.util.List;
import java.util.Random;

public abstract class GameCharacter {
    protected static final Random RNG = new Random();

    private final String name;
    private final int maxHp;
    private final int maxMana;
    private int hp;
    private int mana;
    private boolean guarding;

    protected GameCharacter(String name, int maxHp, int maxMana) {
        this.name = name;
        this.maxHp = maxHp;
        this.maxMana = maxMana;
        restoreFull();
    }

    // ---- Abstract methods (each character type behaves differently) ----
    public abstract String attack(GameCharacter target);
    public abstract String specialMove(GameCharacter target, List<GameCharacter> allies);
    public abstract String getTypeName();
    public abstract Color getThemeColor();
    public abstract int getSpecialCost();

    public String showName() { return name + " the " + getTypeName(); }

    public boolean canUseSpecial() { return mana >= getSpecialCost(); }

    protected void spendMana(int amount) { mana = Math.max(0, mana - amount); }

    public int takeDamage(int amount) {
        amount = Math.max(0, amount);
        if (guarding) { amount /= 2; guarding = false; }
        hp = Math.max(0, hp - amount);
        return amount;
    }

    public int heal(int amount) {
        int before = hp;
        hp = Math.min(maxHp, hp + Math.max(0, amount));
        return hp - before;
    }

    public void restoreMana(int amount) { mana = Math.min(maxMana, mana + Math.max(0, amount)); }
    public void defend() { guarding = true; restoreMana(10); }
    public void restoreFull() { hp = maxHp; mana = maxMana; guarding = false; }

    protected int roll(int min, int max) { return min + RNG.nextInt(max - min + 1); }

    public boolean isAlive() { return hp > 0; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
}