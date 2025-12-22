package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.block.entity.vault.VaultState$1")
public abstract class VaultState$1Mixin
{
	@WrapWithCondition(method = "onEnter", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
	private boolean yolt$openPresentSound(ServerLevel level, int type, BlockPos pos, int data)
	{
		return level.getBlockState(pos).is(Blocks.VAULT);
	}
}