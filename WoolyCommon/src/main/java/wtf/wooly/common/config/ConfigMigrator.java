package wtf.wooly.common.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Merges bundled YAML defaults into a user's existing file without overwriting
 * their values. New nodes retain the comments from the bundled resource.
 */
public final class ConfigMigrator {
    private ConfigMigrator() {
    }

    public static FileConfiguration migrate(JavaPlugin plugin, String resourcePath) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(resourcePath, "resourcePath");

        File destination = new File(plugin.getDataFolder(), resourcePath);
        if (!destination.exists()) {
            plugin.saveResource(resourcePath, false);
            return YamlConfiguration.loadConfiguration(destination);
        }

        YamlConfiguration current = YamlConfiguration.loadConfiguration(destination);
        YamlConfiguration defaults = loadBundledConfiguration(plugin, resourcePath);
        boolean changed = merge(defaults, current);

        if (current.options().getHeader().isEmpty() && !defaults.options().getHeader().isEmpty()) {
            current.options().setHeader(defaults.options().getHeader());
            changed = true;
        }

        if (changed) {
            try {
                current.save(destination);
                plugin.getLogger().info("Migrated " + resourcePath + " with new default entries.");
            } catch (IOException exception) {
                throw new IllegalStateException("Could not save migrated " + resourcePath, exception);
            }
        }
        return current;
    }

    private static YamlConfiguration loadBundledConfiguration(JavaPlugin plugin, String resourcePath) {
        InputStream resource = plugin.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Bundled resource does not exist: " + resourcePath);
        }
        try (Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read bundled resource: " + resourcePath, exception);
        }
    }

    private static boolean merge(ConfigurationSection defaults, ConfigurationSection current) {
        boolean changed = false;
        for (String key : defaults.getKeys(false)) {
            Object defaultValue = defaults.get(key);
            ConfigurationSection defaultSection = defaults.getConfigurationSection(key);

            if (!current.contains(key)) {
                if (defaultSection == null) {
                    current.set(key, defaultValue);
                } else {
                    merge(defaultSection, current.createSection(key));
                }
                // setComments() silently does nothing until the path exists, so it has to come last
                copyComments(defaults, current, key);
                changed = true;
                continue;
            }

            changed |= restoreMissingComments(defaults, current, key);

            ConfigurationSection currentSection = current.getConfigurationSection(key);
            if (defaultSection != null && currentSection != null) {
                changed |= merge(defaultSection, currentSection);
            }
        }
        return changed;
    }

    /** Gives a key that arrived without its comments - from an older migrator - them back. */
    private static boolean restoreMissingComments(ConfigurationSection defaults, ConfigurationSection current, String key) {
        if (!current.getComments(key).isEmpty() || ownComments(defaults, key).isEmpty()) {
            return false;
        }
        copyComments(defaults, current, key);
        return true;
    }

    private static void copyComments(ConfigurationSection source, ConfigurationSection target, String key) {
        List<String> comments = ownComments(source, key);
        if (!comments.isEmpty()) {
            target.setComments(key, comments);
        }
        List<String> inlineComments = source.getInlineComments(key);
        if (!inlineComments.isEmpty()) {
            target.setInlineComments(key, inlineComments);
        }
    }

    /**
     * The comment block that belongs to a key: the lines directly above it, back as far as the
     * blank line separating it from the previous entry. Bukkit hands us every comment line since
     * that entry, so anything above that blank belongs to the entry before, not to this key. The
     * blank itself (a null) is kept, so the key stays separated from its neighbour.
     */
    private static List<String> ownComments(ConfigurationSection section, String key) {
        List<String> comments = section.getComments(key);
        return comments.subList(Math.max(comments.lastIndexOf(null), 0), comments.size());
    }
}
