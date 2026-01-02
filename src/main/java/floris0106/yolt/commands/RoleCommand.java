package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.LEVEL_GAMEMASTERS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.hasPermission;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import floris0106.yolt.util.Language;
import floris0106.yolt.util.Role;
import floris0106.yolt.util.ServerPlayerExtension;

public class RoleCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("role")
			.requires(hasPermission(LEVEL_GAMEMASTERS))
			.then(argument("player", EntityArgument.player())
				.executes(RoleCommand::getRole)
				.then(EnumArgument.argument("role", Role.class)
					.executes(RoleCommand::setRole)
				)
			);
	}

	private static int getRole(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ServerPlayerExtension extension = (ServerPlayerExtension) player;
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.role.query", player.getDisplayName(), extension.yolt$getRole().toString()), false);
		return 1;
	}

	private static int setRole(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		Role role = EnumArgument.getEnum(context, "role", Role.class);
		ServerPlayerExtension extension = (ServerPlayerExtension) player;
		extension.yolt$setRole(role);
		context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.role.set", player.getDisplayName(), role.toString()), false);
		return 1;
	}
}