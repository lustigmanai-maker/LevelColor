package net.kapi.inventorytextureeditor;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class InventoryFilePickerScreen extends Screen {
    private static final int PAGE_SIZE = 8;
    private final Screen parent;
    private final Consumer<Path> onSelect;
    private Path currentDirectory;
    private List<Path> entries = List.of();
    private int page;
    private String error = "";

    public InventoryFilePickerScreen(Screen parent, Consumer<Path> onSelect) {
        super(Component.literal("Choose inventory image"));
        this.parent = parent;
        this.onSelect = onSelect;
        this.currentDirectory = Path.of(System.getProperty("user.home", ".")).toAbsolutePath().normalize();
    }

    @Override
    protected void init() {
        refreshEntries();
        int center = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Up one folder"), b -> goUp())
                .bounds(center - 154, 38, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Home"), b -> {
            currentDirectory = Path.of(System.getProperty("user.home", ".")).toAbsolutePath().normalize();
            page = 0;
            rebuildWidgets();
        }).bounds(center - 49, 38, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Minecraft folder"), b -> {
            currentDirectory = FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize();
            page = 0;
            rebuildWidgets();
        }).bounds(center + 26, 38, 128, 20).build());

        int start = page * PAGE_SIZE;
        int end = Math.min(entries.size(), start + PAGE_SIZE);
        for (int i = start; i < end; i++) {
            Path entry = entries.get(i);
            boolean directory = Files.isDirectory(entry);
            String label = (directory ? "[Folder] " : "[Image] ") + shorten(entry.getFileName().toString(), 31);
            int row = i - start;
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                if (directory) {
                    currentDirectory = entry;
                    page = 0;
                    rebuildWidgets();
                } else {
                    onSelect.accept(entry);
                    this.minecraft.gui.setScreen(parent);
                }
            }).bounds(center - 154, 66 + row * 23, 308, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Previous"), b -> {
            if (page > 0) {
                page--;
                rebuildWidgets();
            }
        }).bounds(center - 154, 254, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Next"), b -> {
            if ((page + 1) * PAGE_SIZE < entries.size()) {
                page++;
                rebuildWidgets();
            }
        }).bounds(center + 64, 254, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.minecraft.gui.setScreen(parent))
                .bounds(center - 45, 280, 90, 20).build());
    }

    private void refreshEntries() {
        List<Path> found = new ArrayList<>();
        error = "";
        try (Stream<Path> stream = Files.list(currentDirectory)) {
            stream.filter(path -> Files.isDirectory(path) || isImage(path))
                    .forEach(found::add);
        } catch (IOException | SecurityException exception) {
            error = "Cannot read this folder.";
        }
        found.sort(Comparator
                .comparing((Path path) -> !Files.isDirectory(path))
                .thenComparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)));
        entries = List.copyOf(found);
        int maxPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);
        page = Math.min(page, maxPage);
    }

    private void goUp() {
        Path parentPath = currentDirectory.getParent();
        if (parentPath != null) {
            currentDirectory = parentPath;
            page = 0;
            rebuildWidgets();
        }
    }

    private void rebuildWidgets() {
        clearWidgets();
        init();
    }

    private static boolean isImage(Path path) {
        if (!Files.isRegularFile(path)) return false;
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private static String shorten(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(1, max - 3)) + "...";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        drawCentered(graphics, "Choose a PNG or JPG image", center, 12, 0xFFFFFFFF);
        drawCentered(graphics, shorten(currentDirectory.toString(), 52), center, 27, 0xFFB8B8B8);
        if (entries.isEmpty() && error.isEmpty()) {
            drawCentered(graphics, "No folders or supported images here.", center, 145, 0xFFAAAAAA);
        }
        if (!error.isEmpty()) {
            drawCentered(graphics, error, center, 145, 0xFFFF5555);
        }
        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        drawCentered(graphics, "Page " + (page + 1) + " / " + pages + "   (" + entries.size() + " items)",
                center, 235, 0xFFAAAAAA);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int center, int y, int color) {
        Component component = Component.literal(text);
        graphics.text(this.font, component, center - this.font.width(component) / 2, y, color, false);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
