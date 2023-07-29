package ru.feytox.skinspy;

import com.google.common.io.Files;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Consumer;

public class HttpUtil {
    private static final URI CAPE = URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active");
    private static final URI SKIN = URI.create("https://api.minecraftservices.com/minecraft/profile/skins");
    private static final URI PROFILE = URI.create("https://api.minecraftservices.com/minecraft/profile");

    private static String getAuthHeader(MinecraftClient client) {
        return "Bearer " + client.getSession().getAccessToken();
    }

    @Nullable
    public static String getProfileRequest(MinecraftClient client, Consumer<Text> infoConsumer) {
        HttpGet http = new HttpGet(PROFILE);
        try (var httpClient = HttpClientBuilder.create().build()) {
            http.addHeader("Authorization", getAuthHeader(client));
            var response = httpClient.execute(http);

            if (response.getStatusLine().getStatusCode() == 200) {
                infoConsumer.accept(translatable("getProfile.success"));
                return EntityUtils.toString(response.getEntity());
            }

            infoConsumer.accept(translatable("getProfile.fail"));
            SkinSpyInitializer.LOGGER.error("Code: {} Reason: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            return null;

        } catch (Exception e) {
            infoConsumer.accept(Text.of(e.toString()));
            SkinSpyInitializer.LOGGER.error(e.toString());
        }

        return null;
    }

    public static boolean uploadSkinRequest(MinecraftClient client, Consumer<Text> infoConsumer, String variant, String fileName) {
        HttpPost http = new HttpPost(SKIN);
        Path skinPath = FabricLoader.getInstance().getConfigDir().resolve(SkinSpyInitializer.MOD_ID).resolve(fileName);
        try {
            File skinFile = skinPath.toFile();
            if (!Files.isFile().apply(skinFile)) throw new IOException("Can't find skin file");

            var builder = MultipartEntityBuilder.create()
                    .addTextBody("variant", variant)
                    .addBinaryBody("file", new FileInputStream(skinFile), ContentType.IMAGE_PNG, "skin.png");
            http.setEntity(builder.build());
            return request(client, infoConsumer, http, "uploadSkin.success", "uploadSkin.fail");

        } catch (Exception e) {
            infoConsumer.accept(translatable("uploadSkin.fail"));
            SkinSpyInitializer.LOGGER.error(e.toString());
            return false;
        }
    }

    public static boolean hideCapeRequest(MinecraftClient client, Consumer<Text> infoConsumer) {
        HttpDelete http = new HttpDelete(CAPE);
        return request(client, infoConsumer, http, "hideCape.success", "hideCape.fail");
    }

    public static boolean showCapeRequest(MinecraftClient client, Consumer<Text> infoConsumer, String capeId) {
        if (capeId == null) {
            infoConsumer.accept(translatable("showCape.empty"));
            return false;
        }
        HttpPut http = new HttpPut(CAPE);
        var builder = EntityBuilder.create()
                .setText("{ \"capeId\": \"" + capeId + "\" }")
                .setContentType(ContentType.APPLICATION_JSON);
        http.setEntity(builder.build());
        return request(client, infoConsumer, http, "showCape.success", "showCape.fail");
    }

    private static boolean request(MinecraftClient client, Consumer<Text> infoConsumer, HttpRequestBase http, String successKey, String failKey) {
        try (var httpClient = HttpClientBuilder.create().build()) {
            http.addHeader("Authorization", getAuthHeader(client));
            var response = httpClient.execute(http);

            if (response.getStatusLine().getStatusCode() == 200) {
                infoConsumer.accept(translatable(successKey));
                return true;
            }

            infoConsumer.accept(translatable(failKey));
            SkinSpyInitializer.LOGGER.error("Code: {} Reason: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            SkinSpyInitializer.LOGGER.error(EntityUtils.toString(response.getEntity()));
            return false;

        } catch (Exception e) {
            infoConsumer.accept(Text.of(e.toString()));
            SkinSpyInitializer.LOGGER.error(e.toString());
        }

        return false;
    }

    private static Text translatable(String infoCode) {
        return Text.translatable(SkinSpyInitializer.MOD_ID + ".response." + infoCode);
    }
}
