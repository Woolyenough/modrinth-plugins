package wtf.wooly.combattag.listeners;

import wtf.wooly.combattag.CombatTag;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerQuit implements Listener {
    /* The combat logger whose death setHealth(0) below is causing, when we announced it ourselves */
    private static UUID silencedDeath;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CombatTag plugin = CombatTag.getPlugin();
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Read before clearing, which is what forgets who they were fighting
        String opponent = CombatTag.lastOpponent.get(playerId);

        if (CombatTag.playersInCombat.containsKey(playerId) && shouldKill(plugin, event)) {
            // Announce first, so we know whether the vanilla death message would be a duplicate
            silencedDeath = announce(plugin, player, opponent) ? playerId : null;
            try {
                player.setHealth(0.0);
            } finally {
                silencedDeath = null;
            }
        }

        // Leaving ends the tag whether or not they were killed, so rejoining starts clean
        PlayerHitPlayer.clearCombat(playerId);
        PlayerHitPlayer.releaseOpponents(player);
    }

    /** Drops the vanilla "player died" line for a logout we have already announced ourselves. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSilencedDeath(PlayerDeathEvent event) {
        if (event.getPlayer().getUniqueId().equals(silencedDeath)) {
            event.deathMessage(null);
        }
    }

    /**
     * Tells the server someone was killed for logging out mid-fight, and who they ran from.
     * Returns whether anything was actually said.
     */
    private static boolean announce(CombatTag plugin, Player player, String opponent) {
        if (!plugin.getConfig().getBoolean("custom-kill-message-on-leave")) return false;
        if (opponent == null || plugin.messages().raw("combat-logged-out").isBlank()) return false;

        plugin.getServer().broadcast(CombatTag.message(player, "combat-logged-out",
                Map.of("player", player.getName(), "opponent", opponent)));
        return true;
    }

    private static boolean shouldKill(CombatTag plugin, PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("kill-on-quit")) return false;
        return !plugin.getConfig().getBoolean("kill-on-quit-only-if-at-fault") || isAtFault(event.getReason());
    }

    private static boolean isAtFault(PlayerQuitEvent.QuitReason reason) {
        return reason == PlayerQuitEvent.QuitReason.DISCONNECTED || reason == PlayerQuitEvent.QuitReason.TIMED_OUT;
    }
}
