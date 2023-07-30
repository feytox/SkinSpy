package ru.feytox.skinspy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import ru.feytox.skinspy.SkinSpyInitializer;

@Config(name = SkinSpyInitializer.MOD_ID)
public class ModConfig implements ConfigData {

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static void load() {
        AutoConfig.getConfigHolder(ModConfig.class).load();
    }

    public boolean enableMod = true;
    public boolean isBlocklistMode = true;
    public String serverTag = "[SkinSpy]";
    public boolean enableSwitcher = false;
    public int switcherDelay = 5;
    public String defaultSkinName = "";
    public SkinType defaultSkinType = SkinType.SLIM;
    public String customSkinName = "";
    public SkinType customSkinType = SkinType.SLIM;
    public String defaultCapeName = "";
    public String customCapeName = "";
    public boolean showInfoInChat = true;
}
