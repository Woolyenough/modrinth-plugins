package wtf.wooly.crystaldamagemodifier;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import wtf.wooly.common.config.ConfigMigrator;
import wtf.wooly.common.message.Messages;
import wtf.wooly.crystaldamagemodifier.commands.CrystalDamage;
import wtf.wooly.crystaldamagemodifier.listeners.DamagedByCrystal;
import wtf.wooly.crystaldamagemodifier.listeners.DamagedByRespawnAnchor;

import java.util.List;

public final class CrystalDamageModifier extends JavaPlugin {
    public static final String END_CRYSTAL = "end-crystal";
    public static final String RESPAWN_ANCHOR = "respawn-anchor";
    /** The explosion types that carry a multiple of their own. */
    public static final List<String> TYPES = List.of(END_CRYSTAL, RESPAWN_ANCHOR);

    private static final double DEFAULT_DAMAGE_FACTOR = 1.0;
    /** The single factor older versions used for both types, folded into them on first load. */
    private static final String LEGACY_FACTOR = "damage-factor";

    private static CrystalDamageModifier instance;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;
        reloadFiles();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(CrystalDamage.node(this), "Inspect and change the explosion damage factor");
        });

        getServer().getPluginManager().registerEvents(new DamagedByCrystal(), this);
        getServer().getPluginManager().registerEvents(new DamagedByRespawnAnchor(), this);
    }

    @Override
    public void onDisable() {
        // Nothing :P
    }

    public static CrystalDamageModifier getPlugin() {
        return instance;
    }

    public Messages messages() {
        return messages;
    }

    public void reloadFiles() {
        // Fold the old single factor in first, before the migrator fills the types with defaults
        reloadConfig();
        migrateLegacyFactor();

        ConfigMigrator.migrate(this, "config.yml");
        reloadConfig();
        messages = Messages.load(this);
        validateDamageFactors();
    }

    /** Where a type's multiple lives in config.yml. */
    public static String path(String type) {
        return "damage-factors." + type;
    }

    /** The multiple to apply to one explosion type's damage. */
    public double damageFactor(String type) {
        return getConfig().getDouble(path(type), DEFAULT_DAMAGE_FACTOR);
    }

    /**
     * Earlier versions scaled both explosion types with a single 'damage-factor'. Copy whatever a
     * server had set into each type it hasn't already configured, so updating doesn't quietly put
     * their damage back to normal, then drop the old key.
     */
    private void migrateLegacyFactor() {
        if (!getConfig().contains(LEGACY_FACTOR)) return;

        double legacy = getConfig().getDouble(LEGACY_FACTOR, DEFAULT_DAMAGE_FACTOR);
        for (String type : TYPES) {
            // Negative covers both "absent" and the interim "-1 follows the global" marker
            if (getConfig().getDouble(path(type), -1) < 0) {
                getConfig().set(path(type), legacy);
            }
        }

        getConfig().setComments(LEGACY_FACTOR, null);
        getConfig().setInlineComments(LEGACY_FACTOR, null);
        getConfig().set(LEGACY_FACTOR, null);
        saveConfig();
        getLogger().info("Moved damage-factor '" + legacy + "' into the per-type damage-factors.");
    }

    public void validateDamageFactors() {
        boolean changed = false;

        for (String type : TYPES) {
            double factor = getConfig().getDouble(path(type), DEFAULT_DAMAGE_FACTOR);
            if (!Double.isFinite(factor) || factor < 0) {
                getLogger().warning("Invalid " + path(type) + " '" + factor + "' in config.yml. Must be a finite number that is zero or greater. Defaulting to " + DEFAULT_DAMAGE_FACTOR + ".");
                getConfig().set(path(type), DEFAULT_DAMAGE_FACTOR);
                changed = true;
            }
        }

        if (changed) {
            saveConfig();
        }
    }
}
