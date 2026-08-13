package wtf.wooly.crystaldamagemodifier.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wtf.wooly.crystaldamagemodifier.CrystalDamageModifier;

public class DamagedByCrystal implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getDamager().getType() == EntityType.END_CRYSTAL) {
                CrystalDamageModifier plugin = CrystalDamageModifier.getPlugin();
                event.setDamage(event.getDamage() * plugin.damageFactor(CrystalDamageModifier.END_CRYSTAL));
            }
        }
    }
}
