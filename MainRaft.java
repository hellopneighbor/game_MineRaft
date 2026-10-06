import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainRaft extends JFrame {

    public enum GameState { MENU, WORLD_SELECT, GAME, PAUSE, SETTINGS }

    private GameState currentState = GameState.MENU;

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MenuPanel menuPanel;
    private WorldSelectPanel worldSelectPanel;
    private Game2DPanel game2DPanel;
    private SettingsPanel settingsPanel;

    private float zoomLevel = 1.0f;
    private String playerName = "Steve";

    public MainRaft() {
        setTitle("mainRaft 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        Logger.init();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this);
        worldSelectPanel = new WorldSelectPanel(this);
        game2DPanel = new Game2DPanel(this);
        settingsPanel = new SettingsPanel(this);

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(worldSelectPanel, "WORLDS");
        mainContainer.add(game2DPanel, "GAME");
        mainContainer.add(settingsPanel, "SETTINGS");

        add(mainContainer);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public GameState getCurrentState() { return currentState; }
    public void setCurrentState(GameState state) { this.currentState = state; }

    public float getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(float zoom) { this.zoomLevel = Math.max(0.5f, Math.min(2.0f, zoom)); }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String name) { this.playerName = name; }

    public void showMenu() { cardLayout.show(mainContainer, "MENU"); menuPanel.requestFocusInWindow(); }
    public void showWorldSelect() { cardLayout.show(mainContainer, "WORLDS"); worldSelectPanel.refreshWorldList(); worldSelectPanel.requestFocusInWindow(); }
    public void startGame() { cardLayout.show(mainContainer, "GAME"); game2DPanel.requestFocusInWindow(); }
    public void showSettings() { cardLayout.show(mainContainer, "SETTINGS"); settingsPanel.requestFocusInWindow(); }

    public Game2DPanel getGamePanel() { return game2DPanel; }

    public static File getSavesFolder() {
        File savesDir = new File(System.getProperty("user.home"), ".mainraft" + File.separator + "saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
        return savesDir;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainRaft::new);
    }
}

class MenuPanel extends JPanel {
    private final MainRaft frame;
    private Rectangle btnPlay, btnNick, btnRandomNick, btnSettings, btnExit;
    private final String[] defaultNicks = {"Steve", "Alex", "CraftMaster", "Miner2026", "RaftExplorer"};

    public MenuPanel(MainRaft frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(800, 480));
        setBackground(new Color(40, 40, 40));

        int btnW = 280, btnH = 40;
        int centerX = (800 - btnW) / 2;

        btnPlay       = new Rectangle(centerX, 130, btnW, btnH);
        btnNick       = new Rectangle(centerX, 190, btnW, btnH);
        btnRandomNick = new Rectangle(centerX, 250, btnW, btnH);
        btnSettings   = new Rectangle(centerX, 310, btnW, btnH);
        btnExit       = new Rectangle(centerX, 370, btnW, btnH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (btnPlay.contains(p)) {
                    frame.setCurrentState(MainRaft.GameState.WORLD_SELECT);
                    frame.showWorldSelect();
                } else if (btnNick.contains(p)) {
                    String input = JOptionPane.showInputDialog(frame, "Введите никнейм:", frame.getPlayerName());
                    if (input != null && !input.trim().isEmpty()) {
                        frame.setPlayerName(input.trim());
                        Logger.log("Никнейм изменен на: " + frame.getPlayerName());
                        repaint();
                    }
                } else if (btnRandomNick.contains(p)) {
                    String randNick = defaultNicks[new Random().nextInt(defaultNicks.length)];
                    frame.setPlayerName(randNick);
                    Logger.log("Сгенерирован случайный ник: " + randNick);
                    repaint();
                } else if (btnSettings.contains(p)) {
                    frame.showSettings();
                } else if (btnExit.contains(p)) {
                    Logger.log("Выход из игры.");
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("mainRaft 2D", getWidth() / 2 - 100, 75);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Игрок: " + frame.getPlayerName(), getWidth() / 2 - 60, 105);

        drawButton(g, btnPlay, "Играть / Выбор мира");
        drawButton(g, btnNick, "Сменить ник");
        drawButton(g, btnRandomNick, "Случайный ник");
        drawButton(g, btnSettings, "Настройки");
        drawButton(g, btnExit, "Выход");
    }

    private void drawButton(Graphics g, Rectangle r, String text) {
        g.setColor(Color.GRAY);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, r.y + (r.height + fm.getAscent()) / 2 - 4);
    }
}

class WorldSelectPanel extends JPanel {
    private final MainRaft frame;
    private final List<String> worldFiles = new ArrayList<>();
    private Rectangle btnNewWorld, btnBack;
    private final List<Rectangle> worldButtons = new ArrayList<>();

    public WorldSelectPanel(MainRaft frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(800, 480));
        setBackground(new Color(45, 45, 45));

        btnNewWorld = new Rectangle(100, 390, 280, 40);
        btnBack     = new Rectangle(420, 390, 280, 40);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                if (btnNewWorld.contains(p)) {
                    String name = JOptionPane.showInputDialog(frame, "Введите название нового мира:", "world_" + (worldFiles.size() + 1));
                    if (name != null && !name.trim().isEmpty()) {
                        frame.getGamePanel().createNewWorld(name.trim() + ".dat");
                        frame.setCurrentState(MainRaft.GameState.GAME);
                        frame.startGame();
                    }
                } else if (btnBack.contains(p)) {
                    frame.showMenu();
                } else {
                    for (int i = 0; i < worldButtons.size(); i++) {
                        if (worldButtons.get(i).contains(p)) {
                            String selectedWorld = worldFiles.get(i);
                            frame.getGamePanel().loadWorld(selectedWorld);
                            break;
                        }
                    }
                }
            }
        });
    }

    public void refreshWorldList() {
        worldFiles.clear();
        worldButtons.clear();

        File folder = MainRaft.getSavesFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".dat"));

        if (files != null) {
            int y = 120;
            for (File f : files) {
                worldFiles.add(f.getName());
                worldButtons.add(new Rectangle(200, y, 400, 35));
                y += 45;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Выбор мира", getWidth() / 2 - 80, 60);

        if (worldFiles.isEmpty()) {
            g.setFont(new Font("Arial", Font.ITALIC, 18));
            g.drawString("Сохраненных миров не найдено. Создайте новый!", 190, 200);
        } else {
            for (int i = 0; i < worldButtons.size(); i++) {
                Rectangle r = worldButtons.get(i);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(r.x, r.y, r.width, r.height);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(r.x, r.y, r.width, r.height);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("Мир: " + worldFiles.get(i), r.x + 15, r.y + 22);
            }
        }

        drawButton(g, btnNewWorld, "Создать новый мир");
        drawButton(g, btnBack, "Назад в меню");
    }

    private void drawButton(Graphics g, Rectangle r, String text) {
        g.setColor(Color.GRAY);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, r.y + (r.height + fm.getAscent()) / 2 - 4);
    }
}

