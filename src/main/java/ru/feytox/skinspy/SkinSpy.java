package ru.feytox.skinspy;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonArray;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import ru.feytox.skinspy.config.ModConfig;
import ru.feytox.skinspy.mixin.ConnectScreenAccessor;

import java.util.Map;
import java.util.function.Consumer;

public class SkinSpy {

    private static final Jankson JSON = Jankson.builder().build();
    @Nullable
    public static Map<String, String> cachedCapes = null;

    private static void setConnectText(ConnectScreen connectScreen, Text text) {
        ((ConnectScreenAccessor) connectScreen).callSetStatus(text);
    }

    public static boolean preConnectServer(MinecraftClient client, ConnectScreen connectScreen, ServerInfo serverInfo) throws InterruptedException {
        ModConfig config = ModConfig.get();
        if (!config.enableMod) return true;

        Consumer<Text> infoConsumer = text -> setConnectText(connectScreen, text);
        boolean blocklist = config.isBlocklistMode;
        boolean tag = serverInfo.name.contains(config.serverTag);

        if (cachedCapes == null) {
            if (!cacheCapes(client, infoConsumer)) return false;
        }

        if (blocklist == tag) {
            String skinVariant = config.isCustomSlim ? "slim" : "classic";
            return setSkinAndCape(client, infoConsumer, config.customSkinName, skinVariant, config.customCapeName);
        }

        String skinVariant = config.isDefaultSlim ? "slim" : "classic";
        return setSkinAndCape(client, infoConsumer, config.defaultSkinName, skinVariant, config.defaultCapeName);
    }

    private static boolean setSkinAndCape(MinecraftClient client, Consumer<Text> infoConsumer, String skinName, String skinVariant, String capeName) {
        boolean skinResult = skinName.isEmpty() || HttpUtil.uploadSkinRequest(client, infoConsumer, skinVariant, skinName);
        boolean capeResult = capeName.isEmpty() || capeName.equals("HIDE") ? HttpUtil.hideCapeRequest(client, infoConsumer) : HttpUtil.showCapeRequest(client, infoConsumer, getCapeId(client, infoConsumer, capeName));
        return skinResult && capeResult;
    }

    @Nullable
    public static String getCapeId(MinecraftClient client, Consumer<Text> infoConsumer, String capeAlias) {
        String capeId = cachedCapes == null ? null : cachedCapes.get(capeAlias.toLowerCase());
        if (capeId != null) return capeId;
        boolean result = cacheCapes(client, infoConsumer);
        return result ? cachedCapes.get(capeAlias.toLowerCase()) : null;
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
}
