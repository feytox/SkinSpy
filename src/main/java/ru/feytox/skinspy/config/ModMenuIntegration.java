package ru.feytox.skinspy.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.feytox.skinspy.SkinSpyInitializer;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::createConfigScreen;
    }

    private static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal(SkinSpyInitializer.MOD_NAME));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ModConfig config = ModConfig.get();

        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/cobbled_deepslate.png"));

        builder.getOrCreateCategory(translatable("generalCategory"))
                .addEntry(entryBuilder.startBooleanToggle(
                        translatable("enableMod"),
                        config.enableMod)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.enableMod = value)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(
                        translatable("isBlocklistMode"),
                        config.isBlocklistMode)
                        .setDefaultValue(true)
                        .setTooltip(translatable("isBlocklistMode.tooltip"))
                        .setSaveConsumer(value -> config.isBlocklistMode = value)
                        .build())
                .addEntry(entryBuilder.startStrField(
                        translatable("serverTag"),
                        config.serverTag)
                        .setDefaultValue("[SkinSpy]")
                        .setTooltip(translatable("serverTag.tooltip"))
                        .setSaveConsumer(value -> config.serverTag = value)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(
                                translatable("enableSwitcher"),
                                config.enableSwitcher)
                        .setDefaultValue(false)
                        .setTooltip(translatable("enableSwitcher.tooltip"))
                        .setSaveConsumer(value -> config.enableSwitcher = value)
                        .build())
                .addEntry(entryBuilder.startIntField(
                        translatable("switcherDelay"),
                        config.switcherDelay)
                        .setDefaultValue(5)
                        .setMin(1)
                        .setTooltip(translatable("switcherDelay.tooltip"))
                        .setSaveConsumer(value -> config.switcherDelay = value)
                        .build())
                .addEntry(entryBuilder.startStrField(
                        translatable("defaultSkinName"),
                        config.defaultSkinName)
                        .setDefaultValue("")
                        .setTooltip(translatable("skinName.tooltip"))
                        .setSaveConsumer(value -> config.defaultSkinName = value)
                        .build())
                .addEntry(entryBuilder.startEnumSelector(
                        translatable("defaultSkinType"),
                        SkinType.class,
                        config.defaultSkinType)
                        .setDefaultValue(SkinType.SLIM)
                        .setTooltip(translatable("skinType.tooltip"))
                        .setSaveConsumer(value -> config.defaultSkinType = value)
                        .build())
                .addEntry(entryBuilder.startStrField(
                        translatable("customSkinName"),
                        config.customSkinName)
                        .setDefaultValue("")
                        .setTooltip(translatable("skinName.tooltip"))
                        .setSaveConsumer(value -> config.customSkinName = value)
                        .build())
                .addEntry(entryBuilder.startEnumSelector(
                                translatable("customSkinType"),
                                SkinType.class,
                                config.customSkinType)
                        .setDefaultValue(SkinType.SLIM)
                        .setTooltip(translatable("skinType.tooltip"))
                        .setSaveConsumer(value -> config.customSkinType = value)
                        .build())
                .addEntry(entryBuilder.startStrField(
                                translatable("defaultCapeName"),
                                config.defaultCapeName)
                        .setDefaultValue("")
                        .setTooltip(translatable("capeName.tooltip"))
                        .setSaveConsumer(value -> config.defaultCapeName = value)
                        .build())
                .addEntry(entryBuilder.startStrField(
                        translatable("customCapeName"),
                        config.customCapeName)
                        .setDefaultValue("")
                        .setTooltip(translatable("capeName.tooltip"))
                        .setSaveConsumer(value -> config.customCapeName = value)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(
                                translatable("showInfoInChat"),
                                config.showInfoInChat)
                        .setDefaultValue(true)
                        .setTooltip(translatable("showInfoInChat.tooltip"))
                        .setSaveConsumer(value -> config.showInfoInChat = value)
                        .build());

        builder.transparentBackground();
        return builder.setSavingRunnable(ModConfig::save).build();
    }

    private static Text translatable(String optionName) {
        return Text.translatable(SkinSpyInitializer.MOD_ID + ".config." + optionName);
    }
}
