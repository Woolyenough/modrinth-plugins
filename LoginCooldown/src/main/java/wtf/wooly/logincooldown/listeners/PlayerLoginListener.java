package wtf.wooly.logincooldown.listeners;

import wtf.wooly.logincooldown.LoginCooldown;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static wtf.wooly.logincooldown.LoginCooldown.deserialize;

public class PlayerLoginListener implements Listener {
    public static final String NOTIFY_PERMISSION = "logincooldown.notify";
    public static final String BYPASS_PERMISSION = "logincooldown.bypass";

    private final LoginCooldown plugin;
    private final Map<String, Queue<Long>> loginTimestamps = new ConcurrentHashMap<>();

    public PlayerLoginListener(LoginCooldown plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(BYPASS_PERMISSION)) {
            return;
        }

        String playerName = event.getPlayer().getName();
        int joinsPer = plugin.setting(LoginCooldown.JOINS_PER);
        long timeFrameMillis = plugin.setting(LoginCooldown.TIME_FRAME) * 1000L;
        long now = System.currentTimeMillis();

        Queue<Long> timestamps = loginTimestamps.computeIfAbsent(playerName, k -> new ConcurrentLinkedQueue<>());

        // Remove expired timestamps
        timestamps.removeIf(ts -> now - ts > timeFrameMillis);

        if (timestamps.size() >= joinsPer) {
            Long earliest = timestamps.peek();
            if (earliest != null) {
                long remaining = timeFrameMillis - (now - earliest);
                String minutes = String.valueOf((remaining + 59999) / 60000);

                String blockedMsg = plugin.getConfig().getString("blocked-msg", "");
                String modMsg = plugin.getConfig().getString("mod-msg", "");

                event.disallow(
                        PlayerLoginEvent.Result.KICK_OTHER,
                        deserialize(blockedMsg.replace("[mins]", minutes))
                );
                plugin.getServer().broadcast(
                        deserialize(modMsg.replace("[username]", playerName)),
                        NOTIFY_PERMISSION
                );
            }
        } else {
            timestamps.add(now);
        }
    }
}
