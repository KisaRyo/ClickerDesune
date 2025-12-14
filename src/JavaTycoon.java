import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// (The Modular Architecture)
import core.Wallet;
import core.PrestigeManager;
import generators.*;
import utils.ImageLoader;
import utils.SaveManager;

public class JavaTycoon {
    
    // --- GAME STATE ---
    private static String currentProfile = "Default"; 
    private static Wallet wallet = new Wallet();
    private static List<Generator> inventory = new ArrayList<>();
    
    // Catalog (Prototypes for the shop display)
    private static ManualClicker shopCursor = new ManualClicker();
    private static GoldMine shopMine = new GoldMine();
    private static Factory shopFactory = new Factory();
    private static Bank shopBank = new Bank();
    private static Temple shopTemple = new Temple();

    private static boolean hasSeenPrestige = false;
    private static double clickHeat = 0; 
    private static boolean showIntensity = true;

    // --- THEME ENGINE ---
    // Kept here because it's purely UI logic
    record Theme(String name, Color accent, Color bgDark, Color bgPanel, Color textHeat) {}
    private static final Theme THEME_GOLD = new Theme("Gold", new Color(255, 215, 0), new Color(30, 30, 35), new Color(45, 45, 50), new Color(255, 100, 100));
    private static final Theme THEME_BLUE = new Theme("Cyber", new Color(0, 255, 255), new Color(10, 15, 30), new Color(20, 30, 50), new Color(255, 0, 255));
    private static final Theme THEME_RED  = new Theme("Mars", new Color(255, 80, 80), new Color(40, 10, 10), new Color(60, 20, 20), new Color(255, 150, 0));
    private static final Theme THEME_PINK = new Theme("Kawaii", new Color(255, 105, 180), new Color(40, 20, 35), new Color(60, 30, 50), new Color(100, 255, 255));
    private static Theme currentTheme = THEME_GOLD;

    // --- IMAGES ---
    private static final String URL_COIN    = "https://img.icons8.com/emoji/256/coin-emoji.png";
    private static final String URL_CURSOR  = "https://img.icons8.com/fluency/96/cursor.png";
    private static final String URL_MINE    = "https://img.icons8.com/fluency/96/gold-ore.png";
    private static final String URL_FACTORY = "https://img.icons8.com/fluency/96/factory.png";
    private static final String URL_BANK    = "https://img.icons8.com/fluency/96/bank.png";
    private static final String URL_TEMPLE  = "https://img.icons8.com/fluency/96/temple.png";
    private static final String URL_LOCK    = "https://img.icons8.com/fluency/96/lock.png";
    private static final String URL_GEAR    = "https://img.icons8.com/fluency/96/settings.png";
    private static final String URL_TROPHY  = "https://img.icons8.com/fluency/96/trophy.png";

    // --- UI COMPONENTS ---
    private static JFrame frame;
    private static PatternPanel mainPanel; 
    private static JPanel rightContainer;  
    private static JPanel headerPanel;
    private static JLabel balanceLabel;
    private static JLabel mpsLabel;
    private static JLabel cpsLabel; 
    private static JLabel prestigeLabel;
    private static JTextArea gameLog;
    private static JLabel bigCoinLabel;
    private static JPanel shopContentPanel;

    public static void main(String[] args) {
        ImageLoader.preload(); // Uses utils.ImageLoader now
        new ProfileMenu(null).setVisible(true);
    }

    public static void launchGame(String profileName) {
        currentProfile = profileName;
        resetGameState(); 
        
        // --- NEW LOAD LOGIC ---
        // We now get data FROM SaveManager and apply it here
        Properties props = SaveManager.loadProfileData(currentProfile);
        if (!props.isEmpty()) {
            wallet.earn(Double.parseDouble(props.getProperty("balance", "0")));
            PrestigeManager.setPoints(Integer.parseInt(props.getProperty("prestige", "0")));
            
            String tName = props.getProperty("theme", "Gold");
            if(tName.equals("Cyber")) currentTheme = THEME_BLUE;
            else if(tName.equals("Mars")) currentTheme = THEME_RED;
            else if(tName.equals("Kawaii")) currentTheme = THEME_PINK;
            
            restoreItems(Integer.parseInt(props.getProperty("count_cursor", "0")), shopCursor, "Cursor");
            restoreItems(Integer.parseInt(props.getProperty("count_mine", "0")), shopMine, "Mine");
            restoreItems(Integer.parseInt(props.getProperty("count_factory", "0")), shopFactory, "Factory");
            restoreItems(Integer.parseInt(props.getProperty("count_bank", "0")), shopBank, "Bank");
            restoreItems(Integer.parseInt(props.getProperty("count_temple", "0")), shopTemple, "Temple");
        }

        if (frame == null) {
            createGameWindow();
        } else {
            frame.setVisible(true);
            applyTheme(currentTheme);
            updateUI();
            rebuildShopButtons();
            log("Loaded Profile: " + currentProfile);
        }
    }