class SettingsPanel extends JPanel {
    private final MainRaft frame;
    private Rectangle btnZoomIn, btnZoomOut, btnBack;

    public SettingsPanel(MainRaft frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(800, 480));
        setBackground(new Color(50, 50, 50));

        int btnW = 200, btnH = 40;
        int centerX = (800 - btnW) / 2;

        btnZoomIn  = new Rectangle(centerX - 110, 200, btnW, btnH);
        btnZoomOut = new Rectangle(centerX + 110, 200, btnW, btnH);
        btnBack    = new Rectangle(centerX, 320, btnW, btnH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (btnZoomIn.contains(p)) {
                    frame.setZoomLevel(frame.getZoomLevel() + 0.1f);
                    repaint();
                } else if (btnZoomOut.contains(p)) {
                    frame.setZoomLevel(frame.getZoomLevel() - 0.1f);
                    repaint();
                } else if (btnBack.contains(p)) {
                    frame.showMenu();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Настройки", getWidth() / 2 - 70, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Масштаб камеры: " + String.format("%.1f", frame.getZoomLevel()) + "x", getWidth() / 2 - 110, 160);

        drawButton(g, btnZoomIn, "Приблизить (+)");
        drawButton(g, btnZoomOut, "Отдалить (-)");
        drawButton(g, btnBack, "Назад");
    }

    private void drawButton(Graphics g, Rectangle r, String text) {
        g.setColor(Color.GRAY);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, r.y + (r.height + fm.getAscent()) / 2 - 4);
    }
}

class Game2DPanel extends JPanel implements Runnable {

    private final MainRaft frame;
    private final int TILE_SIZE = 32;

