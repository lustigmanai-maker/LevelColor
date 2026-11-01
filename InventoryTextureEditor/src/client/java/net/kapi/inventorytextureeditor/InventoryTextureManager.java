package net.kapi.inventorytextureeditor;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class InventoryTextureManager {
    private static final Path CONFIG_DIRECTORY =
            FabricLoader.getInstance().getConfigDir().resolve("inventorytextureeditor");
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath("inventorytextureeditor", "dynamic/inventory_background");

    private static boolean loaded;
    private static int textureWidth = 176;
    private static int textureHeight = 166;

    private InventoryTextureManager() {
    }

    public static void load() {
        Minecraft.getInstance().execute(() -> {
            Path savedFile = findSavedFile();
            if (savedFile == null) {
                loaded = false;
                return;
            }
            try {
                registerImage(readImage(savedFile));
            } catch (Exception exception) {
                loaded = false;
                System.err.println("[Inventory Texture Editor] Could not load saved inventory texture: "
                        + exception.getMessage());
            }
        });
    }

    public static boolean hasBackground() {
        return loaded;
    }

    public static void drawBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        if (!loaded) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y,
                0.0F, 0.0F, width, height, textureWidth, textureHeight);
    }

    public static void importImage(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("No image file was selected.");
        }
        String filename = source.getFileName().toString().toLowerCase(Locale.ROOT);
        String extension;
        if (filename.endsWith(".png")) extension = ".png";
        else if (filename.endsWith(".jpg")) extension = ".jpg";
        else if (filename.endsWith(".jpeg")) extension = ".jpeg";
        else throw new IOException("Choose a PNG, JPG or JPEG file.");

        if (Files.size(source) > 20L * 1024L * 1024L) {
            throw new IOException("The image is too large (maximum 20 MB).");
        }
        NativeImage image = readImage(source);
        if (image.getWidth() > 4096 || image.getHeight() > 4096) {
            image.close();
            throw new IOException("Image dimensions must be 4096 x 4096 pixels or smaller.");
        }

        Files.createDirectories(CONFIG_DIRECTORY);
        Path destination = CONFIG_DIRECTORY.resolve("inventory_background" + extension);
        try {
            if (!source.toAbsolutePath().normalize().equals(destination.toAbsolutePath().normalize())) {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            for (String other : new String[]{".png", ".jpg", ".jpeg"}) {
                Path old = CONFIG_DIRECTORY.resolve("inventory_background" + other);
                if (!old.equals(destination)) Files.deleteIfExists(old);
            }
            registerImage(image);
        } catch (IOException | RuntimeException exception) {
            image.close();
            throw exception;
        }
    }

    public static void clearBackground() throws IOException {
        Files.createDirectories(CONFIG_DIRECTORY);
        for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
            Files.deleteIfExists(CONFIG_DIRECTORY.resolve("inventory_background" + extension));
        }
        loaded = false;
    }

    private static Path findSavedFile() {
        for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
            Path file = CONFIG_DIRECTORY.resolve("inventory_background" + extension);
            if (Files.isRegularFile(file)) return file;
        }
        return null;
    }

    private static NativeImage readImage(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return NativeImage.read(input);
        }
    }

    private static void registerImage(NativeImage image) {
        textureWidth = image.getWidth();
        textureHeight = image.getHeight();
        Minecraft.getInstance().getTextureManager().register(
                BACKGROUND_TEXTURE,
                new DynamicTexture(() -> "Inventory Texture Editor background", image)
        );
        loaded = true;
    }
}
