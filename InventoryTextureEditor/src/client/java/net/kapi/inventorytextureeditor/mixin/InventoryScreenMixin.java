package net.kapi.inventorytextureeditor.mixin;

import net.kapi.inventorytextureeditor.InventoryTextureManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void inventorytextureeditor$drawCustomBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        if (InventoryTextureManager.hasBackground()) {
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) this;
            InventoryTextureManager.drawBackground(
                    graphics,
                    accessor.inventorytextureeditor$getLeftPos(),
                    accessor.inventorytextureeditor$getTopPos(),
                    accessor.inventorytextureeditor$getImageWidth(),
                    accessor.inventorytextureeditor$getImageHeight()
            );
        }
    }
}
