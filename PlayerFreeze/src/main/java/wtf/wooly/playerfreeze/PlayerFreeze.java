package wtf.wooly.playerfreeze;

import wtf.wooly.playerfreeze.listeners.PlayerDamaged;
import wtf.wooly.playerfreeze.listeners.PlayerJoin;
import wtf.wooly.playerfreeze.listeners.PlayerMove;
import wtf.wooly.playerfreeze.listeners.PlayerQuit;
import wtf.wooly.playerfreeze.commands.Freeze;
import wtf.wooly.playerfreeze.commands.PF;
import wtf.wooly.playerfreeze.commands.Unfreeze;
import wtf.wooly.playerfreeze.papi.PAPIExpansion;

import io.papermc.paper.command.brigadier.Commands;
import me.clip.placeholderapi.PlaceholderAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import wtf.wooly.common.config.ConfigMigrator;
import wtf.wooly.common.message.Messages;

public final class PlayerFreeze extends JavaPlugin {
    private static final List<String> VALID_PROT_LEVELS = List.of("none", "pvp", "invincible");
    private static PlayerFreeze instance;
    public static Set<UUID> frozenPlayers = new HashSet<>();
    private static final Map<UUID, Boolean> previousFlightState = new HashMap<>();
    public static final String permUse = "pf.use";
    public static final String permImmune = "pf.immune";
    public static final String permNotify = "pf.notify";
    public static final String permAdmin = "pf.admin";
    private static boolean papiSupport = false;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;
        reloadFiles();
        validateProtLevel();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(Freeze.node(this), "Freeze a player in place");
            commands.register(Unfreeze.node(this), "Unfreeze a frozen player");
            commands.register(PF.node(this), "Manage PlayerFreeze", List.of("playerfreeze", "pf"));
        });

        getServer().getPluginManager().registerEvents(new PlayerDamaged(), this);
        getServer().getPluginManager().registerEvents(new PlayerMove(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found, enabling placeholders.");
            papiSupport = true;
            new PAPIExpansion().register();
        } else {
            getLogger().warning("PlaceholderAPI was not found, placeholders won't be able to be used!");
        }

        getLogger().info("PlayerFreeze is enabled!");
    }

    @Override
    public void onDisable() {
        for (UUID playerId : new HashSet<>(frozenPlayers)) {
            Player player = getServer().getPlayer(playerId);
            if (player != null) {
                unfreezePlayer(player);
            }
        }
        frozenPlayers.clear();
        previousFlightState.clear();
    }

    public static PlayerFreeze getPlugin() {
        return instance;
    }

    public void reloadFiles() {
        ConfigMigrator.migrate(this, "config.yml");
        reloadConfig();
        messages = Messages.load(this);
    }

    public Messages messages() {
        return messages;
    }

    public static Component message(String key, Map<String, String> placeholders) {
        return instance.messages.get(key, placeholders);
    }

    /* Fills in a config value's PlaceholderAPI placeholders, if PlaceholderAPI is installed. */
    public static String setPapiPlaceholders(Player player, String text) {
        if (!papiSupport || player == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static void validateProtLevel() {
        String level = instance.getConfig().getString("protection_level", "invincible");
        if (!VALID_PROT_LEVELS.contains(level)) {
            instance.getLogger().warning("Invalid protection_level '" + level + "' in config.yml. Valid options: none, pvp, invincible. Defaulting to 'invincible'.");
            instance.getConfig().set("protection_level", "invincible");
            instance.saveConfig();
        }
    }

    public static void freezePlayer(Player player) {
        frozenPlayers.add(player.getUniqueId());
        previousFlightState.put(player.getUniqueId(), player.getAllowFlight());
        player.setAllowFlight(true);
        player.setFlying(false);
    }

    public static void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        Boolean hadFlight = previousFlightState.remove(player.getUniqueId());
        player.setAllowFlight(hadFlight != null && hadFlight);
    }

    public static void applyFreezeState(Player player) {
        if (frozenPlayers.contains(player.getUniqueId())) {
            player.setAllowFlight(true);
            player.setFlying(false);
        }
    }
}
