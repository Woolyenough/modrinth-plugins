package wtf.wooly.crystaldamagemodifier.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import wtf.wooly.crystaldamagemodifier.CrystalDamageModifier;

public class DamagedByRespawnAnchor implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getDamager() != null && event.getDamager().getType() == Material.RESPAWN_ANCHOR) {
                CrystalDamageModifier plugin = CrystalDamageModifier.getPlugin();
                event.setDamage(event.getDamage() * plugin.damageFactor(CrystalDamageModifier.RESPAWN_ANCHOR));
            }
        }
    }
}
