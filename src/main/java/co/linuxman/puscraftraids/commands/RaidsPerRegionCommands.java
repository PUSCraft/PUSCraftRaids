package co.linuxman.puscraftraids.commands;

import co.linuxman.puscraftraids.PUSCraftRaids;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaidsPerRegionCommands implements CommandExecutor {
    private PUSCraftRaids plugin;

    public RaidsPerRegionCommands(PUSCraftRaids plugin) {
        this.plugin = plugin;
        plugin.getCommand("raidsperregion").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isConsole = false;
        Player p;
        if (sender instanceof Player) {
            p = (Player)sender;
        } else {
            isConsole = true;
            p = null;
        }

        if (args.length == 0) {
            if (isConsole) {
                this.plugin.getLogger().info("Invalid arguments");
                this.plugin.getLogger().info("Reload Config: /raidsperregion reload");
                this.plugin.getLogger().info("View Source Code: /raidsperregion source");
            } else {

            }

            return false;
        } else {
            if (args[0].equalsIgnoreCase("source")) {
                if (isConsole) {
                    this.plugin.getLogger().info("This plugin is an open source project developed by ShermansWorld and KristOJa");
                    this.plugin.getLogger().info("Link to source code: https://github.com/ShermansWorld/RaidsPerRegion/");
                } else {
                    p.sendMessage("[RaidsPerRegion] This plugin is an open source project developed by ShermansWorld and KristOJa");
                    p.sendMessage("[RaidsPerRegion] Link to source code: https://github.com/ShermansWorld/RaidsPerRegion/");
                }
            } else {
                if (!args[0].equalsIgnoreCase("version")) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (!isConsole && !p.hasPermission("raidsperregion.reload")) {
                            p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
                            return false;
                        }

                        this.plugin.reloadConfig();
                        this.plugin.saveDefaultConfig();
                        if (isConsole) {
                            this.plugin.getLogger().info("config.yml reloaded");
                        } else {
                            p.sendMessage("[RaidsPerRegion] config.yml reloaded");
                        }

                        return false;
                    }

                    if (isConsole) {
                        this.plugin.getLogger().info("Invalid arguments");
                        this.plugin.getLogger().info("Reload Config: /raidsperregion reload");
                        this.plugin.getLogger().info("View Source Code: /raidsperregion source");
                    } else {
                        p.sendMessage("[RaidsPerRegion] Invalid arguments");
                        p.sendMessage("[RaidsPerRegion] Reload Config: /raidsperregion reload");
                        p.sendMessage("[RaidsPerRegion] View Source Code: /raidsperregion source");
                    }

                    return false;
                }

                if (isConsole) {
                    this.plugin.getLogger().info("Your server is running RaidsPerRegion Version 1.3 for Minecraft 1.16.5");
                } else {
                    p.sendMessage("[RaidsPerRegion] Your server is running RaidsPerRegion Version 1.3 for Minecraft 1.16.5");
                }
            }

            return false;
        }
    }
}
