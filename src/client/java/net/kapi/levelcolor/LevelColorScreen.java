package net.kapi.levelcolor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LevelColorScreen extends Screen {
    private EditBox hexInput;
    private EditBox redInput;
    private EditBox greenInput;
    private EditBox blueInput;
    private String status = "";
    private boolean statusError;

    public LevelColorScreen() {
        super(Component.literal("LevelColor"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = Math.max(10, (this.height - 220) / 2);

        hexInput = new EditBox(this.font, centerX - 90, top + 55, 180, 20,
                Component.literal("HEX color"));
        hexInput.setMaxLength(7);
        hexInput.setValue(LevelColorConfig.getHex());
        this.addRenderableWidget(hexInput);

        int rgbStart = centerX - 92;
        redInput = new EditBox(this.font, rgbStart, top + 98, 55, 20, Component.literal("Red"));
        greenInput = new EditBox(this.font, rgbStart + 65, top + 98, 55, 20, Component.literal("Green"));
        blueInput = new EditBox(this.font, rgbStart + 130, top + 98, 55, 20, Component.literal("Blue"));
        for (EditBox field : new EditBox[]{redInput, greenInput, blueInput}) {
            field.setMaxLength(3);
            this.addRenderableWidget(field);
        }
        updateRgbFields();

        this.addRenderableWidget(Button.builder(Component.literal("Apply HEX"), button -> {
            if (LevelColorConfig.setHex(hexInput.getValue())) {
                updateRgbFields();
                setStatus("Color saved!", false);
            } else {
                setStatus("Enter HEX like #B10100.", true);
            }
        }).bounds(centerX - 95, top + 128, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Apply RGB"), button -> applyRgb())
                .bounds(centerX + 5, top + 128, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(centerX - 40, top + 201, 80, 20).build());
    }

    private void applyRgb() {
        try {
            int red = Integer.parseInt(redInput.getValue().trim());
            int green = Integer.parseInt(greenInput.getValue().trim());
            int blue = Integer.parseInt(blueInput.getValue().trim());
            if (LevelColorConfig.setRgb(red, green, blue)) {
                hexInput.setValue(LevelColorConfig.getHex());
                setStatus("RGB color saved!", false);
            } else {
                setStatus("RGB values must be 0 to 255.", true);
            }
        } catch (NumberFormatException exception) {
            setStatus("Enter three RGB numbers.", true);
        }
    }

    private void updateRgbFields() {
        int rgb = LevelColorConfig.getArgb() & 0xFFFFFF;
        redInput.setValue(Integer.toString((rgb >> 16) & 255));
        greenInput.setValue(Integer.toString((rgb >> 8) & 255));
        blueInput.setValue(Integer.toString(rgb & 255));
    }

    private void setStatus(String message, boolean error) {
        this.status = message;
        this.statusError = error;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int centerX = this.width / 2;
        int top = Math.max(10, (this.height - 220) / 2);

        drawCentered(graphics, Component.literal("LevelColor"), centerX, top, 0xFFFFFFFF);
        drawCentered(graphics, Component.literal("Change only the XP level number"), centerX, top + 20, 0xFFB8B8B8);
        graphics.text(this.font, Component.literal("HEX color"), centerX - 90, top + 43, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.literal("RGB (0-255)"), centerX - 92, top + 86, 0xFFFFFFFF, false);
        drawCentered(graphics, Component.literal("Preview: 147"), centerX, top + 157, LevelColorConfig.getArgb());
        if (!status.isEmpty()) {
            drawCentered(graphics, Component.literal(status), centerX, top + 178,
                    statusError ? 0xFFFF5555 : 0xFF55FF55);
        }
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int centerX, int y, int color) {
        graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }
}
