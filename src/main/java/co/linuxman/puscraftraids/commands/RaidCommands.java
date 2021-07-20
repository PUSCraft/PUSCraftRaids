package co.linuxman.puscraftraids.commands;

import co.linuxman.puscraftraids.PUSCraftRaids;
import co.linuxman.puscraftraids.configmanager.ConfigManager;
import co.linuxman.puscraftraids.extras.Title;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.util.*;

public class RaidCommands implements CommandExecutor {
    public static PUSCraftRaids plugin;
    private static boolean timeReached = false;
    private static int totalKills;
    private static boolean maxMobsReached = false;
    private static List<String> mMMobNames = new ArrayList();
    private static List<Double> chances = new ArrayList();
    private static List<Integer> priorities = new ArrayList();
    private boolean runOnce = false;
    private int tierCountdown;
    private String tempStr = "";
    private static String tempStr2 = "";
    private static Map<String, String> scoreboardPlayerData = new HashMap();
    private int minutes;
    private boolean hasMobsOn;
    public static List<Player> playersInRegion = new ArrayList();
    public static Map<String, Integer> raidKills = new HashMap();
    public static int otherDeaths = 0;
    public static List<AbstractEntity> MmEntityList = new ArrayList();
    public static int mobsSpawned = 0;
    public static boolean bossSpawned = false;
    public static String boss = "NONE";
    public static double mobLevel = 1.0D;
    public static AbstractEntity bossEntity;
    public static int tier = 1;
    public static ProtectedRegion region;

    public RaidCommands(PUSCraftRaids plugin) {
        RaidCommands.plugin = plugin;
        plugin.getCommand("raid").setExecutor(this);
    }

    private static void spawnMobs(Random rand, List<Location> regionPlayerLocations, int scoreCounter, MobManager mm, List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, double mobLevel, Scoreboard board, Objective objective) {
        for(int j = 0; j < playersInRegion.size(); ++j) {
            int randomPlayerIdx = rand.nextInt(playersInRegion.size());
            World w = ((Player)playersInRegion.get(j)).getWorld();
            int x = ((Location)regionPlayerLocations.get(randomPlayerIdx)).getBlockX() + rand.nextInt(50) - 25;
            int y = ((Location)regionPlayerLocations.get(randomPlayerIdx)).getBlockY();
            int z = ((Location)regionPlayerLocations.get(randomPlayerIdx)).getBlockZ() + rand.nextInt(50) - 25;
            int spawnRate = rand.nextInt(3);
            int numPlayersInRegion = playersInRegion.size();
            int mobsAlive = mobsSpawned - scoreCounter - otherDeaths;
            if (mobsAlive >= numPlayersInRegion * maxMobsPerPlayer) {
                maxMobsReached = true;
            } else {
                maxMobsReached = false;
            }

            if (spawnRate == 2 && !maxMobsReached) {
                List<Integer> hitIdxs = new ArrayList();

                int maxPriority;
                int maxPriorityIdx;
                for(maxPriority = 0; maxPriority < mMMobNames.size(); ++maxPriority) {
                    maxPriorityIdx = rand.nextInt(1000) + 1;
                    if ((double)maxPriorityIdx <= (Double)chances.get(maxPriority) * 1000.0D) {
                        hitIdxs.add(maxPriority);
                    }
                }

                maxPriority = 0;
                maxPriorityIdx = 0;

                for(int n = 0; n < hitIdxs.size(); ++n) {
                    if ((Integer)priorities.get((Integer)hitIdxs.get(n)) > maxPriority) {
                        maxPriority = (Integer)priorities.get((Integer)hitIdxs.get(n));
                        maxPriorityIdx = (Integer)hitIdxs.get(n);
                    }
                }

                String mythicMobName = (String)mMMobNames.get(maxPriorityIdx);
                if (w.getBlockAt(x, y, z).getType() == Material.AIR) {
                    while(w.getBlockAt(x, y, z).getType() == Material.AIR) {
                        --y;
                    }

                    y += 2;
                } else {
                    while(w.getBlockAt(x, y, z).getType() != Material.AIR) {
                        ++y;
                    }

                    ++y;
                }

                Location mobSpawnLocation = new Location(w, (double)x, (double)y, (double)z);
                ActiveMob mob = mm.spawnMob(mythicMobName, mobSpawnLocation, mobLevel);
                if (mob != null) {
                    AbstractEntity entityOfMob = mob.getEntity();
                    MmEntityList.add(entityOfMob);
                    ++mobsSpawned;
                }
            }

            board.resetScores(tempStr2);
            totalKills = scoreCounter;
            Score tempTotalScore = objective.getScore(ChatColor.AQUA + "Total Kills:      " + totalKills);
            tempTotalScore.setScore(3);
            tempStr2 = ChatColor.AQUA + "Total Kills:      " + totalKills;
        }

    }

