package wtf.wooly.flyspeed;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import wtf.wooly.flyspeed.commands.FlySpeedCommand;
import wtf.wooly.flyspeed.listeners.PlayerQuitListener;
import wtf.wooly.common.message.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FlySpeed extends JavaPlugin {

    private static final Map<UUID, Float> previousSpeeds = new HashMap<>();
    private Messages messages;

    @Override
    public void onEnable() {
        messages = Messages.load(this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(FlySpeedCommand.node(this), "Set a fly speed for you or somebody else", List.of("fs", "flyspeed"));
        });
    }

    public Messages messages() {
        return messages;
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, Float> entry : previousSpeeds.entrySet()) {
            Player player = getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.setFlySpeed(entry.getValue());
            }
        }
        previousSpeeds.clear();
    }

    /** Records a player's fly speed before this plugin changes it, if not already tracked. */
    public static void trackChange(Player player) {
        previousSpeeds.putIfAbsent(player.getUniqueId(), player.getFlySpeed());
    }

    /** Restores a player's fly speed to what it was before this plugin last changed it. */
    public static void restore(Player player) {
        Float previous = previousSpeeds.remove(player.getUniqueId());
        if (previous != null) {
            player.setFlySpeed(previous);
        }
    }
}
