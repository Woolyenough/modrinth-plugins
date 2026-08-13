package wtf.wooly.combattag.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventPriority;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        PlayerHitPlayer.clearCombat(player.getUniqueId());
        PlayerHitPlayer.releaseOpponents(player);
        player.sendActionBar(Component.empty());
    }
}
