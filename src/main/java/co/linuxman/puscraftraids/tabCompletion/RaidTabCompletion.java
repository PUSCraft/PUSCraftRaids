package co.linuxman.puscraftraids.tabCompletion;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaidTabCompletion implements TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        Player p;
        World w;
        boolean isConsole;
        if (sender instanceof Player) {
            p = (Player)sender;
            w = p.getWorld();
            isConsole = false;
        } else {
            ArrayList<Player> online = new ArrayList(Bukkit.getOnlinePlayers());
            w = ((Player)online.get(0)).getWorld();
            isConsole = true;
            p = null;
        }

        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(bukkitWorld);
        Object[] regionHolder = regions.getRegions().keySet().toArray();
        List<String> completions = new ArrayList();
        if (args.length == 1 && sender.hasPermission("raidsperregion.raid")) {
            completions.add("region");
            completions.add("town");
            completions.add("cancel");
            return completions;
        } else if (args.length == 2 && sender.hasPermission("raidsperregion.raid") && args[0].equalsIgnoreCase("region")) {
            for(int i = 0; i < regionHolder.length; ++i) {
                completions.add(regionHolder[i].toString());
            }

            return completions;
        } /*else if (args.length == 2 && sender.hasPermission("raidsperregion.raid") && args[0].equalsIgnoreCase("town")) {
            Plugin plugin = RaidCommands.plugin;
            PluginManager pluginManager = plugin.getServer().getPluginManager();
            Towny towny = (Towny)pluginManager.getPlugin("Towny");
            TownyUniverse uni = towny.getTownyUniverse();
            Object[] townHolder = uni.getTowns().toArray();

            for(int i = 0; i < townHolder.length; ++i) {
                completions.add(townHolder[i].toString());
            }

            return completions;
        }*/ else if (args.length != 3 || !sender.hasPermission("raidsperregion.raid") || !args[0].equalsIgnoreCase("region") && !args[0].equalsIgnoreCase("town")) {
            return Collections.emptyList();
        } else {
            completions.add("1");
            completions.add("2");
            completions.add("3");
            return completions;
        }
    }
}
