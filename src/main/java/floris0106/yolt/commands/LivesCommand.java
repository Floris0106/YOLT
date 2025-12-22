package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;
import net.minecraft.server.permissions.Permissions;

public class LivesCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("lives")
			.then(argument("player", EntityArgument.player())
				.executes(LivesCommand::getLives)
				.then(argument("lives", IntegerArgumentType.integer(-1))
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					.executes(LivesCommand::setLives)
				)
			);
	}

	private static int getLives(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ServerPlayerExtension extension = (ServerPlayerExtension) player;
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.lives.query", player.getDisplayName(), extension.yolt$getLives()), false);
		return 1;
	}

	private static int setLives(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int lives = IntegerArgumentType.getInteger(context, "lives");
		((ServerPlayerExtension) player).yolt$setLives(lives);
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.lives.set", player.getDisplayName(), lives), false);
		return 1;
	}
}