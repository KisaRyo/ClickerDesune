import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D; 
import java.io.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random; 

import core.Wallet;
import core.IsekaiManager;
import generators.*;
import utils.ImageLoader;
import utils.SaveManager;

public class ClickerDesuNe {
    
    // --- GAME STATE ---
    private static String currentProfile = "Guest-kun"; 
    private static Wallet wallet = new Wallet();
    private static List<Generator> inventory = new ArrayList<>();
    
    // --- ANIME CATALOG ---
    // We keep a list of shop items to map them to buttons easily
    private static List<Generator> shopItems = new ArrayList<>();
    private static List<JButton> shopButtons = new ArrayList<>();
    
    private static boolean hasSeenIsekai = false;
    private static double clickHeat = 0; 
    private static boolean showIntensity = true;

    // --- THEME ENGINE ---
    record Theme(
        String name, 
        Color bgBase,       
        Color panelBg,      
        Color accent,       
        Color textMain,     
        Color textSub,      
        Color btnBg,        
        Color btnText       
    ) {}
    
    private static final Theme THEME_DARK = new Theme("Akiba Night", 
        new Color(25, 25, 30), new Color(40, 40, 45, 230), new Color(255, 105, 180), 
        new Color(240, 240, 240), new Color(180, 180, 180), new Color(60, 60, 70), new Color(255, 255, 255));

    private static final Theme THEME_MIKU = new Theme("Miku", 
        new Color(230, 245, 245), new Color(255, 255, 255, 220), new Color(0, 150, 136), 
        new Color(40, 50, 60), new Color(100, 110, 120), new Color(255, 255, 255), new Color(0, 120, 110));

    private static final Theme THEME_DEMON = new Theme("Demon Corps", 
        new Color(10, 12, 10), new Color(20, 35, 20, 240), new Color(50, 205, 50), 
        new Color(220, 255, 220), new Color(100, 150, 100), new Color(15, 25, 15), new Color(50, 205, 50));

    private static final Theme THEME_PIRATE = new Theme("Pirate King", 
        new Color(10, 25, 45), new Color(20, 40, 70, 200), new Color(255, 215, 0), 
        new Color(255, 250, 240), new Color(135, 206, 250), new Color(0, 50, 80), new Color(255, 215, 0));

    private static final Theme THEME_ALCHEMY = new Theme("Alchemy", 
        new Color(50, 50, 50), new Color(70, 70, 70, 230), new Color(220, 20, 60), 
        new Color(240, 240, 240), new Color(192, 192, 192), new Color(90, 90, 90), new Color(255, 255, 255));

    private static Theme currentTheme = THEME_DARK;

    // --- ICONS ---
    private static final String URL_YEN     = "https://img.icons8.com/fluency/240/yen-coin.png";
    private static final String URL_PEN     = "https://img.icons8.com/fluency/96/wacom-tablet.png";
    private static final String URL_GACHA   = "https://img.icons8.com/fluency/96/gumball-machine.png"; 
    private static final String URL_CAFE    = "https://img.icons8.com/fluency/96/tea.png";
    private static final String URL_STUDIO  = "https://img.icons8.com/fluency/96/monitor.png"; 
    private static final String URL_SHRINE  = "https://img.icons8.com/fluency/96/torii.png"; 
    private static final String URL_TRUCK   = "https://img.icons8.com/fluency/96/shipped.png";
    private static final String URL_GEAR    = "https://img.icons8.com/fluency/96/settings.png";

    // --- UI COMPONENTS ---
    private static JFrame frame;
    private static ParticlePanel mainPanel; 
    private static JPanel rightContainer;  
    private static JPanel headerPanel;
    private static JLabel balanceLabel;
    private static JLabel mpsLabel;
    private static JLabel cpsLabel; 
    private static JLabel isekaiLabel;
    private static JTextArea gameLog;
    private static JLabel bigButtonLabel;
    private static JPanel shopContentPanel;
    private static JScrollPane shopScrollPane;
    private static JScrollPane logScrollPane;
    private static JButton isekaiBtn; // Keep reference to toggle visibility

    public static void main(String[] args) {
        new ProfileMenu(null).setVisible(true);
    }

