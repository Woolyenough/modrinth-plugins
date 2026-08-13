package wtf.wooly.flyspeed.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import wtf.wooly.flyspeed.FlySpeed;

public final class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FlySpeed.restore(event.getPlayer());
    }
}