    private final int UP_LIMIT = 824;
    private final int DOWN_LIMIT = 812;
    private final int SIDE_LIMIT = 891;

    private final int ROWS = UP_LIMIT + DOWN_LIMIT;
    private final int COLS = SIDE_LIMIT * 2;
    private int[][] map = new int[ROWS][COLS];

    private float playerX = 0;
    private float playerY = 0;
    private float velocityY = 0;
    private boolean isJumping = false;
    private boolean leftPressed = false, rightPressed = false;

    private float cameraX = 0;
    private float cameraY = 0;

    private String currentWorldName = "world.dat";

    private Image grassTexture, dirtTexture, woodTexture, oreTexture, playerSkin;
    private NPC testNPC;
    private final List<Animal> animals = new ArrayList<>();

    private int selectedSlot = 0;
    private final int[] hotbarBlocks = {1, 2, 3, 4};

    private Rectangle btnResume, btnSave, btnExitToMenu;

    public Game2DPanel(MainRaft frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(800, 480));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);

        int btnW = 220, btnH = 40;
        int centerX = (800 - btnW) / 2;
        btnResume     = new Rectangle(centerX, 160, btnW, btnH);
        btnSave       = new Rectangle(centerX, 230, btnW, btnH);
        btnExitToMenu = new Rectangle(centerX, 300, btnW, btnH);

        loadTextures();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (frame.getCurrentState() == MainRaft.GameState.GAME) {
                        frame.setCurrentState(MainRaft.GameState.PAUSE);
                    } else if (frame.getCurrentState() == MainRaft.GameState.PAUSE) {
                        frame.setCurrentState(MainRaft.GameState.GAME);
                    }
                }

                if (frame.getCurrentState() == MainRaft.GameState.GAME) {
                    if (e.getKeyCode() == KeyEvent.VK_A) leftPressed = true;
                    if (e.getKeyCode() == KeyEvent.VK_D) rightPressed = true;
                    if (e.getKeyCode() == KeyEvent.VK_SPACE && !isJumping) {
                        velocityY = -10f;
                        isJumping = true;
                    }

                    if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
                        int slot = e.getKeyCode() - KeyEvent.VK_1;
                        if (slot < hotbarBlocks.length) {
                            selectedSlot = slot;
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_D) rightPressed = false;
            }
        });

        addMouseWheelListener(e -> {
            if (frame.getCurrentState() == MainRaft.GameState.GAME) {
                float zoom = frame.getZoomLevel() - e.getWheelRotation() * 0.05f;
                frame.setZoomLevel(zoom);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (frame.getCurrentState() == MainRaft.GameState.PAUSE) {
                    Point p = e.getPoint();
                    if (btnResume.contains(p)) {
                        frame.setCurrentState(MainRaft.GameState.GAME);
                    } else if (btnSave.contains(p)) {
                        saveWorld(currentWorldName);
                    } else if (btnExitToMenu.contains(p)) {
                        frame.setCurrentState(MainRaft.GameState.MENU);
                        frame.showMenu();
                    }
                    return;
                }

                if (frame.getCurrentState() == MainRaft.GameState.GAME) {
                    float zoom = frame.getZoomLevel();
                    int worldMouseX = (int) ((e.getX() / zoom) + cameraX);
                    int worldMouseY = (int) ((e.getY() / zoom) + cameraY);

                    int col = worldMouseX / TILE_SIZE;
                    int row = worldMouseY / TILE_SIZE;

                    if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            map[row][col] = 0;
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            if (map[row][col] == 0) {
                                map[row][col] = hotbarBlocks[selectedSlot];
                            }
                        }
                    }
                }
            }
        });

        Thread t = new Thread(this);
        t.start();
    }

    public void createNewWorld(String fileName) {
        this.currentWorldName = fileName;
        this.playerX = 0;
        this.playerY = 0;
        generateRandomWorld();
        spawnEntitiesSafelyRandomly();
        saveWorld(currentWorldName);
    }

    private void generateRandomWorld() {
        Random rand = new Random();
        double seed = rand.nextDouble() * 10000;

        for (int c = 0; c < COLS; c++) {
            int heightOffset = (int) (Math.sin((c + seed) * 0.05) * 6 + Math.cos((c + seed) * 0.02) * 4);
            int surfaceRow = UP_LIMIT + heightOffset;

            for (int r = 0; r < ROWS; r++) {
                if (r < surfaceRow) {
                    map[r][c] = 0;
                } else if (r == surfaceRow) {
                    map[r][c] = 1;
                } else if (r < surfaceRow + 15) {
                    map[r][c] = 2;
                } else {
                    map[r][c] = (rand.nextDouble() < 0.12) ? 4 : 2;
                }
            }

            // Генерация деревьев (блок типа 3)
            if (c > 5 && c < COLS - 5 && rand.nextDouble() < 0.08) {
                map[surfaceRow - 1][c] = 3;
                map[surfaceRow - 2][c] = 3;
                map[surfaceRow - 3][c] = 3;
            }
        }
        Logger.log("Сгенерирован новый мир: " + currentWorldName);
    }

    private int findSurfaceRow(int col) {
        for (int r = 0; r < ROWS; r++) {
            if (map[r][col] != 0) {
                return r;
            }
        }
        return UP_LIMIT;
    }

    private void spawnEntitiesSafelyRandomly() {
        Random rand = new Random();

        if (playerX == 0 && playerY == 0) {
            int playerCol = rand.nextInt(COLS - 100) + 50;
            int playerSurfaceRow = findSurfaceRow(playerCol);
            playerX = playerCol * TILE_SIZE;
            playerY = (playerSurfaceRow - 1) * TILE_SIZE;
        }

        int playerCol = (int) (playerX / TILE_SIZE);

        int npcCol = Math.min(COLS - 5, Math.max(5, playerCol + rand.nextInt(10) - 5));
        int npcSurfaceRow = findSurfaceRow(npcCol);
        testNPC = new NPC(npcCol * TILE_SIZE, (npcSurfaceRow - 1) * TILE_SIZE);

        animals.clear();
        for (int i = 0; i < 5; i++) {
            int animCol = Math.min(COLS - 5, Math.max(5, playerCol + (i * 6) - 15));
            int animSurfaceRow = findSurfaceRow(animCol);
            animals.add(new Animal(animCol * TILE_SIZE, (animSurfaceRow - 1) * TILE_SIZE));
        }

        Logger.log("Сущности заспавнены.");
    }

    private void loadTextures() {
        int size = TILE_SIZE;
        grassTexture = TextureGenerator.generateGrassTexture(size, size);
        dirtTexture = TextureGenerator.generateDirtTexture(size, size);
        woodTexture = TextureGenerator.generateWoodTexture(size, size);
        oreTexture = TextureGenerator.generateOreTexture(size, size);
        playerSkin = TextureGenerator.generatePlayerSkin(size, size);
    }

    @Override
    public void run() {
        while (true) {
            if (frame.getCurrentState() == MainRaft.GameState.GAME) {
                updatePhysics();
                if (testNPC != null) testNPC.update(map, ROWS, COLS);
                for (Animal a : animals) a.update(map, ROWS, COLS);
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePhysics() {
        float speed = 4f;
        float newX = playerX;
        if (leftPressed) newX -= speed;
        if (rightPressed) newX += speed;

        if (!checkCollision(newX, playerY)) {
            playerX = newX;
        }

        velocityY += 0.5f;
        float newY = playerY + velocityY;

        if (!checkCollision(playerX, newY)) {
            playerY = newY;
        } else {
            if (velocityY > 0) {
                isJumping = false;
            }
            velocityY = 0;
        }

        float zoom = frame.getZoomLevel();
        float targetCamX = playerX - (getWidth() / (2f * zoom)) + TILE_SIZE / 2f;
        float targetCamY = playerY - (getHeight() / (2f * zoom)) + TILE_SIZE / 2f;

        cameraX += (targetCamX - cameraX) * 0.1f;
        cameraY += (targetCamY - cameraY) * 0.1f;
    }

    private boolean checkCollision(float px, float py) {
        int leftCol   = (int) (px / TILE_SIZE);
        int rightCol  = (int) ((px + TILE_SIZE - 1) / TILE_SIZE);
        int topRow    = (int) (py / TILE_SIZE);
        int bottomRow = (int) ((py + TILE_SIZE - 1) / TILE_SIZE);

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    if (map[r][c] != 0 && map[r][c] != 3) return true;
                }
            }
        }
        return false;
    }

    public void saveWorld(String fileName) {
        File saveFile = new File(MainRaft.getSavesFolder(), fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(map);
            oos.writeFloat(playerX);
            oos.writeFloat(playerY);
            Logger.log("Сохранен мир: " + saveFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Мир сохранен: " + saveFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWorld(String fileName) {
        this.currentWorldName = fileName;
        File saveFile = new File(MainRaft.getSavesFolder(), fileName);
        if (!saveFile.exists()) {
            JOptionPane.showMessageDialog(this, "Сохранение не найдено!");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            map = (int[][]) ois.readObject();
            playerX = ois.readFloat();
            playerY = ois.readFloat();

            spawnEntitiesSafelyRandomly();

            Logger.log("Загружен мир из: " + saveFile.getAbsolutePath());
            frame.setCurrentState(MainRaft.GameState.GAME);
            frame.startGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        float zoom = frame.getZoomLevel();
        g2d.scale(zoom, zoom);

        int camX = (int) cameraX;
        int camY = (int) cameraY;

        int startCol = Math.max(0, camX / TILE_SIZE);
        int endCol = Math.min(COLS, (camX + (int)(getWidth() / zoom)) / TILE_SIZE + 2);
        int startRow = Math.max(0, camY / TILE_SIZE);
        int endRow = Math.min(ROWS, (camY + (int)(getHeight() / zoom)) / TILE_SIZE + 2);

        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int blockType = map[r][c];
                int x = c * TILE_SIZE - camX;
                int y = r * TILE_SIZE - camY;

                if (blockType == 1) g2d.drawImage(grassTexture, x, y, TILE_SIZE, TILE_SIZE, null);
                else if (blockType == 2) g2d.drawImage(dirtTexture, x, y, TILE_SIZE, TILE_SIZE, null);
                else if (blockType == 3) g2d.drawImage(woodTexture, x, y, TILE_SIZE, TILE_SIZE, null);
                else if (blockType == 4) g2d.drawImage(oreTexture, x, y, TILE_SIZE, TILE_SIZE, null);
            }
        }

        for (Animal a : animals) {
            a.draw(g2d, camX, camY);
        }

        if (testNPC != null) {
            testNPC.draw(g2d, camX, camY);
        }

        int pDrawX = (int) playerX - camX;
        int pDrawY = (int) playerY - camY;
        if (playerSkin != null) {
            g2d.drawImage(playerSkin, pDrawX, pDrawY, TILE_SIZE, TILE_SIZE, null);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int nickW = fm.stringWidth(frame.getPlayerName());
        g2d.drawString(frame.getPlayerName(), pDrawX + (TILE_SIZE - nickW) / 2, pDrawY - 6);

        g2d.scale(1.0 / zoom, 1.0 / zoom);

        drawHotbar(g2d);

        if (frame.getCurrentState() == MainRaft.GameState.PAUSE) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.drawString("Пауза", getWidth() / 2 - 40, 100);

            drawButton(g2d, btnResume, "Продолжить");
            drawButton(g2d, btnSave, "Сохранить мир");
            drawButton(g2d, btnExitToMenu, "Выйти в меню");
        }
    }

    private void drawHotbar(Graphics2D g) {
        int slots = hotbarBlocks.length;
        int slotSize = 40;
        int startX = (getWidth() - (slots * slotSize)) / 2;
        int y = getHeight() - 60;

        for (int i = 0; i < slots; i++) {
            int x = startX + i * slotSize;

            g.setColor(new Color(50, 50, 50, 200));
            g.fillRect(x, y, slotSize, slotSize);

            if (i == selectedSlot) {
                g.setColor(Color.YELLOW);
                g.setStroke(new BasicStroke(3));
            } else {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1));
            }
            g.drawRect(x, y, slotSize, slotSize);

            Image img = null;
            if (hotbarBlocks[i] == 1) img = grassTexture;
            else if (hotbarBlocks[i] == 2) img = dirtTexture;
            else if (hotbarBlocks[i] == 3) img = woodTexture;
            else if (hotbarBlocks[i] == 4) img = oreTexture;

            if (img != null) {
                g.drawImage(img, x + 4, y + 4, 32, 32, null);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(String.valueOf(i + 1), x + 4, y + 14);
        }
    }

    private void drawButton(Graphics2D g, Rectangle r, String text) {
        g.setColor(Color.GRAY);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, r.y + (r.height + fm.getAscent()) / 2 - 4);
    }
}