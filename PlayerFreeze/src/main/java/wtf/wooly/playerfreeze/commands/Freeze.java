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

public final class Freeze {
    private Freeze() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(PlayerFreeze plugin) {
        return Commands.literal("freeze")
                .requires(source -> source.getSender().hasPermission(PlayerFreeze.permUse))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .getFirst();

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", target.getName());
                            placeholders.put("invoker", sender.getName());

                            if (target.hasPermission(PlayerFreeze.permImmune)) {
                                sender.sendMessage(PlayerFreeze.message("player-immune", placeholders));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (PlayerFreeze.frozenPlayers.contains(target.getUniqueId())) {
                                sender.sendMessage(PlayerFreeze.message("already-frozen", placeholders));
                                return Command.SINGLE_SUCCESS;
                            }

                            PlayerFreeze.freezePlayer(target);
                            target.sendMessage(PlayerFreeze.message("frozen", placeholders));
                            sender.sendMessage(PlayerFreeze.message("frozen-invoker", placeholders));
                            plugin.getServer().broadcast(PlayerFreeze.message("frozen-broadcast", placeholders), PlayerFreeze.permNotify);
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(PlayerFreeze.message("freeze-usage", Map.of()));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
