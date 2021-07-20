package co.linuxman.puscraftraids.commands;

import co.linuxman.puscraftraids.PUSCraftRaids;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Player player = ((Player)sender);

        if(sender.hasPermission("puscraftraids.admin")){
            if(args.length == 0){
                //Make Fancy Help
                player.sendMessage("[RaidsPerRegion] Invalid arguments");
                player.sendMessage("[RaidsPerRegion] Reload Config: /raid reload");
                player.sendMessage("[RaidsPerRegion] View Source Code: /raid source");
            }else if(cmd.getName().equalsIgnoreCase("source")){
                //Message with source link
                String website = String.format("Source: %s",PUSCraftRaids.getPlugin().getDescription().getWebsite());
                player.sendMessage(website);
            }else if(cmd.getName().equalsIgnoreCase("reload")){
                //Reload Config
                PUSCraftRaids.getPlugin().reloadConfig();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded Config"));
            }
        }
        sender.sendMessage("You do not have permission for this command");
        return false;
    }
}
