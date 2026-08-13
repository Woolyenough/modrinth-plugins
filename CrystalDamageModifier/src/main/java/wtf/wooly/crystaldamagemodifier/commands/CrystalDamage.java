package wtf.wooly.crystaldamagemodifier.commands;

import wtf.wooly.crystaldamagemodifier.CrystalDamageModifier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CrystalDamage {
    public static final String ADMIN_PERMISSION = "crystaldamage.admin";

    private CrystalDamage() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(CrystalDamageModifier plugin) {
        return Commands.literal("crystaldamage")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            plugin.reloadFiles();
                            ctx.getSource().getSender().sendMessage(plugin.messages().get("config-reloaded"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(setNode(plugin))
                .executes(ctx -> {
                    show(plugin, ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    /** {@code set <type> <factor>}, one branch per explosion type. */
    private static LiteralArgumentBuilder<CommandSourceStack> setNode(CrystalDamageModifier plugin) {
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");

        for (String type : CrystalDamageModifier.TYPES) {
            set.then(Commands.literal(type)
                    .then(Commands.argument("factor", DoubleArgumentType.doubleArg(0))
                            .executes(ctx -> {
                                double factor = DoubleArgumentType.getDouble(ctx, "factor");
                                plugin.getConfig().set(CrystalDamageModifier.path(type), factor);
                                plugin.saveConfig();
                                ctx.getSource().getSender().sendMessage(plugin.messages().get("damage-factor-set",
                                        Map.of("type", type, "factor", String.valueOf(factor))));
                                return Command.SINGLE_SUCCESS;
                            })));
        }
        return set;
    }

    private static void show(CrystalDamageModifier plugin, CommandSender sender) {
        sender.sendMessage(plugin.messages().get("damage-factors-header"));

        for (String type : CrystalDamageModifier.TYPES) {
            sender.sendMessage(plugin.messages().get("damage-factor-entry",
                    Map.of("type", type, "factor", String.valueOf(plugin.damageFactor(type)))));
        }
    }
}
