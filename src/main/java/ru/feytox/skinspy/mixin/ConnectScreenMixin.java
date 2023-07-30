package ru.feytox.skinspy.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.feytox.skinspy.SkinSpy;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {

    @Shadow
    protected void connect(MinecraftClient client, ServerAddress address, @Nullable ServerInfo info) {
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectConnector(CallbackInfo ci) {
        if (SkinSpy.connectInfo != null && SkinSpy.readyToConnect) {
            SkinSpy.readyToConnect = false;
            connect(SkinSpy.connectInfo.client(), SkinSpy.connectInfo.address(), SkinSpy.connectInfo.info());
            SkinSpy.connectInfo = null;
        }
    }

    @WrapWithCondition(
            method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V")
    )
    private static boolean injectSkinSpy(ConnectScreen instance, MinecraftClient client, ServerAddress address, ServerInfo info) {
        return SkinSpy.preConnectServer(client, instance, address, info);
    }
}
