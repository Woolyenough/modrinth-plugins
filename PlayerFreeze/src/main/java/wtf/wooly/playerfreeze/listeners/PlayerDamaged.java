package wtf.wooly.playerfreeze.listeners;

import wtf.wooly.playerfreeze.PlayerFreeze;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Map;

public class PlayerDamaged implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        PlayerFreeze plugin = PlayerFreeze.getPlugin();
        String protLevel = plugin.getConfig().getString("protection_level", "invincible");
        if (!protLevel.equalsIgnoreCase("invincible")) return;
        if (!PlayerFreeze.frozenPlayers.contains(player.getUniqueId())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!PlayerFreeze.frozenPlayers.contains(player.getUniqueId())) return;

        PlayerFreeze plugin = PlayerFreeze.getPlugin();
        String protLevel = plugin.getConfig().getString("protection_level", "invincible");
        if (!List.of("pvp", "invincible").contains(protLevel)) return;

        e.setCancelled(true);

        damager.sendMessage(PlayerFreeze.message("cannot-damage", Map.of("player", player.getName())));
    }
}
