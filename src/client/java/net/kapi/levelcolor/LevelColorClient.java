package net.kapi.levelcolor;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class LevelColorClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("levelcolor", "general")
    );
    private static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.levelcolor.open",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_K,
            CATEGORY
    );

    @Override
    public void onInitializeClient() {
        LevelColorConfig.load();
        KeyMappingHelper.registerKeyMapping(OPEN_MENU);

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("levelcolor", "stopwatch"), LevelColorClient::renderStopwatch);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("start").then(literal("timer").executes(command -> {
                    Stopwatch.start();
                    command.getSource().sendFeedback(Component.literal("Timer started."));
                    return 1;
                }))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("stop").then(literal("timer").executes(command -> {
                    Stopwatch.stop();
                    command.getSource().sendFeedback(Component.literal("Timer stopped at " + Stopwatch.formattedTime() + "."));
                    return 1;
                }))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("resume").then(literal("timer").executes(command -> {
                    Stopwatch.resume();
                    command.getSource().sendFeedback(Component.literal("Timer resumed."));
                    return 1;
                }))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("reset").then(literal("timer").executes(command -> {
                    Stopwatch.reset();
                    command.getSource().sendFeedback(Component.literal("Timer reset to 00:00:00.00."));
                    return 1;
                }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new LevelColorScreen());
                }
            }
        });
    }

    private static void renderStopwatch(net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                                        net.minecraft.client.DeltaTracker deltaTracker) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var text = Component.literal(Stopwatch.formattedTime());
        int x = 8;
        int y = 8;
        int color = LevelColorConfig.getTimerArgb();
        graphics.fill(x - 4, y - 3, x + minecraft.font.width(text) + 4, y + 12, 0x90000000);
        graphics.text(minecraft.font, text, x, y, color, true);
    }
}
