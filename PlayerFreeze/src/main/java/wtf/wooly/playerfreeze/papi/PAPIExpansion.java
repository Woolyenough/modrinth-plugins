package wtf.wooly.playerfreeze.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import wtf.wooly.playerfreeze.PlayerFreeze;

public class PAPIExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "pf";
    }
    @Override
    public @NotNull String getAuthor() {
        return "Woolyenough";
    }
    @Override
    public @NotNull String getVersion() {
        return "1";
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {

        if (params.equalsIgnoreCase("frozen")) {
            return String.valueOf(PlayerFreeze.frozenPlayers.contains(offlinePlayer.getUniqueId()));
        }
        return "";
    }
}
