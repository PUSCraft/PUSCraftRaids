package co.linuxman.puscraftraids.listeners;

import co.linuxman.puscraftraids.commands.RaidCommands;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobListener implements Listener {
    @EventHandler
    public void onMythicMobDead(MythicMobDeathEvent event) {
        if (RaidCommands.region != null) {
            AbstractEntity mobEntity = event.getMob().getEntity();
            if (RaidCommands.MmEntityList.contains(mobEntity)) {
                LivingEntity killer = event.getKiller();
                if (killer instanceof Player) {
                    Player player = (Player)killer;
                    if (!RaidCommands.raidKills.containsKey(player.getName())) {
                        RaidCommands.raidKills.put(player.getName(), 1);
                    } else {
                        RaidCommands.raidKills.put(player.getName(), (Integer)RaidCommands.raidKills.get(player.getName()) + 1);
                    }

                    if (RaidCommands.bossSpawned && mobEntity.equals(RaidCommands.bossEntity)) {
                        RaidCommands.boss = "NONE";
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[Tier" + String.valueOf(RaidCommands.tier) + " Raid] &4&lBoss slain by &6&l" + player.getName()));
                    }
                } else {
                    ++RaidCommands.otherDeaths;
                }
            }
        }

    }
}
