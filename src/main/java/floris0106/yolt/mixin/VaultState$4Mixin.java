package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import floris0106.yolt.util.ServerLevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.block.entity.vault.VaultState$4")
public abstract class VaultState$4Mixin
{
	@WrapOperation(method = "onEnter", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;)V"))
	private void yolt$openPresentSound(ServerLevel level, Entity entity, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, Operation<Void> original)
	{
		if (level.getBlockState(pos).getValue(VaultBlock.OMINOUS))
			soundEvent = SoundEvents.LEAF_LITTER_FALL;

		original.call(level, entity, pos, soundEvent, soundSource);
	}

	@WrapOperation(method = "onExit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;)V"))
	private void yolt$disappearPresent(ServerLevel level, Entity entity, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, Operation<Void> original)
	{
		if (level.getBlockState(pos).getValue(VaultBlock.OMINOUS))
		{
			soundEvent = SoundEvents.WIND_CHARGE_BURST.value();

			level.removeBlockEntity(pos);
			level.removeBlock(pos, false);

			((ServerLevelExtension) level).yolt$removePresentPosition(pos);

			Vec3 particlePos = Vec3.atCenterOf(pos);
			level.sendParticles(ParticleTypes.GUST, particlePos.x, particlePos.y, particlePos.z, 0, 0.0, 0.0, 0.0, 0.0);
		}

		original.call(level, entity, pos, soundEvent, soundSource);
	}
}