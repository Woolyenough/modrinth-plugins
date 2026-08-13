package wtf.wooly.playerfreeze.listeners;

import wtf.wooly.playerfreeze.PlayerFreeze;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMove implements Listener {
    private static final long MESSAGE_COOLDOWN_MS = 3000;
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!PlayerFreeze.frozenPlayers.contains(player.getUniqueId())) return;
        if (!e.hasExplicitlyChangedPosition()) return;

        e.setTo(e.getFrom().setDirection(e.getTo().getDirection()));

        if (shouldSendMessage(player.getUniqueId())) {
            player.sendMessage(PlayerFreeze.message("currently-frozen", Map.of()));
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();
        if (!PlayerFreeze.frozenPlayers.contains(player.getUniqueId())) return;
        if (!e.isFlying()) return;

        e.setCancelled(true);

        if (shouldSendMessage(player.getUniqueId())) {
            player.sendMessage(PlayerFreeze.message("currently-frozen", Map.of()));
        }
    }

    private boolean shouldSendMessage(UUID playerId) {
        long now = System.currentTimeMillis();
        Long last = lastMessageTime.get(playerId);
        if (last != null && now - last < MESSAGE_COOLDOWN_MS) return false;
        lastMessageTime.put(playerId, now);
        return true;
    }
}
