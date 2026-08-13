package wtf.wooly.playerfreeze.commands;

import wtf.wooly.playerfreeze.PlayerFreeze;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class PF {
    private PF() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(PlayerFreeze plugin) {
        return Commands.literal("player-freeze")
                .requires(source -> source.getSender().hasPermission(PlayerFreeze.permAdmin))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            plugin.reloadFiles();
                            PlayerFreeze.validateProtLevel();
                            ctx.getSource().getSender().sendMessage(plugin.messages().get("config-reloaded"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}
