package org.spigotmc;

import net.minecraft.server.MinecraftServer;
import network.secondlife.spigot.SLSpigotConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;

public class TicksPerSecondCommand extends Command
{

    public TicksPerSecondCommand(String name)
    {
        super( name );
        this.description = "Gets the current ticks per second for the server";
        this.usageMessage = "/tps";
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args)
    {
        if (!testPermission(sender)) {
            return true;
        }

        if (sender.hasPermission("bukkit.command.tps.advanced")) {
            double[] tps = Bukkit.spigot().getTPS();
            String[] tpsAvg = new String[tps.length];

            for (int i = 0; i < tps.length; i++) {
                tpsAvg[i] = formatAdvancedTps(tps[i]);
            }

            int entities = MinecraftServer.getServer().entities;
            int activeEntities = MinecraftServer.getServer().activeEntities;
            double activePercent = Math.round(10000.0 * activeEntities / entities) / 100.0;

            sender.sendMessage(ChatColor.GOLD + "TPS from last 1m, 5m, 15m: " + StringUtils.join(tpsAvg, ", "));
            sender.sendMessage(ChatColor.GOLD + "Last tick: "
                    + formatTickTime((System.currentTimeMillis() - MinecraftServer.getServer().lastTick)) + " ms "
                    + ChatColor.GOLD + "▏ " + ChatColor.GOLD + "Full tick: "
                    + formatTickTime(MinecraftServer.getServer().lastTickTime) + " ms "
                    + ChatColor.GOLD + "▏ Uptime: " + ChatColor.GREEN + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime(), true, true));
            sender.sendMessage(ChatColor.GOLD + "Online players: " + ChatColor.GREEN
                    + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + ChatColor.GOLD
                    + " ▏ Active entities: " + ChatColor.GREEN + activeEntities + '/' + entities + " (" + activePercent + "%)");

            int active = 0, daemon = 0;

            for(Thread thread : Thread.getAllStackTraces().keySet()) {
                if(thread.isAlive()) {
                    active++;
                }

                if(thread.isDaemon()) {
                    daemon++;
                }
            }

            sender.sendMessage(ChatColor.GOLD + "Active threads: " + ChatColor.GREEN + active + ChatColor.GOLD
                    + " ▏ Daemon threads: " + ChatColor.GREEN + daemon);
            sender.sendMessage(ChatColor.GOLD + "Mode: " + ChatColor.GREEN + (SLSpigotConfig.uhc ? "UHC" : SLSpigotConfig.practice ? "Practice" : "Default"));
        } else {
            double tps = Bukkit.spigot().getTPS()[1];
            StringBuilder tpsBuilder = new StringBuilder();

            tpsBuilder.append(ChatColor.GOLD).append("Server performance: ");
            tpsBuilder.append(formatBasicTps(tps)).append(ChatColor.GOLD).append("/20.0");
            tpsBuilder.append(" [").append(tps > 18.0 ? ChatColor.GREEN : tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED);

            int i = 0;

            for (; i < Math.round(tps); i++) {
                tpsBuilder.append("|");
            }

            tpsBuilder.append(ChatColor.DARK_GRAY);

            for (; i < 20; i++) {
                tpsBuilder.append("|");
            }

            tpsBuilder.append(ChatColor.GOLD).append("]");
            sender.sendMessage(tpsBuilder.toString());
        }

        return true;
    }

    private static String formatTickTime(double time) {
        return (time < 40.0D ? ChatColor.GREEN : time < 60.0D ? ChatColor.YELLOW : ChatColor.RED).toString() + Math.round(time * 10.0D) / 10.0D;
    }

    private static String formatAdvancedTps(double tps) {
        return (tps > 18.0 ? ChatColor.GREEN : tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED).toString() + Math.min(Math.round(tps * 100.0D) / 100.0, 20.0);
    }

    private String formatBasicTps(double tps) {
        return (tps > 18.0 ? ChatColor.GREEN : tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED).toString() + Math.min(Math.round(tps * 10.0D) / 10.0D, 20.0D);
    }

}