    public static void checkPlayersInRegion(final Scoreboard board, final Objective objective, final MobManager mm, final List<String> mMMobNames, final List<Double> chances, final List<Integer> priorities, final int maxMobsPerPlayer, long conversionSpawnRateMultiplier, final double mobLevel) {
        final int[] id = new int[1];
        final Random rand = new Random();
        id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                if (RaidCommands.timeReached) {
                    Bukkit.getServer().getScheduler().cancelTask(id[0]);
                } else {
                    List<Player> playerList = new ArrayList(Bukkit.getOnlinePlayers());
                    RaidCommands.playersInRegion = new ArrayList();
                    List<Location> onlinePlayerLocations = new ArrayList();
                    List<Location> regionPlayerLocations = new ArrayList();
                    int scoreCounter = 0;

                    int n;
                    for(n = 0; n < playerList.size(); ++n) {
                        onlinePlayerLocations.add(((Player)playerList.get(n)).getLocation());
                        if (RaidCommands.region.contains(((Location)onlinePlayerLocations.get(n)).getBlockX(), ((Location)onlinePlayerLocations.get(n)).getBlockY(), ((Location)onlinePlayerLocations.get(n)).getBlockZ())) {
                            RaidCommands.playersInRegion.add((Player)playerList.get(n));
                            regionPlayerLocations.add(((Player)playerList.get(n)).getLocation());
                        }
                    }

                    for(n = 0; n < RaidCommands.playersInRegion.size(); ++n) {
                        if (((Player)RaidCommands.playersInRegion.get(n)).getScoreboard() != board) {
                            ((Player)RaidCommands.playersInRegion.get(n)).setScoreboard(board);
                        }

                        if (RaidCommands.raidKills.containsKey(((Player)RaidCommands.playersInRegion.get(n)).getName())) {
                            if (RaidCommands.scoreboardPlayerData.containsKey(((Player)RaidCommands.playersInRegion.get(n)).getName())) {
                                board.resetScores((String)RaidCommands.scoreboardPlayerData.get(((Player)RaidCommands.playersInRegion.get(n)).getName()));
                            }

                            Score score = objective.getScore(ChatColor.YELLOW + ((Player)RaidCommands.playersInRegion.get(n)).getName() + ":    " + RaidCommands.raidKills.get(((Player)RaidCommands.playersInRegion.get(n)).getName()));
                            RaidCommands.scoreboardPlayerData.put(((Player)RaidCommands.playersInRegion.get(n)).getName(), ChatColor.YELLOW + ((Player)RaidCommands.playersInRegion.get(n)).getName() + ":    " + RaidCommands.raidKills.get(((Player)RaidCommands.playersInRegion.get(n)).getName()));
                            score.setScore(0);
                            scoreCounter += (Integer)RaidCommands.raidKills.get(((Player)RaidCommands.playersInRegion.get(n)).getName());
                        }
                    }

                    RaidCommands.spawnMobs(rand, regionPlayerLocations, scoreCounter, mm, mMMobNames, chances, priorities, maxMobsPerPlayer, mobLevel, board, objective);
                }

            }
        }, 0L, 20L / conversionSpawnRateMultiplier);
    }

    private void getMobsFromConfig() {
        Set<String> mmMobs = plugin.getConfig().getConfigurationSection("RaidMobs").getKeys(false);
        Iterator it = mmMobs.iterator();

        while(it.hasNext()) {
            mMMobNames.add((String)it.next());
        }

        for(int k = 0; k < mMMobNames.size(); ++k) {
            double chance = plugin.getConfig().getConfigurationSection("RaidMobs").getDouble((String)mMMobNames.get(k) + ".Chance");
            int priority = plugin.getConfig().getConfigurationSection("RaidMobs").getInt((String)mMMobNames.get(k) + ".Priority");
            chances.add(chance);
            priorities.add(priority);
        }

    }

    private boolean isCancelledRaid(String tier, CommandSender sender) {
        if (!PUSCraftRaids.cancelledRaid) {
            return false;
        } else {
            Title title = new Title();
            String raidCancelledTitle = plugin.getConfig().getString("RaidCancelledTitle");
            String raidCancelledSubtitle = plugin.getConfig().getString("RaidCancelledSubtitle");
            if (raidCancelledTitle.contains("@TIER")) {
                raidCancelledTitle = raidCancelledTitle.replaceAll("@TIER", tier);
            }

            if (raidCancelledSubtitle.contains("@TIER")) {
                raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TIER", tier);
            }

            if (region != null) {
                if (raidCancelledTitle.contains("@REGION")) {
                    raidCancelledTitle = raidCancelledTitle.replaceAll("@REGION", region.getId());
                }

                if (raidCancelledSubtitle.contains("@REGION")) {
                    raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@REGION", region.getId());
                }
            }

            /*if (town != null) {
                if (raidCancelledTitle.contains("@TOWN")) {
                    raidCancelledTitle = raidCancelledTitle.replaceAll("@TOWN", town.getName());
                }

                if (raidCancelledSubtitle.contains("@TOWN")) {
                    raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TOWN", town.getName());
                }
            }*/

            if (raidCancelledTitle.contains("@SENDER")) {
                raidCancelledTitle = raidCancelledTitle.replaceAll("@SENDER", sender.getName());
            }

            if (raidCancelledSubtitle.contains("@SENDER")) {
                raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@SENDER", sender.getName());
            }

            int i;
            for(i = 0; i < playersInRegion.size(); ++i) {
                title.send((Player)playersInRegion.get(i), ChatColor.translateAlternateColorCodes('&', raidCancelledTitle), ChatColor.translateAlternateColorCodes('&', raidCancelledSubtitle), 10, 60, 10);
            }

            for(i = 0; i < MmEntityList.size(); ++i) {
                if (((AbstractEntity)MmEntityList.get(i)).isLiving()) {
                    ((AbstractEntity)MmEntityList.get(i)).remove();
                }
            }

            return true;
        }
    }

    private boolean isWonRaid(String tier, int goal, String boss, MobManager mm, double mobLevel, CommandSender sender) {
        if (totalKills < goal) {
            return false;
        } else {
            Title title;
            int i;
            if (boss.equalsIgnoreCase("NONE")) {
                title = new Title();
                String raidWinTitle = plugin.getConfig().getString("RaidWinTitle");
                String raidWinSubtitle = plugin.getConfig().getString("RaidWinSubtitle");
                if (raidWinTitle.contains("@TIER")) {
                    raidWinTitle = raidWinTitle.replaceAll("@TIER", tier);
                }

                if (raidWinSubtitle.contains("@TIER")) {
                    raidWinSubtitle = raidWinSubtitle.replaceAll("@TIER", tier);
                }

                if (region != null) {
                    if (raidWinTitle.contains("@REGION")) {
                        raidWinTitle = raidWinTitle.replaceAll("@REGION", region.getId());
                    }

                    if (raidWinSubtitle.contains("@REGION")) {
                        raidWinSubtitle = raidWinSubtitle.replaceAll("@REGION", region.getId());
                    }
                }

                /*if (town != null) {
                    if (raidWinTitle.contains("@TOWN")) {
                        raidWinTitle = raidWinTitle.replaceAll("@TOWN", town.getName());
                    }

                    if (raidWinSubtitle.contains("@TOWN")) {
                        raidWinSubtitle = raidWinSubtitle.replaceAll("@TOWN", town.getName());
                    }
                }*/

                if (raidWinTitle.contains("@SENDER")) {
                    raidWinTitle = raidWinTitle.replaceAll("@SENDER", sender.getName());
                }

                if (raidWinSubtitle.contains("@SENDER")) {
                    raidWinSubtitle = raidWinSubtitle.replaceAll("@SENDER", sender.getName());
                }

                for(i = 0; i < playersInRegion.size(); ++i) {
                    title.send((Player)playersInRegion.get(i), ChatColor.translateAlternateColorCodes('&', raidWinTitle), ChatColor.translateAlternateColorCodes('&', raidWinSubtitle), 10, 60, 10);
                }

                for(i = 0; i < MmEntityList.size(); ++i) {
                    if (((AbstractEntity)MmEntityList.get(i)).isLiving()) {
                        ((AbstractEntity)MmEntityList.get(i)).damage(1000.0F);
                    }
                }

                if (plugin.getConfig().getBoolean("UseWinLossCommands")) {
                    List perPlayerCommands;
                    String command;
                    try {
                        perPlayerCommands = plugin.getConfig().getStringList("RaidWinCommands.Global");

                        for(i = 0; i < perPlayerCommands.size(); ++i) {
                            command = (String)perPlayerCommands.get(i);
                            if (region != null && command.contains("@REGION")) {
                                command = command.replaceAll("@REGION", region.getId());
                            }

                            /*if (town != null && command.contains("@TOWN")) {
                                command = command.replaceAll("@TOWN", town.getName());
                            }*/

                            if (command.contains("@TIER")) {
                                command = command.replaceAll("@TIER", tier);
                            }

                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    } catch (NullPointerException var19) {
                        var19.printStackTrace();
                    }

                    try {
                        perPlayerCommands = plugin.getConfig().getStringList("RaidWinCommands.PerPlayer");

                        for(i = 0; i < perPlayerCommands.size(); ++i) {
                            command = (String)perPlayerCommands.get(i);
                            if (region != null && command.contains("@REGION")) {
                                command = command.replaceAll("@REGION", region.getId());
                            }

                            /*if (town != null && command.contains("@TOWN")) {
                                command = command.replaceAll("@TOWN", town.getName());
                            }*/

                            if (command.contains("@TIER")) {
                                command = command.replaceAll("@TIER", tier);
                            }

                            Iterator var26 = raidKills.keySet().iterator();

                            while(var26.hasNext()) {
                                String key = (String)var26.next();
                                if (command.contains("@PLAYER")) {
                                    String playerCommand = command.replaceAll("@PLAYER", key);
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
                                }
                            }
                        }
                    } catch (NullPointerException var18) {
                        var18.printStackTrace();
                    }
                }

                return true;
            } else if (bossSpawned) {
                return false;
            } else {
                title = new Title();

                for(int n = 0; n < playersInRegion.size(); ++n) {
                    title.send((Player)playersInRegion.get(n), ChatColor.translateAlternateColorCodes('&', "&4&lBoss Spawned!"), ChatColor.translateAlternateColorCodes('&', "&6Kill the boss to win the raid"), 10, 60, 10);
                }

                bossSpawned = true;
                Random rand = new Random();
                int randIdx = rand.nextInt(playersInRegion.size());
                i = ((Player)playersInRegion.get(randIdx)).getLocation().getBlockX();
                i = ((Player)playersInRegion.get(randIdx)).getLocation().getBlockY();
                int z = ((Player)playersInRegion.get(randIdx)).getLocation().getBlockZ();
                World w = ((Player)playersInRegion.get(randIdx)).getWorld();
                Location spawnLocation = new Location(w, (double)i, (double)i, (double)z);
                ActiveMob mob = mm.spawnMob(boss, spawnLocation, mobLevel);
                if (mob != null) {
                    AbstractEntity entityOfMob = mob.getEntity();
                    bossEntity = entityOfMob;
                    MmEntityList.add(entityOfMob);
                    ++mobsSpawned;
                } else {
                    Bukkit.broadcastMessage(ChatColor.DARK_RED + "[RaidsPerRegion] ERROR WITH BOSS SPAWNED");
                }

                return false;
            }
        }
    }

    private boolean isLostRaid(String tier, int goal, int minutes, CommandSender sender) {
        if (tierCountdown == 0 && minutes == 0) {
            Title title = new Title();
            String raidLoseTitle = plugin.getConfig().getString("RaidLoseTitle");
            String raidLoseSubtitle = plugin.getConfig().getString("RaidLoseSubtitle");
            if (raidLoseTitle.contains("@TIER")) {
                raidLoseTitle = raidLoseTitle.replaceAll("@TIER", tier);
            }

            if (raidLoseSubtitle.contains("@TIER")) {
                raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TIER", tier);
            }

            if (region != null) {
                if (raidLoseTitle.contains("@REGION")) {
                    raidLoseTitle = raidLoseTitle.replaceAll("@REGION", region.getId());
                }

                if (raidLoseSubtitle.contains("@REGION")) {
                    raidLoseSubtitle = raidLoseSubtitle.replaceAll("@REGION", region.getId());
                }
            }

            /*if (town != null) {
                if (raidLoseTitle.contains("@TOWN")) {
                    raidLoseTitle = raidLoseTitle.replaceAll("@TOWN", town.getName());
                }

                if (raidLoseSubtitle.contains("@TOWN")) {
                    raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TOWN", town.getName());
                }
            }*/

            if (raidLoseTitle.contains("@SENDER")) {
                raidLoseTitle = raidLoseTitle.replaceAll("@SENDER", sender.getName());
            }

            if (raidLoseSubtitle.contains("@SENDER")) {
                raidLoseSubtitle = raidLoseSubtitle.replaceAll("@SENDER", sender.getName());
            }

            if (totalKills < goal) {
                //int i;
                for(int i = 0; i < playersInRegion.size(); ++i) {
                    title.send((Player)playersInRegion.get(i), ChatColor.translateAlternateColorCodes('&', raidLoseTitle), ChatColor.translateAlternateColorCodes('&', raidLoseSubtitle), 10, 60, 10);
                }

                if (!plugin.getConfig().getBoolean("MobsStayOnRaidLoss")) {
                    for(int i = 0; i < MmEntityList.size(); ++i) {
                        if (((AbstractEntity)MmEntityList.get(i)).isLiving()) {
                            ((AbstractEntity)MmEntityList.get(i)).remove();
                        }
                    }
                }

                if (plugin.getConfig().getBoolean("UseWinLossCommands")) {
                    //int i;
                    String command;
                    List perPlayerCommands;
                    try {
                        perPlayerCommands = plugin.getConfig().getStringList("RaidLoseCommands.Global");

                        for(int i = 0; i < perPlayerCommands.size(); ++i) {
                            command = (String)perPlayerCommands.get(i);
                            if (region != null && command.contains("@REGION")) {
                                command = command.replaceAll("@REGION", region.getId());
                            }

                            /*if (town != null && command.contains("@TOWN")) {
                                command = command.replaceAll("@TOWN", town.getName());
                            }*/

                            if (command.contains("@TIER")) {
                                command = command.replaceAll("@TIER", tier);
                            }

                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    } catch (NullPointerException var15) {
                        var15.printStackTrace();
                    }

                    try {
                        perPlayerCommands = plugin.getConfig().getStringList("RaidLoseCommands.PerPlayer");

                        for(int i = 0; i < perPlayerCommands.size(); ++i) {
                            command = (String)perPlayerCommands.get(i);
                            if (region != null && command.contains("@REGION")) {
                                command = command.replaceAll("@REGION", region.getId());
                            }

                            /*if (town != null && command.contains("@TOWN")) {
                                command = command.replaceAll("@TOWN", town.getName());
                            }*/

                            if (command.contains("@TIER")) {
                                command = command.replaceAll("@TIER", tier);
                            }

                            Iterator var12 = raidKills.keySet().iterator();

                            while(var12.hasNext()) {
                                String key = (String)var12.next();
                                if (command.contains("@PLAYER")) {
                                    String playerCommand = command.replaceAll("@PLAYER", key);
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
                                }
                            }
                        }
                    } catch (NullPointerException var14) {
                        var14.printStackTrace();
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void resetVariables() {
        timeReached = false;
        totalKills = 0;
        mobsSpawned = 0;
        maxMobsReached = false;
        playersInRegion = new ArrayList();
        MmEntityList = new ArrayList();
        mMMobNames = new ArrayList();
        raidKills = new HashMap();
        PUSCraftRaids.cancelledRaid = false;
        this.runOnce = false;
        priorities = new ArrayList();
        chances = new ArrayList();
        mMMobNames = new ArrayList();
        otherDeaths = 0;
        scoreboardPlayerData = new HashMap();
        bossSpawned = false;
    }

    public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
        ConfigManager cm = new ConfigManager();
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

        if (!isConsole && !p.hasPermission("raidsperregion.raid")) {
            p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
            return false;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (region == null) {
                if (isConsole) {
                    plugin.getLogger().info("There is not a raid in progress right now");
                } else {
                    p.sendMessage("[RaidsPerRegion] There is not a raid in progress right now");
                }

                return false;
            } else {
                if (region != null) {
                    if (isConsole) {
                        plugin.getLogger().info("Canceled raid on region" + region.getId());
                    } else {
                        p.sendMessage("[RaidsPerRegion] Canceled raid on region " + region.getId());
                    }
                }

                PUSCraftRaids.cancelledRaid = true;
                return false;
            }
        } else if (args.length != 3) {
            if (isConsole) {
                plugin.getLogger().info("Invalid arguments");
                plugin.getLogger().info("Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
            } else {
                p.sendMessage("[RaidsPerRegion] Invalid arguments");
                p.sendMessage("[RaidsPerRegion] Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
            }

            return false;
        } else if (region != null) {
            if (isConsole) {
                plugin.getLogger().info("There is already a raid in progress in region " + region.getId());
                plugin.getLogger().info("To cancel this raid type /raid cancel");
            } else {
                p.sendMessage("[RaidsPerRegion] There is already a raid in progress in region " + region.getId());
                p.sendMessage("[RaidsPerRegion] To cancel this raid type /raid cancel");
            }

            return false;
        } else {
            if (args[0].equalsIgnoreCase("region")) {
                com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(bukkitWorld);
                region = regions.getRegion(args[1]);
                Map<String, ProtectedRegion> regionMap = regions.getRegions();
                if (!regionMap.containsKey(args[1])) {
                    if (isConsole) {
                        plugin.getLogger().info("Invalid region. Useage: /raid region [region] [tier]");
                    } else {
                        p.sendMessage("[RaidsPerRegion] Invalid region. Useage: /raid region [region] [tier]");
                    }

                    return false;
                }
            }

            if (!cm.getTiers().getKeys(false).contains(args[2])) {
                if (isConsole) {
                    plugin.getLogger().info("Invalid tier. Useage: /raid [region] [tier]");
                } else {
                    p.sendMessage("[RaidsPerRegion] Invalid tier. Useage: /raid [region] [tier]");
                }

                return false;
            } else {
                long conversionSpawnRateMultiplier = 10L;
                double spawnRateMultiplier = 1.0D;
                tier = Integer.parseInt(args[2]);

                final int killsGoal = cm.getTiers().getConfigurationSection(args[2]).getInt("KillsGoal");
                tierCountdown = cm.getTiers().getConfigurationSection(args[2]).getInt("Time");
                int maxMobsPerPlayer = cm.getTiers().getConfigurationSection(args[2]).getInt("MaxMobsPerPlayer");
                spawnRateMultiplier = cm.getTiers().getConfigurationSection(args[2]).getDouble("SpawnRateMultiplier");
                conversionSpawnRateMultiplier = (long)spawnRateMultiplier;
                mobLevel = cm.getTiers().getConfigurationSection(args[2]).getDouble("MobLevel");
                if (cm.isSpawnBossOnKillGoalReached()) {
                    boss = cm.getTiers().getConfigurationSection(args[2]).getString("Boss");
                } else {
                    boss = "NONE";
                }

                if (conversionSpawnRateMultiplier == 0L) {
                    conversionSpawnRateMultiplier = 1L;
                    if (isConsole) {
                        plugin.getLogger().info("SpawnRateMultipiler too low! defaulting to 1.0");
                    } else {
                        p.sendMessage("[RaidsPerRegion] SpawnRateMultipiler too low! defaulting to 1.0");
                    }
                }

                resetVariables();
                final MobManager mm = MythicMobs.inst().getMobManager();
                if (region != null) {
                    if (region.getFlag(Flags.MOB_SPAWNING) == StateFlag.State.ALLOW) {
                        hasMobsOn = true;
                    } else {
                        hasMobsOn = false;
                        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
                    }
                }

                ArrayList<Player> online = new ArrayList(Bukkit.getOnlinePlayers());
                final Scoreboard board = ((Player)online.get(0)).getScoreboard();
                final Objective objective = board.registerNewObjective("raidKills", "dummy", "" + ChatColor.BOLD + ChatColor.DARK_RED + "Raid: " + "Tier " + args[2]);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                Score goalKills = objective.getScore(ChatColor.GOLD + "Goal:             " + killsGoal);
                Score totalScore = objective.getScore(ChatColor.AQUA + "Total Kills:      0");
                tempStr2 = ChatColor.AQUA + "Total Kills:      0";
                totalScore.setScore(3);
                goalKills.setScore(2);
                Score separater = objective.getScore(ChatColor.DARK_RED + "----------------------");
                separater.setScore(1);
                minutes = tierCountdown / 60;
                tierCountdown %= 60;
                getMobsFromConfig();
                if (region != null) {
                    checkPlayersInRegion(board, objective, mm, mMMobNames, chances, priorities, maxMobsPerPlayer, conversionSpawnRateMultiplier, mobLevel);
                }

                final int[] id = new int[1];
                id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    public void run() {
                        if (!playersInRegion.isEmpty() && !runOnce) {
                            runOnce = true;
                            Title title = new Title();
                            String raidAnnoucementTitle = cm.getRaidAnnoucementTitle().replace("@TIER",args[2])
                                    .replace("@REGION", region.getId())
                                    .replace("@SENDER", region.getId());
                            String raidAnnoucementSubtitle = cm.getRaidAnnoucementSubtitle().replace("@TIER", args[2])
                                    .replace("@REGION", sender.getName())
                                    .replace("@SENDER", sender.getName());

                            for(int n = 0; n < playersInRegion.size(); ++n) {
                                title.send((Player)playersInRegion.get(n), ChatColor.translateAlternateColorCodes('&', raidAnnoucementTitle), ChatColor.translateAlternateColorCodes('&', raidAnnoucementSubtitle), 10, 60, 10);
                            }
                        }

                        if (!isCancelledRaid(args[2], sender) && !isWonRaid(args[2], tierCountdown, RaidCommands.boss, mm, RaidCommands.mobLevel, sender) && !isLostRaid(args[2], tierCountdown, minutes, sender)) {
                            if (tierCountdown == 0 && minutes >= 1) {
                                minutes = minutes - 1;
                                tierCountdown += 60;
                            }

                            --tierCountdown;

                            try {
                                board.resetScores(tempStr);
                            } catch (NullPointerException var5) {
                            }

                            Score timer;
                            if (tierCountdown <= 9) {
                                timer = objective.getScore(ChatColor.GREEN + "Time:             " + minutes + ":0" + tierCountdown);
                                tempStr = ChatColor.GREEN + "Time:             " + minutes + ":0" + tierCountdown;
                                timer.setScore(4);
                            } else {
                                timer = objective.getScore(ChatColor.GREEN + "Time:             " + minutes + ":" + tierCountdown);
                                tempStr = ChatColor.GREEN + "Time:             " + minutes + ":" + tierCountdown;
                                timer.setScore(4);
                            }

                        } else {
                            Bukkit.getServer().getScheduler().cancelTask(id[0]);
                            objective.unregister();
                            timeReached = true;
                            boss = "NONE";
                            mobLevel = 1.0D;
                            if (region != null) {
                                if (!hasMobsOn) {
                                    region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
                                }

                                region = null;
                            }

                            /*if (RaidCommands.town != null) {
                                if (!RaidCommands.this.hasMobsOn) {
                                    RaidCommands.town.setHasMobs(false);
                                }

                                RaidCommands.town = null;
                            }*/
                        }
                    }
                }, 0L, 20L);
                return false;
            }
        }
    }
}