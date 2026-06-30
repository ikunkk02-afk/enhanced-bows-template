package io.github.ikunkk02.enhancedbows.client.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Allows the Cloth Config after-init hook to attach the visual HUD editor button. */
@Mixin(Screen.class)
public interface ScreenAccessor {
	@Invoker("addDrawableChild")
	<T extends Element & Drawable & Selectable> T enhancedBows$addDrawableChild(T element);
}
