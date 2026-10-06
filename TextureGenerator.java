import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TextureGenerator {

    private static final Random random = new Random();

    public static BufferedImage generateGrassTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int g = 120 + random.nextInt(50);
                img.setRGB(x, y, new Color(30, g, 30).getRGB());
            }
        }
        return img;
    }

    public static BufferedImage generateDirtTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int r = 100 + random.nextInt(30);
                img.setRGB(x, y, new Color(r, 50, 20).getRGB());
            }
        }
        return img;
    }

    public static BufferedImage generateWoodTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int br = 90 + random.nextInt(20);
                img.setRGB(x, y, new Color(br, 50, 10).getRGB());
            }
        }
        return img;
    }

    public static BufferedImage generateOreTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (random.nextInt(5) == 0) {
                    img.setRGB(x, y, Color.GRAY.getRGB()); // Руда
                } else {
                    img.setRGB(x, y, new Color(80, 80, 80).getRGB()); // Камень
                }
            }
        }
        return img;
    }

    public static BufferedImage generatePlayerSkin(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(8, 0, 16, 16);
        g.setColor(Color.RED);
        g.fillRect(4, 16, 24, 16);
        g.dispose();
        return img;
    }
}