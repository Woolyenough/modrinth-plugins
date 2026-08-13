package wtf.wooly.combattag.listeners;

import org.bukkit.event.EventPriority;
import wtf.wooly.combattag.CombatTag;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHitPlayer implements Listener {
    public static final Map<UUID, Integer> playerTaskLog = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        CombatTag plugin = CombatTag.getPlugin();
        if (!plugin.getConfig().getBoolean("enabled")) return;

        if (e.getEntity().getType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
            Player attacker = (Player) e.getDamager();
            Player victim = (Player) e.getEntity();

            handleCombat(attacker, victim);
            handleCombat(victim, attacker);
        }
    }

    private void handleCombat(Player player, Player opponent) {
        UUID playerId = player.getUniqueId();
        CombatTag.combatOpponents.computeIfAbsent(playerId, id -> ConcurrentHashMap.newKeySet())
                .add(opponent.getUniqueId());
        CombatTag.lastOpponent.put(playerId, opponent.getName());

        if (CombatTag.playersInCombat.containsKey(playerId)) {
            renewTimer(player);
        } else {
            inCombat(player, opponent.getName());
        }
    }

    public void inCombat(Player player, String with) {
        CombatTag plugin = CombatTag.getPlugin();
        UUID playerId = player.getUniqueId();
        CombatTag.playersInCombat.put(playerId, System.currentTimeMillis());
        if (!plugin.messages().raw("combat-tagged").isBlank()) {
            player.sendMessage(CombatTag.message(player, "combat-tagged", Map.of("opponent", with)));
        }

        long combatDuration = plugin.getConfig().getLong("combat-duration") * 20L;
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> outCombat(player), combatDuration);
        playerTaskLog.put(playerId, task.getTaskId());
    }

    /** Wipes a player's combat state and cancels their pending expiry, without telling them. */
    public static void clearCombat(UUID playerId) {
        CombatTag plugin = CombatTag.getPlugin();

        Integer taskId = playerTaskLog.remove(playerId);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        CombatTag.playersInCombat.remove(playerId);
        CombatTag.combatOpponents.remove(playerId);
        CombatTag.lastOpponent.remove(playerId);
    }

    public static void outCombat(Player player) {
        CombatTag plugin = CombatTag.getPlugin();
        clearCombat(player.getUniqueId());

        if (!plugin.messages().raw("combat-expired").isBlank()) {
            player.sendMessage(CombatTag.message(player, "combat-expired", Map.of()));
        }

        player.sendActionBar(Component.empty());
    }

    public void renewTimer(Player player) {
        CombatTag plugin = CombatTag.getPlugin();
        UUID playerId = player.getUniqueId();
        CombatTag.playersInCombat.put(playerId, System.currentTimeMillis());

        Integer taskId = playerTaskLog.remove(playerId);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        long combatDuration = plugin.getConfig().getLong("combat-duration") * 20L;
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> outCombat(player), combatDuration);
        playerTaskLog.put(playerId, task.getTaskId());
    }

    /**
     * Drops a player who has left the fight - by quitting or dying - from everyone else's opponent
     * list, ending the combat of anyone they were the last opponent of. The departing player's own
     * state is the caller's job, via clearCombat().
     */
    public static void releaseOpponents(Player gone) {
        CombatTag plugin = CombatTag.getPlugin();
        UUID goneId = gone.getUniqueId();

        if (!plugin.getConfig().getBoolean("end-combat-when-opponent-gone")) return;

        for (Map.Entry<UUID, Set<UUID>> entry : CombatTag.combatOpponents.entrySet()) {
            if (!entry.getValue().remove(goneId) || !entry.getValue().isEmpty()) continue;

            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                outCombat(player);
            }
        }
    }
}
