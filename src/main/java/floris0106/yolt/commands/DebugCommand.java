package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.LEVEL_GAMEMASTERS;
import static net.minecraft.commands.Commands.hasPermission;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;

import java.util.Set;

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
		Set<BlockPos> positions = level.getDataStorage().computeIfAbsent(PresentTracker.DATA_TYPE).getPositions();
		positions.removeIf(pos -> !level.getBlockState(pos).is(Blocks.VAULT));

		ChunkSource chunkSource = level.getChunkSource();
		SectionPos center = SectionPos.of(context.getSource().getPosition());
		for (int x = -64; x < 64; x++)
			for (int z = -64; z < 64; z++)
			{
				ChunkAccess chunk = chunkSource.getChunkNow(x + center.x(), z + center.z());
				if (chunk != null)
					chunk.findBlocks(
						state -> state.is(Blocks.VAULT) && state.getValue(VaultBlock.OMINOUS),
						(pos, state) -> positions.add(pos)
					);
			}

		return 1;
	}
}