package net.kapi.inventorytextureeditor.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int inventorytextureeditor$getLeftPos();

    @Accessor("topPos")
    int inventorytextureeditor$getTopPos();

    @Accessor("imageWidth")
    int inventorytextureeditor$getImageWidth();

    @Accessor("imageHeight")
    int inventorytextureeditor$getImageHeight();
}
