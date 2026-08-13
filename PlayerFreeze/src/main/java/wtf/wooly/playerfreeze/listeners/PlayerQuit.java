package wtf.wooly.playerfreeze.listeners;

import wtf.wooly.playerfreeze.PlayerFreeze;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerFreeze plugin = PlayerFreeze.getPlugin();
        Player player = e.getPlayer();
        if (PlayerFreeze.frozenPlayers.contains(player.getUniqueId())) {
            List<String> commands = plugin.getConfig().getStringList("log_out_commands");

            for (String command : commands) {
                String filled = PlayerFreeze.setPapiPlaceholders(player, command.replace("[username]", player.getName()));
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), filled);
            }

            player.setAllowFlight(false);

            if (!plugin.getConfig().getBoolean("persist_across_relog"))
                PlayerFreeze.frozenPlayers.remove(player.getUniqueId());
        }
    }
}
