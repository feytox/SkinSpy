package ru.feytox.skinspy;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
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
