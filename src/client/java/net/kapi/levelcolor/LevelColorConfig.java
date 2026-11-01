package net.kapi.levelcolor;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LevelColorConfig {
    private static final Pattern XP_PATTERN = Pattern.compile("\"xpText\"\\s*:\\s*\"#?([0-9a-fA-F]{6})\"");
    private static final Pattern TIMER_PATTERN = Pattern.compile("\"timerText\"\\s*:\\s*\"#?([0-9a-fA-F]{6})\"");
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("levelcolor.json");
    private static String hex = "#B10100";
    private static int argb = 0xFFB10100;
    private static String timerHex = "#FFFFFF";
    private static int timerArgb = 0xFFFFFFFF;

    private LevelColorConfig() {}

    public static synchronized void load() {
        try {
            if (!Files.exists(CONFIG_FILE)) { save(); return; }
            String json = Files.readString(CONFIG_FILE);
            Matcher xp = XP_PATTERN.matcher(json);
            if (xp.find()) parseXp(xp.group(1));
            Matcher timer = TIMER_PATTERN.matcher(json);
            if (timer.find()) parseTimer(timer.group(1));
            else save();
        } catch (IOException | NumberFormatException exception) {
            System.err.println("[LevelColor] Could not load config: " + exception.getMessage());
        }
    }

    public static synchronized boolean setHex(String input) {
        if (!valid(input)) return false;
        String cleaned = clean(input);
        parseXp(cleaned);
        save();
        return true;
    }

    public static synchronized boolean setTimerHex(String input) {
        if (!valid(input)) return false;
        parseTimer(clean(input));
        save();
        return true;
    }

    private static boolean valid(String input) {
        if (input == null) return false;
        String cleaned = clean(input);
        return cleaned.matches("(?i)[0-9a-f]{6}");
    }

    private static String clean(String input) {
        String cleaned = input.trim();
        return cleaned.startsWith("#") ? cleaned.substring(1) : cleaned;
    }

    private static void parseXp(String input) {
        argb = 0xFF000000 | Integer.parseInt(input, 16);
        hex = "#" + input.toUpperCase(Locale.ROOT);
    }

    private static void parseTimer(String input) {
        timerArgb = 0xFF000000 | Integer.parseInt(input, 16);
        timerHex = "#" + input.toUpperCase(Locale.ROOT);
    }

    public static synchronized boolean setRgb(int red, int green, int blue) {
        if (!isByte(red) || !isByte(green) || !isByte(blue)) return false;
        return setHex(String.format(Locale.ROOT, "%02X%02X%02X", red, green, blue));
    }

    private static boolean isByte(int value) { return value >= 0 && value <= 255; }

    private static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, "{\n  \"xpText\": \"" + hex + "\",\n  \"timerText\": \"" + timerHex + "\"\n}\n");
        } catch (IOException exception) {
            System.err.println("[LevelColor] Could not save config: " + exception.getMessage());
        }
    }

    public static synchronized String getHex() { return hex; }
    public static synchronized int getArgb() { return argb; }
    public static synchronized String getTimerHex() { return timerHex; }
    public static synchronized int getTimerArgb() { return timerArgb; }
}
