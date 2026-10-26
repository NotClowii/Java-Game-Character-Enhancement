import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom-painted battle arena with animated HP bars, idle bobbing, and hit/heal
 * flashes.
 */
public class ArenaPanel extends JPanel {
    private List<GameCharacter> heroes = new ArrayList<>();
    private GameCharacter boss, active;
    private final Map<GameCharacter, Float> shown = new IdentityHashMap<>();
    private final Map<GameCharacter, Integer> prevHp = new IdentityHashMap<>();
    private final Map<GameCharacter, Integer> flash = new IdentityHashMap<>();
    private int tick;

    public ArenaPanel() {
        setPreferredSize(new Dimension(720, 420));
        new Timer(30, e -> {
            tick++;
            update();
            repaint();
        }).start();
    }

    public void setBattle(List<GameCharacter> heroes, GameCharacter boss) {
        this.heroes = heroes;
        this.boss = boss;
        this.active = null;
        shown.clear();
        prevHp.clear();
        flash.clear();
    }

    public void setActive(GameCharacter c) {
        active = c;
    }

    private List<GameCharacter> all() {
        List<GameCharacter> l = new ArrayList<>(heroes);
        if (boss != null)
            l.add(boss);
        return l;
    }

    private void update() {
        for (GameCharacter c : all()) {
            int hp = c.getHp();
            int prev = prevHp.getOrDefault(c, hp);
            if (hp < prev)
                flash.put(c, 12);
            else if (hp > prev)
                flash.put(c, -12);
            prevHp.put(c, hp);
            float s = shown.getOrDefault(c, (float) hp);
            s += (hp - s) * 0.15f;
            if (Math.abs(hp - s) < 0.3f)
                s = hp;
            shown.put(c, s);
            int f = flash.getOrDefault(c, 0);
            if (f > 0)
                flash.put(c, f - 1);
            else if (f < 0)
                flash.put(c, f + 1);
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(20, 20, 50), 0, h, new Color(70, 30, 80)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 130));
        for (int i = 0; i < 40; i++) {
            int tw = ((tick / 10 + i) % 4 == 0) ? 3 : 2;
            g.fillOval((i * 97) % w, (i * 53) % (h / 2), tw, tw);
        }
        int groundY = h - 90;
        g.setColor(new Color(30, 25, 45));
        g.fillRect(0, groundY, w, 90);
        g.setColor(new Color(90, 60, 120));
        g.fillRect(0, groundY, w, 3);

