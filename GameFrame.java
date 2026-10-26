import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Main window: party setup (user input + validation) and turn-based battle. */
public class GameFrame extends JFrame {
    private static final int MAX_PARTY = 4;
    private static final Pattern NAME_OK = Pattern.compile("[A-Za-z0-9 ]{1,12}");
    private static final Color BG = new Color(24, 24, 38), PANEL = new Color(36, 36, 58);

    private final List<GameCharacter> roster = new ArrayList<>(); // party list
    private final DefaultListModel<String> rosterModel = new DefaultListModel<>();
    private final JList<String> rosterList = new JList<>(rosterModel);
    private final JTextField nameField = new JTextField(12);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[] { "Warrior", "Mage", "Healer" });
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel("Build your party (1-4 heroes) and start the battle.",
            SwingConstants.CENTER);
    private final JTextArea logArea = new JTextArea(10, 24);
    private final ArenaPanel arena = new ArenaPanel();

    private final JButton addBtn = button("Add Hero", new Color(60, 140, 90));
    private final JButton removeBtn = button("Remove Selected", new Color(150, 70, 70));
    private final JButton startBtn = button("Start Battle", new Color(70, 100, 190));
    private final JButton attackBtn = button("Attack [1]", new Color(190, 80, 60));
    private final JButton specialBtn = button("Special [2]", new Color(120, 80, 200));
    private final JButton defendBtn = button("Defend [3]", new Color(70, 120, 160));
    private final JButton restartBtn = button("Reset", new Color(90, 90, 110));

    private LinkedQueue<GameCharacter> turnQueue = new LinkedQueue<>(); // ADT: turn order
    private LinkedStack<String> battleLog = new LinkedStack<>(); // ADT: log (newest on top)
    private Dragon boss;
    private GameCharacter current;
    private boolean battleActive, busy;
    private int actionsLeft, round;

    public GameFrame() {
        super("Dragon Quest Arena - Game Character RPG");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildSetupPanel(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.EAST);
        wireEvents();
        updateButtons();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // ---------- UI construction ----------
    private static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        return b;
    }

    private JPanel buildSetupPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(210, 0));

        p.add(label("PARTY SETUP", 15, Color.ORANGE));
        p.add(Box.createVerticalStrut(8));
        p.add(label("Hero name (1-12 letters/numbers):", 11, Color.WHITE));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        p.add(nameField);
        p.add(Box.createVerticalStrut(6));
        p.add(label("Class:", 11, Color.WHITE));
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        p.add(typeBox);
        p.add(Box.createVerticalStrut(8));
        for (JButton b : new JButton[] { addBtn, removeBtn, startBtn, restartBtn }) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            b.setAlignmentX(LEFT_ALIGNMENT);
            p.add(b);
            p.add(Box.createVerticalStrut(5));
        }
        errorLabel.setForeground(new Color(255, 110, 110));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(errorLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(label("Your party:", 11, Color.WHITE));
        rosterList.setBackground(new Color(20, 20, 32));
        rosterList.setForeground(Color.WHITE);
        JScrollPane sp = new JScrollPane(rosterList);
        sp.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sp);
        return p;
    }

    private JPanel buildCenter() {
        JPanel c = new JPanel(new BorderLayout(6, 6));
        c.setOpaque(false);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        c.add(statusLabel, BorderLayout.NORTH);
        c.add(arena, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        actions.add(attackBtn);
        actions.add(specialBtn);
        actions.add(defendBtn);
        actions.setPreferredSize(new Dimension(0, 42));
        c.add(actions, BorderLayout.SOUTH);
        return c;
    }

    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.setPreferredSize(new Dimension(270, 0));
        p.add(label("BATTLE LOG (newest first)", 13, Color.ORANGE), BorderLayout.NORTH);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(20, 20, 32));
        logArea.setForeground(new Color(220, 230, 255));
        p.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return p;
    }

    private static JLabel label(String t, int size, Color c) {
        JLabel l = new JLabel(t);
        l.setForeground(c);
        l.setFont(new Font("SansSerif", Font.BOLD, size));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void wireEvents() {
        addBtn.addActionListener(e -> addCharacter());
        nameField.addActionListener(e -> addCharacter());
        removeBtn.addActionListener(e -> removeSelected());
        startBtn.addActionListener(e -> startBattle());
        restartBtn.addActionListener(e -> resetToSetup());
        attackBtn.addActionListener(e -> heroAct("attack"));
        specialBtn.addActionListener(e -> heroAct("special"));
        defendBtn.addActionListener(e -> heroAct("defend"));

        JRootPane root = getRootPane();
        bindKey(root, "1", () -> heroAct("attack"));
        bindKey(root, "2", () -> heroAct("special"));
        bindKey(root, "3", () -> heroAct("defend"));
    }

    private void bindKey(JRootPane root, String key, Runnable r) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        root.getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        });
    }

    // ---------- Setup logic with input validation ----------
    private void addCharacter() {
        if (battleActive)
            return;
        String name = nameField.getText().trim().replaceAll("\\s+", " ");
        if (!NAME_OK.matcher(name).matches()) {
            error("Name must be 1-12 letters, numbers or spaces.");
            return;
        }
        if (roster.size() >= MAX_PARTY) {
            error("Party is full (max " + MAX_PARTY + ").");
            return;
        }
        for (GameCharacter c : roster)
            if (c.getName().equalsIgnoreCase(name)) {
                error("That name is already taken.");
                return;
            }

        GameCharacter hero;
        switch ((String) typeBox.getSelectedItem()) {
            case "Warrior":
                hero = new Warrior(name);
                break;
            case "Mage":
                hero = new Mage(name);
                break;
            default:
                hero = new Healer(name);
        }
        roster.add(hero);
        rosterModel.addElement(hero.showName());
        nameField.setText("");
        error(" ");
        arena.setBattle(roster, null);
        updateButtons();
    }

    private void removeSelected() {
        int i = rosterList.getSelectedIndex();
        if (i < 0) {
            error("Select a hero to remove first.");
            return;
        }
        roster.remove(i);
        rosterModel.remove(i);
        error(" ");
        arena.setBattle(roster, null);
        updateButtons();
    }

    private void error(String msg) {
        errorLabel.setText("<html>" + msg + "</html>");
    }

    // ---------- Battle logic ----------
    private void startBattle() {
        if (roster.isEmpty()) {
            error("Add at least one hero first.");
            return;
        }
        for (GameCharacter c : roster)
            c.restoreFull();
        boss = new Dragon();
        turnQueue = new LinkedQueue<>();
        for (GameCharacter c : roster)
            turnQueue.enqueue(c);
        battleLog = new LinkedStack<>();
        battleActive = true;
        busy = false;
        round = 1;
        actionsLeft = roster.size();
        arena.setBattle(roster, boss);
        log("A wild dragon appears! Round 1 begins.");
        prepareTurn();
    }

    private void prepareTurn() {
        while (!turnQueue.isEmpty() && !turnQueue.peek().isAlive())
            turnQueue.dequeue();
        current = turnQueue.peek();
        if (current == null) {
            endBattle(false);
            return;
        }
        current.restoreMana(5);
        arena.setActive(current);
        statusLabel.setText("Round " + round + " - " + current.showName() + "'s turn");
        updateButtons();
    }

    private void heroAct(String action) {
        if (!battleActive || busy || current == null)
            return;
        if (action.equals("special") && !current.canUseSpecial())
            return;

        GameCharacter hero = turnQueue.dequeue();
        switch (action) {
            case "attack":
                log(hero.attack(boss));
                break;
            case "special":
                log(hero.specialMove(boss, roster));
                break;
            default:
                hero.defend();
                log(hero.getName() + " braces for impact (+10 MP, next hit halved).");
        }
        if (hero.isAlive())
            turnQueue.enqueue(hero);

        if (!boss.isAlive()) {
            endBattle(true);
            return;
        }
        if (--actionsLeft > 0) {
            prepareTurn();
            return;
        }

        busy = true;
        arena.setActive(null);
        statusLabel.setText("The dragon's turn...");
        updateButtons();
        Timer t = new Timer(900, e -> bossTurn());
        t.setRepeats(false);
        t.start();
    }

    private void bossTurn() {
        List<GameCharacter> alive = new ArrayList<>();
        for (GameCharacter c : roster)
            if (c.isAlive())
                alive.add(c);
        boss.restoreMana(10);
        if (boss.canUseSpecial() && Math.random() < 0.35) {
            log(boss.specialMove(null, roster));
        } else {
            log(boss.attack(alive.get((int) (Math.random() * alive.size()))));
        }
        for (GameCharacter c : roster)
            if (!c.isAlive() && c.getHp() == 0 && !deathLogged(c))
                log(c.getName() + " has fallen!");

        busy = false;
        boolean anyAlive = false;
        for (GameCharacter c : roster)
            if (c.isAlive())
                anyAlive = true;
        if (!anyAlive) {
            endBattle(false);
            return;
        }
        round++;
        actionsLeft = 0;
        for (GameCharacter c : turnQueue.toList())
            if (c.isAlive())
                actionsLeft++;
        log("--- Round " + round + " ---");
        prepareTurn();
    }

    private final List<GameCharacter> fallen = new ArrayList<>();

    private boolean deathLogged(GameCharacter c) {
        if (fallen.contains(c))
            return true;
        fallen.add(c);
        return false;
    }

    private void endBattle(boolean won) {
        battleActive = false;
        busy = false;
        current = null;
        fallen.clear();
        arena.setActive(null);
        String msg = won ? "VICTORY! The dragon has been slain!" : "DEFEAT... your party has fallen.";
        log(msg);
        statusLabel.setText(msg + " Press Reset to play again.");
        updateButtons();
        JOptionPane.showMessageDialog(this, msg, "Battle Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetToSetup() {
        battleActive = false;
        busy = false;
        current = null;
        boss = null;
        fallen.clear();
        for (GameCharacter c : roster)
            c.restoreFull();
        arena.setBattle(roster, null);
        battleLog = new LinkedStack<>();
        logArea.setText("");
        statusLabel.setText("Build your party (1-4 heroes) and start the battle.");
        updateButtons();
    }

    private void log(String msg) {
        battleLog.push(msg);
        StringBuilder sb = new StringBuilder();
        for (String s : battleLog.toList())
            sb.append(s).append("\n\n");
        logArea.setText(sb.toString());
        logArea.setCaretPosition(0);
    }

    private void updateButtons() {
        boolean setup = !battleActive;
        addBtn.setEnabled(setup);
        removeBtn.setEnabled(setup);
        startBtn.setEnabled(setup && !roster.isEmpty());
        nameField.setEnabled(setup);
        typeBox.setEnabled(setup);
        boolean turn = battleActive && !busy && current != null;
        attackBtn.setEnabled(turn);
        defendBtn.setEnabled(turn);
        specialBtn.setEnabled(turn && current.canUseSpecial());
        specialBtn.setText(turn ? "Special [2] (" + current.getSpecialCost() + " MP)" : "Special [2]");
    }
}