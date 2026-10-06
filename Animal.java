import java.awt.*;
import java.util.Random;

public class Animal {
    private float x, y;
    private float velocityY = 0;
    private int moveDirection = 0;
    private int stateTimer = 0;
    private final Random random = new Random();
    private final int tileSize = 32;

    public Animal(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(int[][] map, int rows, int cols) {
        stateTimer++;
        if (stateTimer > 80 + random.nextInt(100)) {
            moveDirection = random.nextInt(3) - 1;
            stateTimer = 0;
        }

        float speed = 1.0f;
        float nextX = x + (moveDirection * speed);

        int frontCol = (int) ((nextX + (moveDirection > 0 ? tileSize : 0)) / tileSize);
        int feetRow = (int) ((y + tileSize - 1) / tileSize);

        if (frontCol >= 0 && frontCol < cols && feetRow >= 0 && feetRow < rows) {
            if (map[feetRow][frontCol] != 0 && velocityY == 0) {
                velocityY = -7f;
            }
        }

        if (!checkCollision(nextX, y, map, rows, cols)) {
            x = nextX;
        }

        velocityY += 0.5f;
        float nextY = y + velocityY;
        if (!checkCollision(x, nextY, map, rows, cols)) {
            y = nextY;
        } else {
            velocityY = 0;
        }
    }

    private boolean checkCollision(float px, float py, int[][] map, int rows, int cols) {
        int leftCol = (int) (px / tileSize);
        int rightCol = (int) ((px + tileSize - 1) / tileSize);
        int topRow = (int) (py / tileSize);
        int bottomRow = (int) ((py + tileSize - 1) / tileSize);

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    if (map[r][c] != 0) return true;
                }
            }
        }
        return false;
    }

    public void draw(Graphics g, int cameraX, int cameraY) {
        int drawX = (int) x - cameraX;
        int drawY = (int) y - cameraY;

        // Коричневая живность
        g.setColor(new Color(120, 60, 20));
        g.fillRect(drawX + 4, drawY + 8, 24, 24);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("Животное", drawX - 2, drawY - 2);
    }
}