package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;discard()V"))
	private void yolt$playLandingSound(CallbackInfo ci)
	{
		FallingBlockEntity fallingBlock = (FallingBlockEntity) (Object) this;
		if (fallingBlock.getBlockState().is(Blocks.VAULT))
			fallingBlock.playSound(SoundEvents.ANVIL_LAND);
	}
}