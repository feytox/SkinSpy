package ru.feytox.skinspy.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ConnectScreen.class)
public interface ConnectScreenAccessor {
    @Invoker
    void callSetStatus(Text status);
}
