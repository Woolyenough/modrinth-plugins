package wtf.wooly.playerfreeze.commands;

import wtf.wooly.playerfreeze.PlayerFreeze;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Unfreeze {
    private Unfreeze() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(PlayerFreeze plugin) {
        return Commands.literal("unfreeze")
                .requires(source -> source.getSender().hasPermission(PlayerFreeze.permUse))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .suggests((ctx, builder) -> {
                            PlayerFreeze.frozenPlayers.stream()
                                    .map(plugin.getServer()::getPlayer)
                                    .filter(Objects::nonNull)
                                    .map(Player::getName)
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .getFirst();

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", target.getName());
                            placeholders.put("invoker", sender.getName());

                            if (!PlayerFreeze.frozenPlayers.contains(target.getUniqueId())) {
                                sender.sendMessage(PlayerFreeze.message("not-frozen", placeholders));
                                return Command.SINGLE_SUCCESS;
                            }

                            PlayerFreeze.unfreezePlayer(target);
                            target.sendMessage(PlayerFreeze.message("unfrozen", placeholders));
                            sender.sendMessage(PlayerFreeze.message("unfrozen-invoker", placeholders));
                            plugin.getServer().broadcast(PlayerFreeze.message("unfrozen-broadcast", placeholders), PlayerFreeze.permNotify);
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(PlayerFreeze.message("unfreeze-usage", Map.of()));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
