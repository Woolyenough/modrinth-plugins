package wtf.wooly.flyspeed.commands;

import wtf.wooly.flyspeed.FlySpeed;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wtf.wooly.common.message.Messages;

import java.util.Map;

public final class FlySpeedCommand {
    private static final String USE_PERMISSION = "fs.use";
    private static final String OTHERS_PERMISSION = "fs.others";

    private FlySpeedCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(FlySpeed plugin) {
        return Commands.literal("fly-speed")
                .requires(source -> source.getSender().hasPermission(USE_PERMISSION))
                .then(Commands.argument("speed", FloatArgumentType.floatArg(-10f, 10f))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                send(plugin, ctx.getSource().getSender(), "console-needs-player");
                                return Command.SINGLE_SUCCESS;
                            }
                            return apply(plugin, ctx, player);
                        })
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .requires(source -> source.getSender().hasPermission(OTHERS_PERMISSION))
                                .executes(ctx -> apply(plugin, ctx, resolveTarget(ctx)))))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    String playerArg = sender.hasPermission(OTHERS_PERMISSION) ? " [player]" : "";
                    sender.sendMessage(message(plugin, "usage", Map.of("player-argument", playerArg)));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private static Player resolveTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(ctx.getSource())
                .getFirst();
    }

    private static int apply(FlySpeed plugin, CommandContext<CommandSourceStack> ctx, Player target) {
        float speed = FloatArgumentType.getFloat(ctx, "speed");

        FlySpeed.trackChange(target);
        target.setFlySpeed(speed / 10f);

        CommandSender sender = ctx.getSource().getSender();
        String name = target.equals(sender) ? "Your" : target.getName() + "'s";
        sender.sendMessage(message(plugin, "speed-set", Map.of("target", name, "speed", String.valueOf(speed))));
        return Command.SINGLE_SUCCESS;
    }

    private static void send(FlySpeed plugin, CommandSender sender, String key) {
        Messages messages = plugin.messages();
        sender.sendMessage(messages.get("prefix").append(messages.get(key)));
    }

    private static Component message(FlySpeed plugin, String key, Map<String, String> placeholders) {
        Messages messages = plugin.messages();
        return messages.get("prefix").append(messages.get(key, placeholders));
    }
}
