package wtf.wooly.logincooldown.commands;

import wtf.wooly.logincooldown.LoginCooldown;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static wtf.wooly.logincooldown.LoginCooldown.deserialize;

public final class LoginCooldownCommand {
    public static final String ADMIN_PERMISSION = "logincooldown.admin";
    private static final String PREFIX = "<grey>[<#ff6a6a>LoginCooldown<grey>] ";

    private LoginCooldownCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(LoginCooldown plugin) {
        return Commands.literal("login-cooldown")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            plugin.reloadFiles();
                            ctx.getSource().getSender().sendMessage(deserialize(PREFIX + "<green>Configuration reloaded."));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(setNode(plugin))
                .executes(ctx -> {
                    show(plugin, ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setNode(LoginCooldown plugin) {
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");

        for (String setting : LoginCooldown.SETTINGS) {
            set.then(Commands.literal(setting)
                    .then(Commands.argument("value", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                int value = IntegerArgumentType.getInteger(ctx, "value");
                                plugin.getConfig().set(setting, value);
                                plugin.saveConfig();
                                ctx.getSource().getSender().sendMessage(deserialize(PREFIX + "<green>Set <white>" + setting + " <green>to <white>" + value + "<green>."));
                                return Command.SINGLE_SUCCESS;
                            })));
        }
        return set;
    }

    private static void show(LoginCooldown plugin, CommandSender sender) {
        sender.sendMessage(deserialize(PREFIX + "<white>Current settings:"));

        for (String setting : LoginCooldown.SETTINGS) {
            sender.sendMessage(deserialize("  <#ffcfb7>" + setting + " <grey>- <white>" + plugin.setting(setting)));
        }
    }
}
