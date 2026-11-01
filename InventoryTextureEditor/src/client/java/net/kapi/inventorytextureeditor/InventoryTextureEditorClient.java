package net.kapi.inventorytextureeditor;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class InventoryTextureEditorClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("inventorytextureeditor", "general")
    );

    private static final KeyMapping OPEN_EDITOR = new KeyMapping(
            "key.inventorytextureeditor.open",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_I,
            CATEGORY
    );

    @Override
    public void onInitializeClient() {
        InventoryTextureManager.load();
        KeyMappingHelper.registerKeyMapping(OPEN_EDITOR);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_EDITOR.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new InventoryTextureEditorScreen(null));
                }
            }
        });
    }
}
