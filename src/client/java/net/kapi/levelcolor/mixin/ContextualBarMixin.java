package net.kapi.levelcolor.mixin;

import net.kapi.levelcolor.LevelColorConfig;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ContextualBar.class)
public interface ContextualBarMixin {
    @ModifyConstant(method = "extractExperienceLevel", constant = @Constant(intValue = -8323296))
    private static int levelcolor$changeForegroundColor(int originalColor) {
        return LevelColorConfig.getArgb();
    }
}
