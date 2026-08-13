package wtf.wooly.combattag.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import wtf.wooly.combattag.CombatTag;

public class PAPIExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "ct";
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

        if (params.equalsIgnoreCase("in_combat")) {
            return String.valueOf(CombatTag.playersInCombat.containsKey(offlinePlayer.getUniqueId()));
        }
        if (params.equalsIgnoreCase("time_left")) {
            CombatTag plugin = CombatTag.getPlugin();
            long startedTime = CombatTag.playersInCombat.getOrDefault(offlinePlayer.getUniqueId(), 0L);
            if (startedTime == 0L) return "0";
            long nowTime = System.currentTimeMillis();
            long timeLeft = startedTime - nowTime + (plugin.getConfig().getInt("combat-duration") * 1000L);
            int timeLeftInt = (int) timeLeft / 1000;
            if (timeLeftInt == plugin.getConfig().getInt("combat-duration"))
                timeLeftInt -= 1;
            return String.valueOf(timeLeftInt+1);
        }
        return "";
    }
}