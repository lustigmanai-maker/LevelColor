package net.kapi.inventorytextureeditor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public final class InventoryTextureEditorScreen extends Screen {
    private final Screen parent;
    private String status = "Import a PNG or JPG image for your inventory.";
    private boolean statusError;

    public InventoryTextureEditorScreen(Screen parent) {
        super(Component.literal("Inventory Texture Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = Math.max(8, (this.height - 260) / 2);

        this.addRenderableWidget(Button.builder(Component.literal("Import PNG / JPG"), button -> chooseImage())
                .bounds(centerX - 105, top + 174, 210, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Remove custom background"), button -> {
            try {
                InventoryTextureManager.clearBackground();
                setStatus("Custom background removed.", false);
            } catch (Exception exception) {
                setStatus("Could not remove the saved image.", true);
            }
        }).bounds(centerX - 105, top + 200, 210, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(centerX - 45, top + 230, 90, 20).build());
    }

    private void chooseImage() {
        this.minecraft.gui.setScreen(new InventoryFilePickerScreen(this, path -> {
            try {
                InventoryTextureManager.importImage(path);
                setStatus("Imported: " + path.getFileName(), false);
            } catch (Exception exception) {
                String message = exception.getMessage();
                setStatus(message == null ? "Could not import that image." : message, true);
            }
        }));
    }

    private void setStatus(String message, boolean error) {
        this.status = message;
        this.statusError = error;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int centerX = this.width / 2;
        int top = Math.max(8, (this.height - 260) / 2);
        drawCentered(graphics, Component.literal("Inventory Texture Editor"), centerX, top, 0xFFFFFFFF);
        drawCentered(graphics, Component.literal("Preview - recommended image: 176 x 166 px"),
                centerX, top + 19, 0xFFB8B8B8);

        int previewX = centerX - 66;
        int previewY = top + 39;
        graphics.fill(previewX - 2, previewY - 2, previewX + 134, previewY + 127, 0xFF292929);
        if (InventoryTextureManager.hasBackground()) {
            InventoryTextureManager.drawBackground(graphics, previewX, previewY, 132, 123);
        } else {
            graphics.fill(previewX, previewY, previewX + 132, previewY + 123, 0xFF171717);
            drawCentered(graphics, Component.literal("No image imported"), centerX, previewY + 56, 0xFFAAAAAA);
        }
        drawCentered(graphics, Component.literal("Press I in-game to open this editor"),
                centerX, top + 158, 0xFFAAAAAA);
        if (!status.isEmpty()) {
            drawCentered(graphics, Component.literal(status), centerX, top + 255,
                    statusError ? 0xFFFF5555 : 0xFF55FF55);
        }
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int centerX, int y, int color) {
        graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
