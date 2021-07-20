package co.linuxman.puscraftraids;

import co.linuxman.puscraftraids.commands.Commands;
import co.linuxman.puscraftraids.listeners.MobListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PUSCraftRaids extends JavaPlugin {
    private static Plugin plugin;
    public static boolean cancelledRaid = false;

    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new MobListener(), this);

        //getCommand("raidsperregion").setTabCompleter(new RaidsPerRegionTabCompletion());
        //getCommand("raid").setTabCompleter(new RaidTabCompletion());
        getCommand("raid").setExecutor(new Commands());

        saveDefaultConfig();
    }

    public static Plugin getPlugin(){
        return plugin;
    }
}