    private static void saveCurrentState() {
        // We pass the data TO the SaveManager
        SaveManager.saveGame(currentProfile, wallet, inventory, currentTheme.name());
    }

    private static void createGameWindow() {
        frame = new JFrame("Java Tycoon: " + currentProfile);
        frame.setSize(1100, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveCurrentState(); }
        });
        
        mainPanel = new PatternPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.setContentPane(mainPanel);

        // HEADER
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false); 
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel statsBox = new JPanel(); 
        statsBox.setLayout(new BoxLayout(statsBox, BoxLayout.Y_AXIS));
        statsBox.setOpaque(false);
        
        balanceLabel = new JLabel("$ 0.00");
        balanceLabel.setForeground(currentTheme.accent);
        balanceLabel.setFont(new Font("Verdana", Font.BOLD, 36));
        
        mpsLabel = new JLabel("$ 0.0 / sec");
        mpsLabel.setForeground(Color.WHITE); 
        mpsLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        
        cpsLabel = new JLabel("Click Intensity: 0");
        cpsLabel.setForeground(currentTheme.textHeat); 
        cpsLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        prestigeLabel = new JLabel("Multiplier: x1.0");
        prestigeLabel.setForeground(new Color(180, 180, 180));
        prestigeLabel.setFont(new Font("Verdana", Font.PLAIN, 14));
        
        statsBox.add(balanceLabel);
        statsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        statsBox.add(mpsLabel);
        statsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        statsBox.add(cpsLabel);
        statsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        statsBox.add(prestigeLabel);
        
        headerPanel.add(statsBox, BorderLayout.WEST);

        // SETTINGS BUTTON
        JButton settingsBtn = new JButton(); 
        settingsBtn.setIcon(ImageLoader.getIcon(URL_GEAR, 24)); 
        styleButton(settingsBtn, new Color(60, 60, 60));
        settingsBtn.setPreferredSize(new Dimension(45, 45)); 
        settingsBtn.addActionListener(e -> openSettingsMenu());
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(settingsBtn);
        
        headerPanel.add(btnWrapper, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // CENTER
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        ImageIcon coinIcon = ImageLoader.getIcon(URL_COIN, 240);
        bigCoinLabel = new JLabel(coinIcon);
        bigCoinLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bigCoinLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                bigCoinLabel.setIcon(ImageLoader.getIcon(URL_COIN, 230)); 
                wallet.earn(getCursorPower());
                clickHeat += 1.0; 
                updateUI();
            }
            public void mouseReleased(MouseEvent e) { bigCoinLabel.setIcon(coinIcon); }
        });

        centerPanel.add(bigCoinLabel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // RIGHT
        rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(currentTheme.bgPanel);
        rightContainer.setPreferredSize(new Dimension(380, 0));
        rightContainer.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(0,0,0,50)));

        JLabel shopTitle = new JLabel("MARKETPLACE");
        shopTitle.setFont(new Font("Verdana", Font.BOLD, 18));
        shopTitle.setForeground(Color.WHITE);
        shopTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        shopTitle.setHorizontalAlignment(SwingConstants.CENTER);
        rightContainer.add(shopTitle, BorderLayout.NORTH);

        shopContentPanel = new JPanel();
        shopContentPanel.setLayout(new BoxLayout(shopContentPanel, BoxLayout.Y_AXIS));
        shopContentPanel.setOpaque(false);
        shopContentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JScrollPane shopScroll = new JScrollPane(shopContentPanel);
        shopScroll.setBorder(null);
        shopScroll.getViewport().setOpaque(false);
        shopScroll.setOpaque(false);
        shopScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        rightContainer.add(shopScroll, BorderLayout.CENTER);
        mainPanel.add(rightContainer, BorderLayout.EAST);

        // BOTTOM
        gameLog = new JTextArea(5, 50);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        gameLog.setForeground(new Color(200, 200, 200));
        gameLog.setBackground(new Color(20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(gameLog);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50,50,50)));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // LOOPS
        new Timer(100, e -> {
            double passive = calculatePassiveMPS() / 10.0;
            if(passive > 0) wallet.earn(passive); 
            if (clickHeat > 0) {
                clickHeat *= 0.90; 
                if (clickHeat < 0.1) clickHeat = 0;
            }
            updateUI();
            checkPrestigeUnlock();
        }).start();

        new Timer(30000, e -> {
            saveCurrentState();
            log("Auto-Saved: " + currentProfile);
        }).start();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        rebuildShopButtons(); 
        applyTheme(currentTheme); 
    }

    // --- HELPER METHODS ---

    private static void restoreItems(int count, Generator proto, String type) {
        for (int i = 0; i < count; i++) {
            proto.upgrade(); 
            if(type.equals("Cursor")) inventory.add(new ManualClicker());
            if(type.equals("Mine")) inventory.add(new GoldMine());
            if(type.equals("Factory")) inventory.add(new Factory());
            if(type.equals("Bank")) inventory.add(new Bank());
            if(type.equals("Temple")) inventory.add(new Temple());
        }
    }

    private static void resetGameState() {
        wallet = new Wallet(); // Uses core.Wallet
        inventory.clear();
        shopCursor = new ManualClicker(); 
        shopMine = new GoldMine(); 
        shopFactory = new Factory(); 
        shopBank = new Bank(); 
        shopTemple = new Temple();
        hasSeenPrestige = false;
        clickHeat = 0;
        currentTheme = THEME_GOLD; 
        PrestigeManager.reset(); 
    }

    private static void openSettingsMenu() {
        JDialog dialog = new JDialog(frame, "Settings", true);
        dialog.setSize(300, 480); 
        dialog.setLayout(new GridLayout(7, 1, 10, 10)); 
        dialog.getContentPane().setBackground(currentTheme.bgPanel); 
        dialog.setLocationRelativeTo(frame);

        JButton switchProfileBtn = new JButton("Switch Profile / Quit");
        styleButton(switchProfileBtn, new Color(100, 100, 100));
        switchProfileBtn.addActionListener(e -> {
            saveCurrentState(); 
            dialog.dispose();
            frame.setVisible(false); 
            new ProfileMenu(null).setVisible(true); 
        });
        dialog.add(switchProfileBtn);

        JCheckBox toggleIntensity = new JCheckBox("Show Click Intensity");
        toggleIntensity.setSelected(showIntensity);
        toggleIntensity.setOpaque(false);
        toggleIntensity.setForeground(Color.WHITE);
        toggleIntensity.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleIntensity.setHorizontalAlignment(SwingConstants.CENTER);
        toggleIntensity.addActionListener(e -> { showIntensity = toggleIntensity.isSelected(); updateUI(); });
        dialog.add(toggleIntensity);

        JLabel lbl = new JLabel("Select Theme:", SwingConstants.CENTER);
        lbl.setForeground(Color.LIGHT_GRAY);
        dialog.add(lbl);

        JButton goldBtn = new JButton("Classic Gold");
        styleButton(goldBtn, THEME_GOLD.accent);
        goldBtn.setForeground(Color.BLACK);
        goldBtn.addActionListener(e -> { applyTheme(THEME_GOLD); dialog.dispose(); });
        
        JButton blueBtn = new JButton("Cyber Blue");
        styleButton(blueBtn, THEME_BLUE.accent);
        blueBtn.setForeground(Color.BLACK);
        blueBtn.addActionListener(e -> { applyTheme(THEME_BLUE); dialog.dispose(); });

        JButton redBtn = new JButton("Mars Red");
        styleButton(redBtn, THEME_RED.accent);
        redBtn.setForeground(Color.BLACK);
        redBtn.addActionListener(e -> { applyTheme(THEME_RED); dialog.dispose(); });

        JButton pinkBtn = new JButton("Kawaii Pink");
        styleButton(pinkBtn, THEME_PINK.accent);
        pinkBtn.setForeground(Color.BLACK);
        pinkBtn.addActionListener(e -> { applyTheme(THEME_PINK); dialog.dispose(); });

        dialog.add(goldBtn); dialog.add(blueBtn); dialog.add(redBtn); dialog.add(pinkBtn); 
        dialog.setVisible(true);
    }
    
    private static void applyTheme(Theme t) {
        currentTheme = t;
        rightContainer.setBackground(currentTheme.bgPanel);
        balanceLabel.setForeground(currentTheme.accent);
        cpsLabel.setForeground(currentTheme.textHeat);
        rebuildShopButtons();
        mainPanel.repaint();
    }

    static class PatternPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            g2.setColor(currentTheme.bgDark); g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 10)); 
            for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);
            Color glow = new Color(currentTheme.accent.getRed(), currentTheme.accent.getGreen(), currentTheme.accent.getBlue(), 40);
            float radius = Math.min(w, h) * 0.8f;
            RadialGradientPaint p = new RadialGradientPaint(new Point2D.Float(w/2.0f, h/2.0f), radius, new float[]{0f, 1f}, new Color[]{glow, new Color(0,0,0,0)});
            g2.setPaint(p); g2.fillRect(0, 0, w, h);
        }
    }

    private static void rebuildShopButtons() {
        shopContentPanel.removeAll();
        addShopBtn(shopCursor, "Cursor", URL_CURSOR);
        if (countOwned(ManualClicker.class) > 0) addShopBtn(shopMine, "Mine", URL_MINE);
        if (countOwned(GoldMine.class) > 0) addShopBtn(shopFactory, "Factory", URL_FACTORY);
        if (countOwned(Factory.class) > 0) addShopBtn(shopBank, "Bank", URL_BANK);
        if (countOwned(Bank.class) > 0) addShopBtn(shopTemple, "Temple", URL_TEMPLE);
        if (hasSeenPrestige) {
            shopContentPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
            JButton prestigeBtn = new JButton("PRESTIGE ($5k)");
            prestigeBtn.setIcon(ImageLoader.getIcon(URL_TROPHY, 32));
            styleButton(prestigeBtn, currentTheme.accent);
            prestigeBtn.setForeground(Color.BLACK); 
            prestigeBtn.addActionListener(e -> performPrestige());
            shopContentPanel.add(prestigeBtn);
        }
        shopContentPanel.revalidate(); shopContentPanel.repaint();
    }

    private static void addShopBtn(Generator g, String n, String u) {
        shopContentPanel.add(createShopItem(g, n, ImageLoader.getIcon(u, 48)));
        shopContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private static void performPrestige() {
        if(wallet.getBalance() >= 5000) {
            int points = (int)(wallet.getBalance() / 1000);
            PrestigeManager.addPoints(points);
            resetGameState(); 
            log("PRESTIGE RESET! Points: " + points);
            updateUI(); rebuildShopButtons();
        } else { log("Need $5000 to Prestige!"); }
    }

    private static void updateUI() {
        balanceLabel.setText("$ " + String.format("%,.2f", wallet.getBalance()));
        mpsLabel.setText("$ " + String.format("%,.1f", calculateDisplayMPS()) + " / sec");
        cpsLabel.setText("Click Intensity: " + String.format("%.1f", clickHeat));
        cpsLabel.setVisible(showIntensity);
        prestigeLabel.setText("Prestige: x" + String.format("%.1f", PrestigeManager.getMultiplier()));
        if (frame != null) frame.setTitle("Java Tycoon: " + currentProfile);
    }

    private static double getCursorPower() { return (1 + shopCursor.calculateBaseProduction()) * PrestigeManager.getMultiplier(); }
    private static double calculatePassiveMPS() { double t = 0; for(Generator g : inventory) if(!(g instanceof ManualClicker)) t += g.getFinalProduction(); return t; }
    private static double calculateDisplayMPS() { return calculatePassiveMPS() + (clickHeat * getCursorPower()); }
    private static void checkPrestigeUnlock() { if (!hasSeenPrestige && wallet.getBalance() >= 5000) { hasSeenPrestige = true; log("PRESTIGE UNLOCKED!"); rebuildShopButtons(); } }
    private static long countOwned(Class<? extends Generator> type) { return inventory.stream().filter(type::isInstance).count(); }
    private static void log(String msg) { gameLog.append(" > " + msg + "\n"); gameLog.setCaretPosition(gameLog.getDocument().getLength()); }

    private static void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80,80,80), 1), new EmptyBorder(10, 15, 10, 15)));
        btn.setMaximumSize(new Dimension(350, 80));
    }

    private static JButton createShopItem(Generator gen, String name, ImageIcon icon) {
        double baseRate = 0; String unit = "/ sec";
        if(name.equals("Cursor")) { baseRate = 1.0; unit = "click power"; }
        else if(name.equals("Mine")) baseRate = 6.0; else if(name.equals("Factory")) baseRate = 50.0;
        else if(name.equals("Bank")) baseRate = 350.0; else if(name.equals("Temple")) baseRate = 1500.0;
        String html = String.format("<html><div style='text-align: left; width: 160px;'><b>%s</b> <span style='font-size:9px; color:gray;'>(Lvl %d)</span><br><font color='%s'>$%,.0f</font><br><font color='#AAAAAA' size='2'>+ %,.1f %s</font></div></html>", 
            name, gen.getLevel(), toHexString(currentTheme.accent), gen.getCost(), baseRate * PrestigeManager.getMultiplier(), unit);
        JButton btn = new JButton(html); btn.setIcon(icon); btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setIconTextGap(20); 
        btn.addActionListener(e -> { if(wallet.spend(gen.getCost())) { gen.upgrade(); 
                 if(name.equals("Cursor")) inventory.add(new ManualClicker()); if(name.equals("Mine")) inventory.add(new GoldMine());
                 if(name.equals("Factory")) inventory.add(new Factory()); if(name.equals("Bank")) inventory.add(new Bank());
                 if(name.equals("Temple")) inventory.add(new Temple());
                 log("Bought " + name); updateUI(); rebuildShopButtons(); } else { log("Too expensive!"); } });
        styleButton(btn, new Color(60, 60, 65)); return btn;
    }

    private static String toHexString(Color c) { return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()); }

    // --- PROFILE MENU UI ---
    static class ProfileMenu extends JDialog {
        private JList<String> profileList;
        private DefaultListModel<String> listModel;

        public ProfileMenu(JFrame parent) {
            super(parent, "Select Profile", true);
            setSize(400, 500);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(45, 45, 50));

            JLabel title = new JLabel("Welcome to Java Tycoon", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setForeground(Color.WHITE);
            title.setBorder(new EmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            listModel = new DefaultListModel<>();
            refreshList();
            
            profileList = new JList<>(listModel);
            profileList.setBackground(new Color(30, 30, 35));
            profileList.setForeground(Color.WHITE);
            profileList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JScrollPane scroll = new JScrollPane(profileList);
            scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
            add(scroll, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            btnPanel.setOpaque(false);
            btnPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JButton playBtn = new JButton("PLAY");
            styleButton(playBtn, new Color(0, 200, 100));
            playBtn.addActionListener(e -> {
                String selected = profileList.getSelectedValue();
                if (selected != null) {
                    dispose(); 
                    launchGame(selected); 
                }
            });

            JButton createBtn = new JButton("NEW");
            styleButton(createBtn, new Color(0, 100, 200));
            createBtn.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(this, "Enter Profile Name:");
                if (name != null && !name.trim().isEmpty()) {
                    SaveManager.saveGame(name.trim(), wallet, inventory, currentTheme.name()); // Pass defaults
                    refreshList();
                }
            });

            JButton deleteBtn = new JButton("DELETE");
            styleButton(deleteBtn, new Color(200, 50, 50));
            deleteBtn.addActionListener(e -> {
                String selected = profileList.getSelectedValue();
                if (selected != null) {
                    int confirm = JOptionPane.showConfirmDialog(this, "Delete " + selected + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        SaveManager.deleteProfile(selected);
                        refreshList();
                    }
                }
            });

            btnPanel.add(createBtn); btnPanel.add(deleteBtn); btnPanel.add(playBtn);
            add(btnPanel, BorderLayout.SOUTH);
        }
        private void refreshList() {
            listModel.clear();
            for (String s : SaveManager.getProfiles()) listModel.addElement(s);
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        protected void configureScrollBarColors() { this.thumbColor = new Color(80, 80, 80); this.trackColor = new Color(30, 30, 30); }
        protected JButton createDecreaseButton(int o) { return createZero(); } protected JButton createIncreaseButton(int o) { return createZero(); }
        private JButton createZero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
    }
}