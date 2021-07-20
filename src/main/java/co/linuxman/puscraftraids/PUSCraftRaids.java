package co.linuxman.puscraftraids;

import co.linuxman.puscraftraids.commands.Commands;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PUSCraftRaids extends JavaPlugin {
    private static Plugin plugin;
    public static boolean cancelledRaid = false;

    public void onEnable() {
        //Initialize Plugin
        plugin = this;

        //Register Listeners
        //getServer().getPluginManager().registerEvents(new MobListener(), this);

        //Register Commands
        getCommand("raids").setExecutor(new Commands());

        //Create default config/directory
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        saveDefaultConfig();
    }

    public static Plugin getPlugin(){
        return plugin;
    }
}
