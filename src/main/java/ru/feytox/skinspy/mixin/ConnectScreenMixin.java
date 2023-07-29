package ru.feytox.skinspy.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.feytox.skinspy.SkinSpy;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @WrapWithCondition(
            method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V")
    )
    private static boolean injectSkinSpy(ConnectScreen instance, MinecraftClient client, ServerAddress address, ServerInfo info) {
        try {
            return SkinSpy.preConnectServer(client, instance, info);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
