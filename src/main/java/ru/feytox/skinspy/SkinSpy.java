package ru.feytox.skinspy;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonArray;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import ru.feytox.skinspy.config.ModConfig;
import ru.feytox.skinspy.config.SkinType;
import ru.feytox.skinspy.mixin.ConnectScreenAccessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SkinSpy {

    private static final Jankson JSON = Jankson.builder().build();
    @Nullable
    public static Map<String, String> cachedCapes = null;
    public static boolean readyToConnect = false;
    @Nullable
    public static ConnectInfo connectInfo = null;

    private static void setConnectText(ConnectScreen connectScreen, Text text) {
        ((ConnectScreenAccessor) connectScreen).callSetStatus(text);
    }

    public static boolean preConnectServer(MinecraftClient client, ConnectScreen connectScreen, ServerAddress address, ServerInfo serverInfo) {
        ModConfig config = ModConfig.get();
        if (!config.enableMod) return true;

        connectInfo = new ConnectInfo(client, address, serverInfo);
        Consumer<Text> infoConsumer = text -> setConnectText(connectScreen, text);
        boolean blocklist = config.isBlocklistMode;
        boolean tag = serverInfo.name.contains(config.serverTag);

        if (cachedCapes == null) {
            if (!cacheCapes(client, infoConsumer)) return false;
        }

        String skinName = blocklist == tag ? config.customSkinName : config.defaultSkinName;
        SkinType skinType = blocklist == tag ? config.customSkinType : config.defaultSkinType;
        String capeName = blocklist == tag ? config.customCapeName : config.defaultCapeName;
        return setSkinAndCape(client, infoConsumer, skinName, skinType, capeName);
    }

    private static boolean setSkinAndCape(MinecraftClient client, Consumer<Text> infoConsumer, String skinName, SkinType skinType, String capeName) {
        List<Supplier<Boolean>> requests = new ObjectArrayList<>();
        if (!skinName.isEmpty()) {
            requests.add(() -> HttpUtil.uploadSkinRequest(client, infoConsumer, skinType.name().toLowerCase(), skinName));
        }
        if (!capeName.isEmpty()) {
            Supplier<Boolean> capeRequest = capeName.equalsIgnoreCase("hide") ? () ->  HttpUtil.hideCapeRequest(client, infoConsumer) : () ->  HttpUtil.showCapeRequest(client, infoConsumer, getCapeId(client, infoConsumer, capeName));
            requests.add(capeRequest);
        }

        CompletableFuture.runAsync(() -> {
            boolean result = sendRequests(infoConsumer, requests);
            if (result) readyToConnect = true;
        });
        return requests.isEmpty();
    }

    @Nullable
    public static String getCapeId(MinecraftClient client, Consumer<Text> infoConsumer, String capeAlias) {
        String capeId = cachedCapes == null ? null : cachedCapes.get(capeAlias.toLowerCase());
        if (capeId != null) return capeId;
        boolean result = cacheCapes(client, infoConsumer);
        return result ? cachedCapes.get(capeAlias.toLowerCase()) : null;
    }

    public static boolean sendRequests(Consumer<Text> infoConsumer, List<Supplier<Boolean>> requests) {
        if (requests.isEmpty()) return true;
        boolean result = true;
        infoConsumer.accept(Text.translatable("skinspy.info.request"));

        for (Supplier<Boolean> request : requests) {
            result = result && request.get();
        }
        return result;
    }

    public static boolean cacheCapes(MinecraftClient client, Consumer<Text> infoConsumer) {
        String jsonString = HttpUtil.getProfileRequest(client, infoConsumer);
        if (jsonString == null) return false;

        try {
            JsonObject json = JSON.load(jsonString);
            if (!(json.get("capes") instanceof JsonArray jsonCapes)) throw new SyntaxError("The received response does not contain a list of capes");

            final Map<String, String> capes = new Object2ObjectOpenHashMap<>();
            for (JsonElement element : jsonCapes) {
                if (!(element instanceof JsonObject capeJson)) throw new SyntaxError("Wrong cape response format");
                String capeAlias = capeJson.get(String.class, "alias");
                capeAlias = capeAlias == null ? null : capeAlias.toLowerCase();
                String capeId = capeJson.get(String.class, "id");
                capes.put(capeAlias, capeId);
            }
            cachedCapes = capes;
            return true;

        } catch (SyntaxError e) {
            infoConsumer.accept(Text.of(e.toString()));
            SkinSpyInitializer.LOGGER.error(e.toString());
            SkinSpyInitializer.LOGGER.error(jsonString);
            return false;
        }
    }

    public record ConnectInfo(MinecraftClient client, ServerAddress address, ServerInfo info) {}
}
