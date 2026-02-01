package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;

public class RevealCommand
{
    public static ArgumentBuilder<CommandSourceStack, ?> register()
    {
        return literal("reveal").executes(RevealCommand::revealTargets);
    }

    private static int revealTargets(CommandContext<CommandSourceStack> context)
    {
        ServerPlayer player = Objects.requireNonNull(context.getSource().getPlayer());
        ServerPlayerExtension extension = (ServerPlayerExtension) player;
        extension.yolt$revealTargets();
        context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.reveal.success"), false);
        return 1;
    }
}