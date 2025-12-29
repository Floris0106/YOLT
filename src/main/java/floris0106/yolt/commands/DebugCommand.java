package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.LEVEL_GAMEMASTERS;
import static net.minecraft.commands.Commands.hasPermission;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import floris0106.yolt.util.PresentTracker;

public class DebugCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("debug")
			.requires(hasPermission(LEVEL_GAMEMASTERS))
			.then(
				literal("fix_present_tracker").executes(DebugCommand::fixPresentTracker)
			);
	}

	private static int fixPresentTracker(CommandContext<CommandSourceStack> context)
	{
		ServerLevel level = context.getSource().getLevel();
		PresentTracker tracker = level.getDataStorage().computeIfAbsent(PresentTracker.DATA_TYPE);
		tracker.getPositions().removeIf(pos -> !level.getBlockState(pos).is(Blocks.VAULT));
		return 1;
	}
}