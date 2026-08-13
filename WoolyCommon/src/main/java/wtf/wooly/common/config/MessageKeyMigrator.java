package wtf.wooly.common.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Moves message keys that used to live in config.yml over to messages.yml.
 *
 * <p>Earlier releases kept messages in config.yml, so a server updating from one of those
 * still has its customised strings sitting in that file. For every key the bundled
 * messages.yml defines we look for a matching entry in the user's config.yml — either at
 * the same path or under a legacy {@code messages:} section — copy its value across and
 * delete it from config.yml. Once the key is gone from config.yml there is nothing left to
 * find, so this runs itself out after the first load.</p>
 */
public final class MessageKeyMigrator {
    private static final String CONFIG_RESOURCE = "config.yml";
    private static final String MESSAGES_RESOURCE = "messages.yml";
    private static final String LEGACY_SECTION = "messages";

    private MessageKeyMigrator() {
    }

    /**
     * Pulls any leftover message keys out of the plugin's config.yml and into {@code messages}.
     * Both files are saved when something moved; the plugin's in-memory config is updated too.
     *
     * @param messages the already-migrated messages.yml, mutated in place
     * @return true when at least one key was moved
     */
    public static boolean migrate(JavaPlugin plugin, FileConfiguration messages) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(messages, "messages");

        File configFile = new File(plugin.getDataFolder(), CONFIG_RESOURCE);
        if (!configFile.exists()) {
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        List<String> moved = new ArrayList<>();

        for (String key : messages.getKeys(true)) {
            if (messages.isConfigurationSection(key)) {
                continue;
            }
            String legacyPath = findLegacyPath(config, key);
            if (legacyPath == null) {
                continue;
            }
            messages.set(key, config.get(legacyPath));
            remove(config, legacyPath);
            moved.add(legacyPath);
        }

        if (moved.isEmpty()) {
            return false;
        }

        pruneLegacySection(config);
        plugin.saveConfig();
        save(messages, new File(plugin.getDataFolder(), MESSAGES_RESOURCE));
        plugin.getLogger().info("Moved " + moved.size() + " message key(s) from config.yml to messages.yml: "
                + String.join(", ", moved));
        deleteConfigIfObsolete(plugin, config, configFile);
        return true;
    }

    /**
     * Finds where a message key is hiding in config.yml, or null when it isn't there.
     * A value that isn't message-shaped belongs to a config option that happens to share
     * the name, so it is left alone.
     */
    private static String findLegacyPath(FileConfiguration config, String key) {
        for (String candidate : List.of(key, LEGACY_SECTION + "." + key)) {
            if (config.contains(candidate, true) && isMessageValue(config.get(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isMessageValue(Object value) {
        if (value instanceof String) {
            return true;
        }
        return value instanceof List<?> list && !list.isEmpty() && list.stream().allMatch(String.class::isInstance);
    }

    private static void remove(ConfigurationSection section, String path) {
        section.setComments(path, null);
        section.setInlineComments(path, null);
        section.set(path, null);
    }

    private static void pruneLegacySection(FileConfiguration config) {
        ConfigurationSection legacy = config.getConfigurationSection(LEGACY_SECTION);
        if (legacy != null && legacy.getKeys(false).isEmpty()) {
            remove(config, LEGACY_SECTION);
        }
    }

    /** A plugin that bundles no config.yml has no settings of its own, so an emptied file is just noise. */
    private static void deleteConfigIfObsolete(JavaPlugin plugin, FileConfiguration config, File configFile) {
        if (plugin.getResource(CONFIG_RESOURCE) != null || !config.getKeys(true).isEmpty()) {
            return;
        }
        if (configFile.delete()) {
            plugin.getLogger().info("Removed the now-empty config.yml; this plugin only uses messages.yml.");
        }
    }

    private static void save(FileConfiguration messages, File destination) {
        try {
            messages.save(destination);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save migrated " + MESSAGES_RESOURCE, exception);
        }
    }
}