    public static void launchGame(String profileName) {
        currentProfile = profileName;
        resetGameState(); 
        
        Properties props = SaveManager.loadProfileData(currentProfile);
        if (!props.isEmpty()) {
            wallet.earn(Double.parseDouble(props.getProperty("balance", "0")));
            IsekaiManager.setPoints(Integer.parseInt(props.getProperty("isekai_points", "0")));
            
            String t = props.getProperty("theme", "Akiba Night");
            if(t.equals("Demon Corps")) currentTheme = THEME_DEMON;
            else if(t.equals("Pirate King")) currentTheme = THEME_PIRATE;
            else if(t.equals("Alchemy")) currentTheme = THEME_ALCHEMY;
            else if(t.equals("Miku")) currentTheme = THEME_MIKU;
            else currentTheme = THEME_DARK;

            // Restore counts
            restoreItems(Integer.parseInt(props.getProperty("count_pen", "0")), shopItems.get(0), "Pen");
            restoreItems(Integer.parseInt(props.getProperty("count_gacha", "0")), shopItems.get(1), "Gacha");
            restoreItems(Integer.parseInt(props.getProperty("count_cafe", "0")), shopItems.get(2), "Cafe");
            restoreItems(Integer.parseInt(props.getProperty("count_studio", "0")), shopItems.get(3), "Studio");
            restoreItems(Integer.parseInt(props.getProperty("count_shrine", "0")), shopItems.get(4), "Shrine");
        }

        if (frame == null) {
            createGameWindow();
        } else {
            frame.setVisible(true);
            applyTheme(currentTheme);
            log("Okaeri, " + currentProfile + "-sama!");
        }
        
        // Initial button text update
        updateShopUI();
    }

    private static void saveCurrentState() {
        SaveManager.saveGame(currentProfile, wallet, inventory, currentTheme.name());
    }

    private static void createGameWindow() {
        frame = new JFrame("Clicker Desu Ne: " + currentProfile);
        frame.setSize(1100, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { saveCurrentState(); }});
        
