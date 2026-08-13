package wtf.wooly.combattag;

import org.bukkit.scheduler.BukkitRunnable;
import wtf.wooly.combattag.commands.CT;
import wtf.wooly.combattag.listeners.PlayerCommandPreprocess;
import wtf.wooly.combattag.listeners.PlayerDeath;
import wtf.wooly.combattag.listeners.PlayerHitPlayer;
import wtf.wooly.combattag.listeners.PlayerQuit;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import me.clip.placeholderapi.PlaceholderAPI;
import wtf.wooly.combattag.papi.PAPIExpansion;
import wtf.wooly.common.config.ConfigMigrator;
import wtf.wooly.common.message.Messages;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTag extends JavaPlugin {
    public static final String perms = "combattag.admin";
    private static CombatTag instance;
    public static final Map<UUID, Long> playersInCombat = new ConcurrentHashMap<>();
    /* Everyone who has tagged a player, so we know when the last of them is gone */
    public static final Map<UUID, Set<UUID>> combatOpponents = new ConcurrentHashMap<>();
    /* The name of whoever hit a player most recently, to name in the combat-log announcement */
    public static final Map<UUID, String> lastOpponent = new ConcurrentHashMap<>();
    private static boolean papiSupport = false;
    public static BukkitTask actionBarTask;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;
        reloadAll();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(CT.node(this), "Manage CombatTag", List.of("ct"));
        });

        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocess(), this);
        getServer().getPluginManager().registerEvents(new PlayerHitPlayer(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeath(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found, enabling placeholders.");
            papiSupport = true;
            new PAPIExpansion().register();
        } else {
            getLogger().warning("PlaceholderAPI was not found, placeholders won't be able to be used!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Detecting plugin shutdown, removing all active combat tags.");
        for (int taskId : PlayerHitPlayer.playerTaskLog.values()) {
            getServer().getScheduler().cancelTask(taskId);
        }
        PlayerHitPlayer.playerTaskLog.clear();
        playersInCombat.clear();
        combatOpponents.clear();
        lastOpponent.clear();
    }

    public static Component message(Player player, String key, Map<String, String> placeholders) {
        String message = instance.messages.raw(key);
        if (papiSupport && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return Messages.render(message, placeholders);
    }

    public void reloadAll() {
        ConfigMigrator.migrate(this, "config.yml");
        reloadConfig();
        messages = Messages.load(this);

        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        long period = 5L;

        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playersInCombat.keySet()) {
                    Player player = getServer().getPlayer(uuid);
                    if (player != null && !messages.raw("in-combat-action-bar").isBlank()) {
                        player.sendActionBar(message(player, "in-combat-action-bar", Map.of()));
                    }
                }
            }
        }.runTaskTimer(this, 0L, period);
    }

    public static CombatTag getPlugin() {
        return instance;
    }

    public Messages messages() {
        return messages;
    }
}
