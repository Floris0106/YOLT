package floris0106.yolt.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class HealthCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("health")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
			.then(argument("player", EntityArgument.player())
				.executes(HealthCommand::getTotalHealth)
				.then(argument("health", DoubleArgumentType.doubleArg(-1.0))
					.executes(HealthCommand::setTotalHealth)
				)
			);
	}

	private static int getTotalHealth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ServerPlayerExtension extension = (ServerPlayerExtension) player;
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.health.query", player.getDisplayName(), extension.yolt$getTotalHealth()), false);
		return 1;
	}

	private static int setTotalHealth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		float totalHealth = (float) DoubleArgumentType.getDouble(context, "health");
		((ServerPlayerExtension) player).yolt$setTotalHealth(totalHealth);
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.health.set", player.getDisplayName(), totalHealth), false);
		return 1;
	}
}