package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.entity.vault.VaultState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VaultState.class)
public abstract class VaultStateMixin
{
	@WrapOperation(method = "tickAndGetNext", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/block/entity/vault/VaultState;updateStateForConnectedPlayers(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/vault/VaultConfig;Lnet/minecraft/world/level/block/entity/vault/VaultServerData;Lnet/minecraft/world/level/block/entity/vault/VaultSharedData;D)Lnet/minecraft/world/level/block/entity/vault/VaultState;"))
	private VaultState yolt$stayActive(ServerLevel level, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, double range, Operation<VaultState> original)
	{
		if (level.getBlockState(pos).getValue(VaultBlock.OMINOUS))
			return VaultState.ACTIVE;

		return original.call(level, pos, config, serverData, sharedData, range);
	}

	@WrapOperation(method = "tickAndGetNext", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/level/block/entity/vault/VaultState;updateStateForConnectedPlayers(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/vault/VaultConfig;Lnet/minecraft/world/level/block/entity/vault/VaultServerData;Lnet/minecraft/world/level/block/entity/vault/VaultSharedData;D)Lnet/minecraft/world/level/block/entity/vault/VaultState;"))
	private VaultState yolt$deactivate(ServerLevel level, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, double range, Operation<VaultState> original)
	{
		if (level.getBlockState(pos).getValue(VaultBlock.OMINOUS))
			return VaultState.INACTIVE;

		return original.call(level, pos, config, serverData, sharedData, range);
	}

	@WrapOperation(method = "ejectResultItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	private void yolt$changeEjectSound(ServerLevel level, Entity entity, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<Void> original)
	{
		if (level.getBlockState(pos).getValue(VaultBlock.OMINOUS))
			soundEvent = SoundEvents.CRAFTER_CRAFT;

		original.call(level, entity, pos, soundEvent, soundSource, volume, pitch);
	}
}