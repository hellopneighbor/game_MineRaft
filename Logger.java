import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static File logFile;

    public static void init() {
        File gameDir = new File(System.getProperty("user.home"), ".mainraft");
        File logsDir = new File(gameDir, "logs");
        logsDir.mkdirs();
        logFile = new File(logsDir, "log.txt");
        log("=== Запуск mainRaft 2D ===");
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = "[" + timestamp + "] " + message;
        System.out.println(entry);

        if (logFile != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(entry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}