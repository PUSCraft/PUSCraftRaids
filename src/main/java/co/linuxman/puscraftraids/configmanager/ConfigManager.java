package co.linuxman.puscraftraids.configmanager;

import co.linuxman.puscraftraids.PUSCraftRaids;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager{
    private FileConfiguration config;
    private String RaidAnnoucementTitle;
    private String RaidAnnoucementSubtitle;
    private String RaidWinTitle;
    private String RaidWinSubtitle;
    private String RaidLoseTitle;
    private String RaidLoseSubtitle;
    private String RaidCancelledTitle;
    private String RaidCancelledSubtitle;
    private boolean SpawnBossOnKillGoalReached;
    private ConfigurationSection RaidMobs;
    private ConfigurationSection Tiers;
    private boolean UseWinLossCommands;
    private ConfigurationSection RaidWinCommands;
    private ConfigurationSection RaidLoseCommands;
    private boolean MobsStayOnRaidLoss;

    public ConfigManager(){
        config = PUSCraftRaids.getPlugin().getConfig();
        RaidAnnoucementTitle = formatColor(config.getString("RaidAnnoucementTitle"));
        RaidAnnoucementSubtitle = formatColor(config.getString("RaidAnnoucementSubtitle"));
        RaidWinTitle = formatColor(config.getString("RaidWinTitle"));
        RaidWinSubtitle = formatColor(config.getString("RaidWinSubtitle"));
        RaidLoseTitle = formatColor(config.getString("RaidLoseTitle"));
        RaidLoseSubtitle = formatColor(config.getString("RaidLoseSubtitle"));
        RaidCancelledTitle = formatColor(config.getString("RaidCancelledTitle"));
        RaidCancelledSubtitle = formatColor(config.getString("RaidCancelledSubtitle"));
        SpawnBossOnKillGoalReached = config.getBoolean("SpawnBossOnKillGoalReached");
        RaidMobs = config.getConfigurationSection("RaidMobs");
        Tiers = config.getConfigurationSection("Tiers");
        UseWinLossCommands = config.getBoolean("UseWinLossCommands");
        RaidWinCommands = config.getConfigurationSection("RaidWinCommands");
        RaidLoseCommands = config.getConfigurationSection("RaidLoseCommands");
        MobsStayOnRaidLoss = config.getBoolean("MobsStayOnRaidLoss");
    }

    public String getRaidAnnoucementTitle() {
        return RaidAnnoucementTitle;
    }

    public String getRaidAnnoucementSubtitle() {
        return RaidAnnoucementSubtitle;
    }

    public String getRaidWinTitle() {
        return RaidWinTitle;
    }

    public String getRaidWinSubtitle() {
        return RaidWinSubtitle;
    }

    public String getRaidLoseTitle() {
        return RaidLoseTitle;
    }

    public String getRaidLoseSubtitle() {
        return RaidLoseSubtitle;
    }

    public String getRaidCancelledTitle() {
        return RaidCancelledTitle;
    }

    public String getRaidCancelledSubtitle() {
        return RaidCancelledSubtitle;
    }

    public boolean isSpawnBossOnKillGoalReached() {
        return SpawnBossOnKillGoalReached;
    }

    public ConfigurationSection getRaidMobs() {
        return RaidMobs;
    }

    public ConfigurationSection getTiers() {
        return Tiers;
    }

    public boolean isUseWinLossCommands() {
        return UseWinLossCommands;
    }

    public ConfigurationSection getRaidWinCommands() {
        return RaidWinCommands;
    }

    public ConfigurationSection getRaidLoseCommands() {
        return RaidLoseCommands;
    }

    public boolean isMobsStayOnRaidLoss() {
        return MobsStayOnRaidLoss;
    }

    public String formatColor(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
