package wtf.wooly.custommotd.listeners;

import wtf.wooly.custommotd.CustomMOTD;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

import java.util.List;
import java.util.UUID;

public class ServerListPingListener implements Listener {
    private final CustomMOTD plugin;

    public ServerListPingListener(CustomMOTD plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        FileConfiguration config = plugin.getConfig();

        String line1 = config.getString("motd.line1", "");
        String line2 = config.getString("motd.line2", "");
        event.motd(MiniMessage.miniMessage().deserialize(line1 + "<reset>\n" + line2));

        CachedServerIcon icon = plugin.getIcon();
        if (icon != null) {
            event.setServerIcon(icon);
        }

        if (config.getBoolean("enable_hover_message")) {
            List<String> lines = config.getStringList("player_hover_message");
            List<PaperServerListPingEvent.ListedPlayerInfo> listedPlayers = event.getListedPlayers();
            listedPlayers.clear();
            for (String line : lines) {
                listedPlayers.add(new PaperServerListPingEvent.ListedPlayerInfo(line, UUID.randomUUID()));
            }
        }
    }
}
