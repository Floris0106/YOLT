package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import floris0106.yolt.util.Events;
import floris0106.yolt.util.ServerLevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z"))
	private boolean yolt$alwaysPlaceVault(BlockState state, BlockPlaceContext context, Operation<Boolean> original)
	{
		if (((FallingBlockEntity) (Object) this).getBlockState().is(Blocks.VAULT))
			return true;

		return original.call(state, context);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private boolean yolt$onPresentPlaced(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original)
	{
		if (!original.call(level, pos, state, flags))
			return false;


		if (state.is(Blocks.VAULT))
		{
			((FallingBlockEntity) (Object) this).playSound(SoundEvents.ANVIL_LAND);
			((ServerLevelExtension) level).yolt$addPresentPosition(pos);
		}
		else if (state.is(Blocks.SCULK_CATALYST))
		{
			((FallingBlockEntity) (Object) this).playSound(SoundEvents.ANVIL_LAND);

			for (int i = 1; i <= 3; i++)
				Events.schedule(Objects.requireNonNull(level.getServer()).getTickCount() + i * 200, () -> yolt$spreadSculk(level, pos));
		}

		return true;
	}

	@Unique
	private void yolt$spreadSculk(Level level, BlockPos pos)
	{
		if (level.getBlockEntity(pos) instanceof SculkCatalystBlockEntity catalyst)
		{
			SculkSpreader spreader = catalyst.getListener().getSculkSpreader();
			while (spreader.getCursors().size() < SculkSpreader.MAX_CURSORS)
				spreader.addCursor(new SculkSpreader.ChargeCursor(pos.offset(level.random.nextInt(-1, 2), level.random.nextInt(-1, 2), level.random.nextInt(-1, 2)), 75));
		}
	}
}