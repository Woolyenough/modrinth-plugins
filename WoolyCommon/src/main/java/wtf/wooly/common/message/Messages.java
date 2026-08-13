package wtf.wooly.common.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import wtf.wooly.common.config.ConfigMigrator;
import wtf.wooly.common.config.MessageKeyMigrator;

import java.util.Map;
import java.util.Objects;

public final class Messages {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final FileConfiguration configuration;

    private Messages(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public static Messages load(JavaPlugin plugin) {
        FileConfiguration configuration = ConfigMigrator.migrate(plugin, "messages.yml");
        MessageKeyMigrator.migrate(plugin, configuration);
        return new Messages(configuration);
    }

    public String raw(String key) {
        return Objects.requireNonNullElse(configuration.getString(key), "");
    }

    public Component get(String key) {
        return MINI_MESSAGE.deserialize(raw(key));
    }

    public Component get(String key, Map<String, String> placeholders) {
        return render(raw(key), placeholders);
    }

    /* Deserialises a message and fills in its {@code [name]} placeholders. */
    public static Component render(String message, Map<String, String> placeholders) {
        String filled = message;
        for (String name : placeholders.keySet()) {
            filled = filled.replace("[" + name + "]", "<" + name + ">");
        }
        TagResolver[] resolvers = placeholders.entrySet().stream()
                .map(entry -> Placeholder.unparsed(entry.getKey(), entry.getValue()))
                .toArray(TagResolver[]::new);
        return MINI_MESSAGE.deserialize(filled, resolvers);
    }

    public Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }
}
