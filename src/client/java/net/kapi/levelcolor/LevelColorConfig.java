package net.kapi.levelcolor;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LevelColorConfig {
    private static final Pattern COLOR_PATTERN =
            Pattern.compile("\"xpText\"\\s*:\\s*\"#?([0-9a-fA-F]{6})\"");
    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("levelcolor.json");

    private static String hex = "#B10100";
    private static int argb = 0xFFB10100;

    private LevelColorConfig() {}

    public static synchronized void load() {
        try {
            if (!Files.exists(CONFIG_FILE)) {
                save();
                return;
            }
            Matcher matcher = COLOR_PATTERN.matcher(Files.readString(CONFIG_FILE));
            if (matcher.find()) {
                parseHexWithoutSaving(matcher.group(1));
            }
        } catch (IOException | NumberFormatException exception) {
            System.err.println("[LevelColor] Could not load config: " + exception.getMessage());
        }
    }

    public static synchronized boolean setHex(String input) {
        if (input == null) return false;
        String cleaned = input.trim();
        if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
        if (!cleaned.matches("(?i)[0-9a-f]{6}")) return false;

        try {
            parseHexWithoutSaving(cleaned);
            save();
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static void parseHexWithoutSaving(String input) {
        int rgb = Integer.parseInt(input, 16);
        argb = 0xFF000000 | rgb;
        hex = "#" + input.toUpperCase(Locale.ROOT);
    }

    public static synchronized boolean setRgb(int red, int green, int blue) {
        if (!isByte(red) || !isByte(green) || !isByte(blue)) return false;
        return setHex(String.format(Locale.ROOT, "%02X%02X%02X", red, green, blue));
    }

    private static boolean isByte(int value) {
        return value >= 0 && value <= 255;
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, "{\n  \"xpText\": \"" + hex + "\"\n}\n");
        } catch (IOException exception) {
            System.err.println("[LevelColor] Could not save config: " + exception.getMessage());
        }
    }

    public static synchronized String getHex() {
        return hex;
    }

    public static synchronized int getArgb() {
        return argb;
    }
}
