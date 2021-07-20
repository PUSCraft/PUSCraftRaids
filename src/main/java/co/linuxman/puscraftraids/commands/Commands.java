package co.linuxman.puscraftraids.commands;

import co.linuxman.puscraftraids.PUSCraftRaids;
import co.linuxman.puscraftraids.raids.RaidManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        RaidManager raidManager = new RaidManager();

        if(sender.hasPermission("puscraftraids.admin")){
            if(cmd.getName().equalsIgnoreCase("raids") || cmd.getName().equalsIgnoreCase("puscraftraids")){
                if(args.length == 0){
                    //Make Fancy Help
                    sender.sendMessage("[RaidsPerRegion] Invalid arguments");
                    sender.sendMessage("[RaidsPerRegion] Reload Config: /raid reload");
                    sender.sendMessage("[RaidsPerRegion] View Source Code: /raid source");
                }else if(args.length == 1 && args[0].equalsIgnoreCase("source")){
                    //Message with source link
                    String website = String.format("Source: %s",PUSCraftRaids.getPlugin().getDescription().getWebsite());
                    sender.sendMessage(website);
                }else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
                    //Reload Config
                    PUSCraftRaids.getPlugin().reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded Config"));
                }else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")){
                    if(raidManager.isRaidInProgress){
                        raidManager.cancelRaid();
                        sender.sendMessage("[RaidsPerRegion] Raid has been canceled");
                    }
                }
            }
        }
        sender.sendMessage("You do not have permission for this command");
        return false;
    }
}
