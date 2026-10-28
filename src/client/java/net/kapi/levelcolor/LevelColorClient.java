package net.kapi.levelcolor;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new LevelColorScreen());
                }
            }
        });
    }
}