        mainPanel = new ParticlePanel();
        mainPanel.setLayout(new BorderLayout(20, 20)); 
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25)); 
        frame.setContentPane(mainPanel);

        // --- HEADER ---
        headerPanel = new StyledPanel();
        headerPanel.setLayout(new BorderLayout());
        
        JPanel statsBox = new JPanel(); 
        statsBox.setLayout(new BoxLayout(statsBox, BoxLayout.Y_AXIS));
        statsBox.setOpaque(false);
        
        balanceLabel = new JLabel("¥ 0");
        balanceLabel.setForeground(currentTheme.accent);
        balanceLabel.setFont(new Font("Verdana", Font.BOLD, 48));
        
        mpsLabel = new JLabel("¥ 0.0 / sec");
        mpsLabel.setForeground(currentTheme.textMain); 
        mpsLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        
        cpsLabel = new JLabel("Spirit: 0");
        cpsLabel.setForeground(currentTheme.accent); 
        cpsLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        isekaiLabel = new JLabel("World: 1");
        isekaiLabel.setForeground(currentTheme.textSub);
        isekaiLabel.setFont(new Font("Verdana", Font.ITALIC, 14));
        
        statsBox.add(balanceLabel); statsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        statsBox.add(mpsLabel); statsBox.add(Box.createRigidArea(new Dimension(0, 10)));
        statsBox.add(cpsLabel); statsBox.add(Box.createRigidArea(new Dimension(0, 5)));
        statsBox.add(isekaiLabel);
        
        headerPanel.add(statsBox, BorderLayout.WEST);

        JButton settingsBtn = new JButton(); 
        settingsBtn.setIcon(ImageLoader.getIcon(URL_GEAR, 28, "⚙️")); 
        styleButton(settingsBtn, currentTheme.btnBg, currentTheme.btnText); 
        settingsBtn.setPreferredSize(new Dimension(50, 50)); 
        settingsBtn.addActionListener(e -> openSettingsMenu());
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(settingsBtn);
        
        headerPanel.add(btnWrapper, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- CENTER ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        ImageIcon mainIcon = ImageLoader.getIcon(URL_YEN, 240, "💴"); 
        bigButtonLabel = new JLabel(mainIcon);
        bigButtonLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bigButtonLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                bigButtonLabel.setIcon(ImageLoader.getIcon(URL_YEN, 230, "💴")); 
                wallet.earn(getCursorPower());
                clickHeat += 1.0; 
                updateUI();
                updateShopUI(); // Check affordability on click
            }
            public void mouseReleased(MouseEvent e) { bigButtonLabel.setIcon(mainIcon); }
        });

        centerPanel.add(bigButtonLabel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL (MARKET) ---
        rightContainer = new StyledPanel();
        rightContainer.setLayout(new BorderLayout());
        
        // UPDATED: Widen the container to hide horizontal scrollbar
        rightContainer.setPreferredSize(new Dimension(460, 0)); 

        JLabel shopTitle = new JLabel("AKIHABARA");
        shopTitle.setFont(new Font("Verdana", Font.BOLD, 20));
        shopTitle.setForeground(currentTheme.textMain);
        shopTitle.setBorder(new EmptyBorder(10, 0, 15, 0));
        shopTitle.setHorizontalAlignment(SwingConstants.CENTER);
        rightContainer.add(shopTitle, BorderLayout.NORTH);

        shopContentPanel = new JPanel();
        shopContentPanel.setLayout(new BoxLayout(shopContentPanel, BoxLayout.Y_AXIS));
        shopContentPanel.setOpaque(false);
        shopContentPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        // Initialize buttons ONCE
        initializeShopButtons();

        shopScrollPane = new JScrollPane(shopContentPanel);
        shopScrollPane.setBorder(null);
        shopScrollPane.getViewport().setOpaque(false);
        shopScrollPane.setOpaque(false);
        
        // UPDATED: Never show horizontal scrollbar
        shopScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        shopScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI(currentTheme.accent, currentTheme.panelBg));
        
        rightContainer.add(shopScrollPane, BorderLayout.CENTER);
        mainPanel.add(rightContainer, BorderLayout.EAST);

        // --- BOTTOM LOG ---
        StyledPanel logPanel = new StyledPanel();
        logPanel.setLayout(new BorderLayout());
        
        gameLog = new JTextArea(5, 50);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        gameLog.setForeground(currentTheme.textSub);
        gameLog.setOpaque(false);
        
        logScrollPane = new JScrollPane(gameLog);
        logScrollPane.setBorder(null);
        logScrollPane.setOpaque(false);
        logScrollPane.getViewport().setOpaque(false);
        logScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI(currentTheme.accent, currentTheme.panelBg));
        
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        // --- TIMERS ---
        
        // 1. Logic Timer (Money & Game State) - 100ms
        new Timer(100, e -> {
            double passive = calculatePassiveMPS() / 10.0;
            if(passive > 0) wallet.earn(passive); 
            if (clickHeat > 0) { clickHeat *= 0.90; if (clickHeat < 0.1) clickHeat = 0; }
            updateUI();
            checkIsekaiUnlock();
            
            // Optimization: Only update shop text occasionally or on interaction, 
            // but running it here is fine if we aren't deleting components.
            updateShopUI(); 
        }).start();

        // 2. Animation Timer (Visuals Only) - 16ms (~60 FPS) for smooth blossoms
        new Timer(16, e -> {
            mainPanel.repaint(); 
        }).start();

        // 3. Auto-Save - 30s
        new Timer(30000, e -> { saveCurrentState(); log("Progress Saved."); }).start();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --- PARTICLE BACKGROUND ---
    static class ParticlePanel extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Random rand = new Random();
        public ParticlePanel() { for (int i = 0; i < 40; i++) particles.add(new Particle()); }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(currentTheme.bgBase);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(new Color(128, 128, 128, 20)); 
            for (int x = 0; x < getWidth(); x += 50) g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 50) g2.drawLine(0, y, getWidth(), y);
            
            Color ac = currentTheme.accent;
            g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 80)); 
            for (Particle p : particles) {
                p.y += p.speed;
                p.x += Math.sin(p.y * 0.02) * 0.5; 
                if (p.y > getHeight()) { p.y = -10; p.x = rand.nextInt(getWidth()); } 
                g2.fill(new Ellipse2D.Float(p.x, p.y, p.size, p.size));
            }
        }
        private class Particle {
            float x = rand.nextInt(1000); float y = rand.nextInt(800);
            float speed = 0.5f + rand.nextFloat() * 1.5f; float size = 4 + rand.nextFloat() * 6;
        }
    }

    // --- UPDATES & THEME APPLICATION ---
    private static void applyTheme(Theme t) {
        currentTheme = t;
        balanceLabel.setForeground(currentTheme.accent);
        mpsLabel.setForeground(currentTheme.textMain);
        cpsLabel.setForeground(currentTheme.accent);
        isekaiLabel.setForeground(currentTheme.textSub);
        gameLog.setForeground(currentTheme.textSub);
        
        if(shopScrollPane != null) shopScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI(t.accent, t.panelBg));
        if(logScrollPane != null) logScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI(t.accent, t.panelBg));

        // Update existing buttons with new colors
        for(JButton btn : shopButtons) {
            styleButton(btn, currentTheme.btnBg, currentTheme.btnText);
        }
        if(isekaiBtn != null) styleButton(isekaiBtn, new Color(255, 80, 80), Color.WHITE);

        headerPanel.repaint();
        rightContainer.repaint();
        mainPanel.repaint();
    }

    private static void openSettingsMenu() {
        JDialog dialog = new JDialog(frame, "Settings", true);
        dialog.setSize(300, 450); 
        dialog.setLayout(new GridLayout(7, 1, 8, 8)); 
        dialog.getContentPane().setBackground(currentTheme.bgBase); 
        dialog.setLocationRelativeTo(frame);

        JButton switchBtn = createSettingsBtn("Logout / Quit", new Color(200, 60, 60), Color.WHITE);
        switchBtn.addActionListener(e -> {
            saveCurrentState(); dialog.dispose(); frame.setVisible(false); new ProfileMenu(null).setVisible(true);
        });
        dialog.add(switchBtn);

        JLabel lbl = new JLabel("SELECT THEME", SwingConstants.CENTER);
        lbl.setForeground(currentTheme.textMain);
        dialog.add(lbl);

        dialog.add(createThemeBtn("Akiba Night (Dark)", THEME_DARK));
        dialog.add(createThemeBtn("Miku (Light)", THEME_MIKU));
        dialog.add(createThemeBtn("Demon Corps (Green)", THEME_DEMON));
        dialog.add(createThemeBtn("Pirate King (Gold)", THEME_PIRATE));
        dialog.add(createThemeBtn("Alchemy (Red)", THEME_ALCHEMY));

        dialog.setVisible(true);
    }
    
    private static JButton createThemeBtn(String name, Theme t) {
        JButton btn = createSettingsBtn(name, t.btnBg, t.btnText);
        btn.setBorder(BorderFactory.createLineBorder(t.accent, 2));
        btn.addActionListener(e -> applyTheme(t));
        return btn;
    }
    
    private static JButton createSettingsBtn(String text, Color bg, Color txt) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(txt);
        btn.setFocusPainted(false);
        return btn;
    }

    // --- STYLED PANEL ---
    static class StyledPanel extends JPanel {
        public StyledPanel() { setOpaque(false); setBorder(new EmptyBorder(15, 15, 15, 15)); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentTheme.panelBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(currentTheme.accent.getRed(), currentTheme.accent.getGreen(), currentTheme.accent.getBlue(), 60));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- OPTIMIZED SHOP LOGIC ---
    
    // 1. Run ONCE at startup
    private static void initializeShopButtons() {
        shopContentPanel.removeAll();
        shopButtons.clear();
        
        // Define Images
        String[] urls = {URL_PEN, URL_GACHA, URL_CAFE, URL_STUDIO, URL_SHRINE};
        String[] emojis = {"✒️", "🔮", "☕", "🖥️", "⛩️"};
        
        for (int i = 0; i < shopItems.size(); i++) {
            Generator gen = shopItems.get(i);
            JButton btn = new JButton();
            btn.setIcon(ImageLoader.getIcon(urls[i], 48, emojis[i]));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setIconTextGap(20);
            
            // Action Listener
            btn.addActionListener(e -> { 
                if(wallet.spend(gen.getCost())) { 
                     gen.upgrade(); 
                     // Add new instance to inventory
                     try { inventory.add(gen.getClass().getDeclaredConstructor().newInstance()); } catch(Exception ex) {}
                     log("Acquired " + gen.getClass().getSimpleName() + "!"); 
                     updateUI(); 
                     updateShopUI(); 
                } else { log("Not enough Yen!"); } 
            });
            
            styleButton(btn, currentTheme.btnBg, currentTheme.btnText);
            
            shopContentPanel.add(btn);
            shopContentPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            shopButtons.add(btn);
        }
        
        // Add Isekai Button (Hidden initially)
        isekaiBtn = new JButton("TRUCK-KUN (¥5k)");
        isekaiBtn.setIcon(ImageLoader.getIcon(URL_TRUCK, 32, "🚚"));
        styleButton(isekaiBtn, new Color(255, 80, 80), Color.WHITE);
        isekaiBtn.addActionListener(e -> performIsekai());
        isekaiBtn.setVisible(false);
        
        shopContentPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        shopContentPanel.add(isekaiBtn);
    }

    // 2. Run EVERY TICK (Only updates text, doesn't delete buttons)
    private static void updateShopUI() {
        for(int i=0; i<shopItems.size(); i++) {
            Generator gen = shopItems.get(i);
            JButton btn = shopButtons.get(i);
            
            double baseRate = 0; String unit = "/ sec";
            if(i==0) { baseRate = 1.0; unit = "click power"; }
            else if(i==1) baseRate = 6.0; else if(i==2) baseRate = 50.0;
            else if(i==3) baseRate = 350.0; else if(i==4) baseRate = 1500.0;

            String html = String.format("<html><div style='text-align: left; width: 220px;'>" +
                "<b>%s</b> <span style='font-size:10px; opacity:0.7;'>(Lvl %d)</span><br>" +
                "<font color='%s'>¥%,.0f</font><br>" +
                "<span style='font-size:10px; opacity:0.8;'>+ %,.1f %s</span></div></html>", 
                gen.getClass().getSimpleName(), gen.getLevel(), toHexString(currentTheme.accent), gen.getCost(), baseRate * IsekaiManager.getMultiplier(), unit);
            
            btn.setText(html);
            
            // Logic: Disable if can't afford (Optional, but good for UI)
            // btn.setEnabled(wallet.getBalance() >= gen.getCost());
        }
        
        // Toggle Truck-kun visibility
        if(hasSeenIsekai) isekaiBtn.setVisible(true);
    }

    private static void styleButton(JButton btn, Color bg, Color text) {
        btn.setBackground(bg); 
        btn.setForeground(text); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        Color borderColor = new Color(text.getRed(), text.getGreen(), text.getBlue(), 50);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        btn.setMaximumSize(new Dimension(440, 85)); // WIDER BUTTONS
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static String toHexString(Color c) { return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()); }

    // --- STANDARD METHODS ---
    private static void restoreItems(int count, Generator proto, String type) {
        for (int i = 0; i < count; i++) {
            proto.upgrade(); 
            try { inventory.add(proto.getClass().getDeclaredConstructor().newInstance()); } catch(Exception e) {}
        }
    }
    private static void resetGameState() {
        wallet = new Wallet(); inventory.clear();
        shopItems.clear();
        shopItems.add(new MangaPen()); shopItems.add(new GachaMachine()); shopItems.add(new MaidCafe()); 
        shopItems.add(new AnimeStudio()); shopItems.add(new Shrine());
        hasSeenIsekai = false; clickHeat = 0; IsekaiManager.reset();
        
        // Re-init the UI buttons for new state
        if(shopContentPanel != null) initializeShopButtons();
    }
    
    private static void performIsekai() {
        if(wallet.getBalance() >= 5000) {
            int pointsToAdd = (int)(wallet.getBalance() / 1000); 
            int currentLevel = IsekaiManager.getLevel(); 
            resetGameState(); 
            IsekaiManager.setPoints(currentLevel + pointsToAdd); 
            log("ISEKAI'D! Reborn with Lv." + IsekaiManager.getLevel() + " power!"); 
            updateUI(); 
            updateShopUI();
            saveCurrentState(); 
        } else { log("Need ¥5000 to summon Truck-kun!"); }
    }
    
    private static void updateUI() {
        balanceLabel.setText("¥ " + String.format("%,.0f", wallet.getBalance())); 
        mpsLabel.setText("¥ " + String.format("%,.1f", calculateDisplayMPS()) + " / sec");
        cpsLabel.setText("Spirit: " + String.format("%.1f", clickHeat));
        cpsLabel.setVisible(showIntensity);
        isekaiLabel.setText("World Lv: " + IsekaiManager.getLevel() + " (x" + String.format("%.1f", IsekaiManager.getMultiplier()) + ")");
        if (frame != null) frame.setTitle("Clicker Desu Ne: " + currentProfile);
    }
    private static double getCursorPower() { return (1 + shopItems.get(0).calculateBaseProduction()) * IsekaiManager.getMultiplier(); }
    private static double calculatePassiveMPS() { double t = 0; for(Generator g : inventory) if(!(g instanceof MangaPen)) t += g.getFinalProduction(); return t; }
    private static double calculateDisplayMPS() { return calculatePassiveMPS() + (clickHeat * getCursorPower()); }
    private static void checkIsekaiUnlock() { if (!hasSeenIsekai && wallet.getBalance() >= 5000) { hasSeenIsekai = true; log("A TRUCK IS APPROACHING..."); updateShopUI(); } }
    private static void log(String msg) { gameLog.append(" > " + msg + "\n"); gameLog.setCaretPosition(gameLog.getDocument().getLength()); }

    // --- PROFILE MENU ---
    static class ProfileMenu extends JDialog {
        private JList<String> profileList;
        private DefaultListModel<String> listModel;
        public ProfileMenu(JFrame parent) {
            super(parent, "Select Profile", true); setSize(400, 500); setLocationRelativeTo(null); setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(30, 30, 35));
            JLabel title = new JLabel("Clicker Desu Ne", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(new Color(255, 105, 180)); title.setBorder(new EmptyBorder(30, 0, 20, 0)); add(title, BorderLayout.NORTH);
            listModel = new DefaultListModel<>(); refreshList();
            profileList = new JList<>(listModel); profileList.setBackground(new Color(40, 40, 45)); profileList.setForeground(Color.WHITE);
            profileList.setFont(new Font("Segoe UI", Font.PLAIN, 18)); profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scroll = new JScrollPane(profileList); scroll.setBorder(new EmptyBorder(10, 40, 20, 40)); scroll.getViewport().setOpaque(false); scroll.setOpaque(false); add(scroll, BorderLayout.CENTER);
            JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0)); btnPanel.setOpaque(false); btnPanel.setBorder(new EmptyBorder(0, 40, 30, 40));
            JButton playBtn = createStyledBtn("START", new Color(0, 180, 100)); playBtn.addActionListener(e -> { String s = profileList.getSelectedValue(); if (s != null) { dispose(); launchGame(s); }});
            JButton createBtn = createStyledBtn("NEW", new Color(0, 120, 200)); createBtn.addActionListener(e -> { String n = JOptionPane.showInputDialog(this, "Hero Name:"); if (n!=null && !n.trim().isEmpty()) { SaveManager.saveGame(n.trim(), wallet, inventory, currentTheme.name()); refreshList(); }});
            JButton deleteBtn = createStyledBtn("ERASE", new Color(200, 60, 60)); deleteBtn.addActionListener(e -> { String s = profileList.getSelectedValue(); if (s!=null && JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", 0) == 0) { SaveManager.deleteProfile(s); refreshList(); }});
            btnPanel.add(createBtn); btnPanel.add(deleteBtn); btnPanel.add(playBtn); add(btnPanel, BorderLayout.SOUTH);
        }
        private JButton createStyledBtn(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setFocusPainted(false); b.setBorder(BorderFactory.createEmptyBorder(10,0,10,0)); return b; }
        private void refreshList() { listModel.clear(); for (String s : SaveManager.getProfiles()) listModel.addElement(s); }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        private Color thumb, track;
        public ModernScrollBarUI(Color t, Color tr) { this.thumb = t; this.track = tr; }
        protected void configureScrollBarColors() { this.thumbColor = thumb; this.trackColor = track; }
        protected JButton createDecreaseButton(int o) { return createZero(); } protected JButton createIncreaseButton(int o) { return createZero(); }
        private JButton createZero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
    }
}