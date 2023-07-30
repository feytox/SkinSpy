package ru.feytox.skinspy;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.feytox.skinspy.config.ModConfig;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SkinSpyInitializer implements ModInitializer {

    public static final String MOD_NAME = "SkinSpy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String MOD_ID = "skinspy";

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        startAsyncCapesCaching();

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (SkinSpy.switcherTicks > 0) {
                SkinSpy.switcherTicks--;
            } else if (SkinSpy.switcherTicks == 0) {
                SkinSpy.switcherTicks = -1;
                ModConfig config = ModConfig.get();
                MinecraftClient client = MinecraftClient.getInstance();
                Consumer<Text> infoConsumer = config.showInfoInChat ? text -> { if (client.player != null) client.player.sendMessage(text); } : text -> {};
                SkinSpy.setSkinAndCape(client, !SkinSpy.wasCustomKit, config, infoConsumer);
            }
        });

        LOGGER.info("SkinSpyInitializer has been initialized!");
    }

    public static void startAsyncCapesCaching() {
        MinecraftClient client = MinecraftClient.getInstance();
        CompletableFuture.runAsync(() -> {
            Consumer<Text> infoConsumer = text -> {};
            SkinSpy.cacheCapes(client, infoConsumer);
        });
    }
}
