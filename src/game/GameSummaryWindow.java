package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class GameSummaryWindow extends JDialog {
    private ArrayList<MenuElement> elements;
    private ArrayList<Player> players;
    private MenuElement[] winnerImgs = new MenuElement[3];
    private MenuElement[] scoreTexts = new MenuElement[3];
    private int[] targetScores = new int[3];
    private int[] displayedScores = new int[3];
    private int animPhase = 0;
    private javax.swing.Timer animTimer;
    private boolean phaseStarted = false;
    private long phaseStartMs = 0L;
    private int revealDelayMs = 400;
    private boolean announcePlayed = false;
    private long announceStartMs = 0L;

    public GameSummaryWindow(java.util.List<Player> players) {
        super((Frame) null, true);
        this.players = new ArrayList<>(players);
        setUndecorated(true);
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        try { SoundManager.getInstance().playSFX(GameConfig.LOCATION_OPEN_SOUND); } catch (Exception ignored) {}
        setContentPane(new ContentPanel());
        pack();
        centerWindow();
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private class ContentPanel extends JPanel {
        private java.awt.image.BufferedImage bgImage;
        public ContentPanel() {
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            elements = new ArrayList<>();
            loadMenuElements(this);
            startRevealAnimation();
            try {
                String path = "assets"+File.separator+"leader"+File.separator+"bg.png";
                File f = new File(path);
                if (!f.exists()) f = new File(System.getProperty("user.dir")+File.separator+path);
                if (f.exists()) {
                    bgImage = javax.imageio.ImageIO.read(f);
                }
            } catch (Exception ignored) {}
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    handleHover(e.getX(), e.getY());
                }
            });
        }

        private void loadMenuElements(JPanel panel) {
            MenuElement img;
            MenuElement text;
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"LOC-PawnShop-Gameboy.png", -216.0, -147.0, 713.4, 643.5);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Panel-Background-Small.png", 336.7, 144.0, 1220.7, 860.6);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"QuestNotepad_Small.png", 582.0, -2.0, 756.0, 274.0);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"SpecialItem-Tier1 Placeholder.png", 582.0, 385.1, 190.1, 249.9);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"SpecialItem-Tier1 Placeholder.png", 864.9, 385.1, 190.1, 249.9);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"SpecialItem-Tier1 Placeholder.png", 1158.0, 385.1, 190.1, 249.9);
            elements.add(img);

            int winnerEducationId = getWinnerIdByMetric("education");
            int winnerSkillId = getWinnerIdByMetric("skill");
            int winnerMoneyId = getWinnerIdByMetric("money");

            winnerImgs[0] = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+ winnerEducationId + ".png", 606.0, 321.0, 118.0, 246.0);
            winnerImgs[0].setVisibility(false);
            elements.add(winnerImgs[0]);
            winnerImgs[1] = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+ winnerSkillId + ".png", 888.0, 321.0, 118.0, 246.0);
            winnerImgs[1].setVisibility(false);
            elements.add(winnerImgs[1]);
            winnerImgs[2] = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+ winnerMoneyId + ".png", 1182.0, 321.0, 118.0, 246.0);
            winnerImgs[2].setVisibility(false);
            elements.add(winnerImgs[2]);

            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Price Tag Background.png", 528.0, 635.0, 244.4, 75.2);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Price Tag Background.png", 830.0, 636.6, 234.0, 72.0);
            elements.add(img);
            text = new MenuElement("การเรียน", 607.4, 683.0, 32);
            text.setTextColor(new Color(30, 30, 40));
            elements.add(text);
            text = new MenuElement("การงาน", 919.4, 683.0, 32);
            text.setTextColor(new Color(30, 30, 40));
            elements.add(text);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Price Tag Background.png", 1146.5, 639.8, 213.2, 65.6);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Icon-Job-Tier-4-Badge.png", 479.0, 608.8, 127.5, 127.5);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Icon-Job-Tier-2-Badge #2849.png", 791.0, 608.4, 128.4, 128.4);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Icon-Job-Tier-5-Badge.png", 1082.0, 607.6, 130.0, 130.0);
            elements.add(img);
            text = new MenuElement("การเงิน", 1212.0, 683.0, 32);
            text.setTextColor(new Color(30, 30, 40));
            elements.add(text);

            targetScores[0] = getScoreByMetric("education", winnerEducationId);
            targetScores[1] = getScoreByMetric("skill", winnerSkillId);
            targetScores[2] = getScoreByMetric("money", winnerMoneyId);
            displayedScores[0] = 0; displayedScores[1] = 0; displayedScores[2] = 0;
            scoreTexts[0] = new MenuElement("", 606.5, 749.0, 32);
         
            scoreTexts[0].setTextColor(new Color(30, 30, 40));
            scoreTexts[0].setVisibility(false);
            elements.add(scoreTexts[0]);
            scoreTexts[1] = new MenuElement("", 919.4, 749.0, 32);
            scoreTexts[1].setVisibility(false);
            scoreTexts[1].setTextColor(new Color(30, 30, 40));
            elements.add(scoreTexts[1]);
            scoreTexts[2] = new MenuElement("", 1200.0, 749.0, 32);
            scoreTexts[2].setVisibility(false);
            scoreTexts[2].setTextColor(new Color(30, 30, 40));
            elements.add(scoreTexts[2]);
            text = new MenuElement("ผู้เล่นที่ใช้ชีวิตได้คุ้มค่าที่สุดในแต่ละด้าน", 621.0, 166.0, 34);
            text.setTextColor(new Color(30, 30, 40));
            elements.add(text);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Tut-Objectives-Trophy.png", 1198.0, 104.0, 91.8, 96.1);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Robot-Head-05 #44668.png", 1482.0, 144.0, 473.3, 338.9);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"ui"+File.separator+"button"+File.separator+"Button-Small-Blue.png", 806.7, 852.0, 205.5, 109.8);
            img.setButtonId("home_btn");
            img.setVisibility(false);
            elements.add(img);
            img = new MenuElement(MenuElement.ElementType.IMAGE, "assets"+File.separator+"leader"+File.separator+"Icon-Home.png", 860.8, 858.2, 97.4, 97.4);
            img.setButtonId("home_btn");
            img.setVisibility(false);
            elements.add(img);
        }

        private void startRevealAnimation() {
            animPhase = 0;
            phaseStarted = false;
            phaseStartMs = 0L;
            announcePlayed = false;
            announceStartMs = 0L;
            if (animTimer != null) animTimer.stop();
            animTimer = new javax.swing.Timer(50, e -> {
                if (animPhase >= 3) {
                    for (MenuElement el : elements) {
                        if ("home_btn".equals(el.getButtonId())) el.setVisibility(true);
                    }
                    ((javax.swing.Timer)e.getSource()).stop();
                    repaint();
                    return;
                }

                if (!phaseStarted) {
                    scoreTexts[animPhase].setVisibility(true);
                    displayedScores[animPhase] = 0;
                    try { SoundManager.getInstance().playSFX(GameConfig.SCORE_FILL_SOUND); } catch (Exception ignored) {}
                    phaseStarted = true;
                    phaseStartMs = System.currentTimeMillis();
                    announcePlayed = false;
                    announceStartMs = 0L;
                }

                int target = targetScores[animPhase];
                int step = Math.max(1, Math.max(5, target / 30));
                if (displayedScores[animPhase] < target) {
                    displayedScores[animPhase] = Math.min(target, displayedScores[animPhase] + step);
                    scoreTexts[animPhase].setText(displayedScores[animPhase] + " แต้ม");
                } else {
                    if (!announcePlayed) {
                        try { SoundManager.getInstance().playSFX(GameConfig.SCORE_TYPE_ANNOUNCED_SOUND); } catch (Exception ignored) {}
                        announcePlayed = true;
                        announceStartMs = System.currentTimeMillis();
                    }
                    if (!winnerImgs[animPhase].getVisibility()) {
                        if (System.currentTimeMillis() - announceStartMs >= 200) {
                            winnerImgs[animPhase].setVisibility(true);
                        }
                    } else {
                        if (System.currentTimeMillis() - announceStartMs >= 3000) {
                            animPhase++;
                            phaseStarted = false;
                        }
                    }
                }
                repaint();
            });
            animTimer.start();
        }

        private int getWinnerIdByMetric(String metric) {
            int winnerId = 1;
            int best = Integer.MIN_VALUE;
            for (Player p : players) {
                int v = 0;
                if ("education".equals(metric)) v = p.getEducation();
                else if ("skill".equals(metric)) v = p.getSkill();
                else if ("money".equals(metric)) v = p.getMoney();
                if (v > best) {
                    best = v;
                    winnerId = p.getPlayerId();
                }
            }
            return Math.max(1, Math.min(4, winnerId));
        }

        private int getScoreByMetric(String metric, int playerId) {
            for (Player p : players) {
                if (p.getPlayerId() == playerId) {
                    if ("education".equals(metric)) return p.getEducation();
                    if ("skill".equals(metric)) return p.getSkill();
                    if ("money".equals(metric)) return p.getMoney();
                }
            }
            return 0;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (bgImage != null) {
                g2d.drawImage(bgImage, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT, null);
            } else {
                g2d.setColor(new Color(235, 240, 245));
                g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            }
            for (MenuElement el : elements) {
                el.render(g2d);
            }
        }

        private void handleHover(int mx, int my) {
            boolean any = false;
            for (int i = elements.size() - 1; i >= 0; i--) {
                MenuElement el = elements.get(i);
                boolean h = el.contains(mx, my);
                el.setHovered(h);
                if (h && "home_btn".equals(el.getButtonId()) && el.getVisibility()) {
                    any = true;
                }
            }

            for (MenuElement el : elements) {
                if ("home_btn".equals(el.getButtonId()) && el.getVisibility()) {
                    String p = el.getImagePath();
                    if (p != null) {
                        String norm = p.replace('\\','/');
                        boolean isButtonBg = norm.contains("/ui/button/");
                        if (isButtonBg) {
                            if (any) {
                                el.setImagePath("assets"+File.separator+"ui"+File.separator+"button"+File.separator+"Button-Small-Pink.png");
                            } else {
                                el.setImagePath("assets"+File.separator+"ui"+File.separator+"button"+File.separator+"Button-Small-Blue.png");
                            }
                        }
                    }
                }
            }

            setCursor(any ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        private void handleClick(int mx, int my) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                MenuElement el = elements.get(i);
                if (el.contains(mx, my) && "home_btn".equals(el.getButtonId()) && el.getVisibility()) {
                    try {
                        SoundManager.getInstance().playSFX(GameConfig.LOCATION_CLOSE_SOUND);
                        network.NetworkManager.getInstance().sendMessage("RESET_GAME");
                        network.NetworkManager.getInstance().disconnect();
                    } catch (Exception ignored) {}
                    java.awt.Window[] windows = java.awt.Window.getWindows();
                    for (java.awt.Window w : windows) {
                        if (w instanceof GameWindow) {
                            w.dispose();
                        }
                    }
                    SwingUtilities.getWindowAncestor(this).dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainMenu menu = new MainMenu();
                        menu.setVisible(true);
                    });
                    return;
                }
            }
        }
    }
}


