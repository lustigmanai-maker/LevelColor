package net.kapi.inventorytextureeditor;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.function.Consumer;

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
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            Path savedFile = findSavedFile();
            if (savedFile == null) {
                loaded = false;
                return;
            }

            try {
                NativeImage image = readImage(savedFile);
                registerImage(image);
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
        if (!loaded) {
            return;
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }

    public static void importImage(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("No image file was selected.");
        }

        String filename = source.getFileName().toString().toLowerCase(Locale.ROOT);
        String extension;
        if (filename.endsWith(".png")) {
            extension = ".png";
        } else if (filename.endsWith(".jpg")) {
            extension = ".jpg";
        } else if (filename.endsWith(".jpeg")) {
            extension = ".jpeg";
        } else {
            throw new IOException("Choose a PNG, JPG or JPEG file.");
        }

        long size = Files.size(source);
        if (size > 20L * 1024L * 1024L) {
            throw new IOException("The image is too large (maximum 20 MB).");
        }

        NativeImage image = readImage(source);
        if (image.getWidth() > 4096 || image.getHeight() > 4096) {
            image.close();
            throw new IOException("Image dimensions must be 4096 x 4096 pixels or smaller.");
        }

        Files.createDirectories(CONFIG_DIRECTORY);
        Path destination = CONFIG_DIRECTORY.resolve("inventory_background" + extension);
        Path normalizedSource = source.toAbsolutePath().normalize();
        Path normalizedDestination = destination.toAbsolutePath().normalize();

        try {
            if (!normalizedSource.equals(normalizedDestination)) {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            for (String otherExtension : new String[]{".png", ".jpg", ".jpeg"}) {
                Path oldFile = CONFIG_DIRECTORY.resolve("inventory_background" + otherExtension);
                if (!oldFile.equals(destination)) {
                    Files.deleteIfExists(oldFile);
                }
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

    public static void openFilePicker(Consumer<Path> callback) {
        EventQueue.invokeLater(() -> {
            Path selected = null;
            try {
                FileDialog dialog = new FileDialog((Frame) null, "Select inventory background", FileDialog.LOAD);
                dialog.setFilenameFilter((directory, name) -> {
                    String lower = name.toLowerCase(Locale.ROOT);
                    return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
                });
                dialog.setVisible(true);

                String file = dialog.getFile();
                String directory = dialog.getDirectory();
                if (file != null && directory != null) {
                    selected = Path.of(directory, file);
                }
                dialog.dispose();
            } catch (RuntimeException exception) {
                System.err.println("[Inventory Texture Editor] File picker error: " + exception.getMessage());
            }

            Path result = selected;
            Minecraft.getInstance().execute(() -> callback.accept(result));
        });
    }

    private static Path findSavedFile() {
        for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
            Path file = CONFIG_DIRECTORY.resolve("inventory_background" + extension);
            if (Files.isRegularFile(file)) {
                return file;
            }
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
