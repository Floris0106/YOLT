package floris0106.yolt.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import floris0106.yolt.config.Config;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.*;

public class ResetCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("reset")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
			.executes(ResetCommand::reset);
	}

	private static int reset(CommandContext<CommandSourceStack> context)
	{
        int lives = Config.getDefaultLives();
        float totalHealth = Config.getDefaultTotalHealth();
		for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers())
        {
            ServerPlayerExtension extension = (ServerPlayerExtension)player;
            if (player.gameMode.isSurvival())
            {
                extension.yolt$setLives(lives);
                extension.yolt$setTotalHealth(totalHealth);
            }
            else
                extension.yolt$setLives(-1);
        }

		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.reset.success"), true);
		return 1;
	}
}