        if (heroes.isEmpty()) {
            g.setColor(new Color(220, 220, 240));
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Create your party on the left to begin!", w / 2 - 170, h / 2);
        }
        for (int i = 0; i < heroes.size(); i++)
            drawHero(g, heroes.get(i), 70 + i * 105, groundY + 10);
        if (boss != null)
            drawDragon(g, boss, w - 130, groundY + 10);
        g.dispose();
    }

    private void drawHero(Graphics2D g, GameCharacter c, int x, int y) {
        boolean alive = c.isAlive();
        int fl = flash.getOrDefault(c, 0);
        int bob = alive ? (int) (Math.sin(tick * 0.12 + x) * 3) : 0;
        int cx = x + (fl > 0 ? (tick % 2 == 0 ? 5 : -5) : 0);
        int top = y - 60 + bob;
        Color base = alive ? c.getThemeColor() : Color.GRAY;

        g.setColor(base);
        g.fillRoundRect(cx - 22, top, 44, 60, 18, 18);
        g.setColor(alive ? new Color(245, 210, 175) : Color.LIGHT_GRAY);
        g.fillOval(cx - 14, top - 30, 28, 28);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 7, top - 20, 4, 4);
        g.fillOval(cx + 3, top - 20, 4, 4);

        g.setStroke(new BasicStroke(3));
        switch (c.getTypeName()) {
            case "Warrior":
                g.setColor(new Color(215, 215, 230));
                g.drawLine(cx + 28, top + 45, cx + 28, top + 5);
                g.setColor(new Color(130, 90, 50));
                g.drawLine(cx + 22, top + 42, cx + 34, top + 42);
                g.setColor(new Color(120, 120, 150));
                g.fillOval(cx - 42, top + 15, 20, 28);
                break;
            case "Mage":
                g.setColor(new Color(130, 90, 50));
                g.drawLine(cx + 28, top + 55, cx + 28, top - 8);
                g.setColor(new Color(255, 120, 40));
                g.fillOval(cx + 22, top - 20, 13, 13);
                g.setColor(base.darker());
                g.fillPolygon(new int[] { cx - 17, cx + 17, cx }, new int[] { top - 24, top - 24, top - 64 }, 3);
                break;
            default:
                g.setColor(Color.WHITE);
                g.fillRect(cx - 3, top + 12, 6, 24);
                g.fillRect(cx - 12, top + 21, 24, 6);
        }
        g.setStroke(new BasicStroke(1));

        if (fl != 0) {
            g.setColor(fl > 0 ? new Color(255, 0, 0, 100) : new Color(0, 255, 120, 100));
            g.fillRoundRect(cx - 22, top, 44, 60, 18, 18);
        }
        if (c == active && alive) {
            int ay = top - 75 + (int) (Math.sin(tick * 0.2) * 4);
            g.setColor(Color.YELLOW);
            g.fillPolygon(new int[] { cx - 8, cx + 8, cx }, new int[] { ay, ay, ay + 12 }, 3);
        }
        drawBars(g, c, x, y + 10, 84);
    }

    private void drawDragon(Graphics2D g, GameCharacter c, int x, int y) {
        boolean alive = c.isAlive();
        int fl = flash.getOrDefault(c, 0);
        int bob = alive ? (int) (Math.sin(tick * 0.08) * 5) : 0;
        int cx = x + (fl > 0 ? (tick % 2 == 0 ? 6 : -6) : 0);
        int top = y - 130 + bob;
        Color body = alive ? c.getThemeColor() : Color.GRAY;

        g.setColor(body.darker());
        g.fillPolygon(new int[] { cx - 5, cx + 50, cx + 75 },
                new int[] { top + 45, top - 25 + (int) (Math.sin(tick * 0.15) * 8), top + 60 }, 3);
        g.fillPolygon(new int[] { cx + 50, cx + 110, cx + 85 }, new int[] { top + 90, top + 110, top + 70 }, 3); // tail
        g.setColor(body);
        g.fillOval(cx - 55, top + 35, 130, 85);
        g.fillOval(cx - 85, top + 15, 60, 50);
        g.setColor(new Color(240, 230, 200));
        g.fillPolygon(new int[] { cx - 70, cx - 60, cx - 64 }, new int[] { top + 18, top + 18, top - 2 }, 3);
        g.fillPolygon(new int[] { cx - 50, cx - 40, cx - 46 }, new int[] { top + 20, top + 20, top }, 3);
        g.setColor(alive ? Color.YELLOW : Color.DARK_GRAY);
        g.fillOval(cx - 68, top + 28, 9, 9);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 65, top + 31, 3, 4);
        if (fl != 0) {
            g.setColor(fl > 0 ? new Color(255, 255, 255, 130) : new Color(0, 255, 120, 90));
            g.fillOval(cx - 55, top + 35, 130, 85);
        }
        drawBars(g, c, x, y + 10, 150);
    }

    private void drawBars(Graphics2D g, GameCharacter c, int cx, int y, int w) {
        float s = shown.getOrDefault(c, (float) c.getHp());
        double r = Math.max(0, Math.min(1, s / c.getMaxHp()));
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(cx - w / 2, y, w, 9, 6, 6);
        g.setColor(new Color((int) (220 * (1 - r) + 35), (int) (200 * r + 30), 60));
        g.fillRoundRect(cx - w / 2, y, (int) (w * r), 9, 6, 6);
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(cx - w / 2, y + 11, w, 5, 4, 4);
        g.setColor(new Color(70, 140, 255));
        g.fillRoundRect(cx - w / 2, y + 11, (int) (w * c.getMana() / (double) c.getMaxMana()), 5, 4, 4);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String l1 = c.getName() + " (" + c.getTypeName() + ")";
        String l2 = c.getHp() + "/" + c.getMaxHp() + " HP";
        g.setColor(Color.WHITE);
        g.drawString(l1, cx - fm.stringWidth(l1) / 2, y + 31);
        g.setColor(new Color(200, 200, 220));
        g.drawString(l2, cx - fm.stringWidth(l2) / 2, y + 44);
    }
}