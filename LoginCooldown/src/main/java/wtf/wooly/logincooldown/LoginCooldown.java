package wtf.wooly.logincooldown;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import wtf.wooly.common.config.ConfigMigrator;
import wtf.wooly.logincooldown.commands.LoginCooldownCommand;
import wtf.wooly.logincooldown.listeners.PlayerLoginListener;

import java.util.List;
import java.util.Map;

public final class LoginCooldown extends JavaPlugin {
    public static final String JOINS_PER = "joins-per";
    public static final String TIME_FRAME = "time-frame";
    /** The settings a server can change without editing config.yml by hand. */
    public static final List<String> SETTINGS = List.of(JOINS_PER, TIME_FRAME);

    private static final Map<String, Integer> DEFAULTS = Map.of(JOINS_PER, 3, TIME_FRAME, 450);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        reloadFiles();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(LoginCooldownCommand.node(this), "Manage LoginCooldown", List.of("logincooldown"));
        });

        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
    }

    @Override
    public void onDisable() {
        // Nothing :P
    }

    public void reloadFiles() {
        ConfigMigrator.migrate(this, "config.yml");
        reloadConfig();
        validateSettings();
    }

    public static Component deserialize(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public int setting(String setting) {
        return getConfig().getInt(setting, DEFAULTS.get(setting));
    }

    public void validateSettings() {
        boolean changed = false;

        for (String setting : SETTINGS) {
            int value = getConfig().getInt(setting, DEFAULTS.get(setting));
            if (value < 1) {
                getLogger().warning("Invalid " + setting + " '" + value + "' in config.yml. Must be 1 or greater. Defaulting to " + DEFAULTS.get(setting) + ".");
                getConfig().set(setting, DEFAULTS.get(setting));
                changed = true;
            }
        }

        if (changed) {
            saveConfig();
        }
    }
}
