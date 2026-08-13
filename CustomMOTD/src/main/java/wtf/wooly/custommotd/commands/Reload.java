package wtf.wooly.custommotd.commands;

import wtf.wooly.custommotd.CustomMOTD;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class Reload {
    private Reload() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(CustomMOTD plugin) {
        return Commands.literal("motd-reload")
                .requires(source -> source.getSender().hasPermission(CustomMOTD.ADMIN_PERMISSION))
                .executes(ctx -> {
                    plugin.reloadAll();
                    ctx.getSource().getSender().sendMessage(plugin.messages().get("config-reloaded"));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
