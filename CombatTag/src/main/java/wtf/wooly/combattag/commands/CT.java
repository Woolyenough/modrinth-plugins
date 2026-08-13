package wtf.wooly.combattag.commands;

import wtf.wooly.combattag.CombatTag;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class CT {
    private CT() {
    }

    public static LiteralCommandNode<CommandSourceStack> node(CombatTag plugin) {
        return Commands.literal("combat-tag")
                .requires(source -> source.getSender().hasPermission(CombatTag.perms))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            plugin.reloadAll();
                            ctx.getSource().getSender().sendMessage(plugin.messages().get("config-reloaded"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}
