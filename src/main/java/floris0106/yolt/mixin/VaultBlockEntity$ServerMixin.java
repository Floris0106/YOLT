package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultBlockEntity$ServerMixin
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity$Server;shouldCycleDisplayItem(JLnet/minecraft/world/level/block/entity/vault/VaultState;)Z"))
	private static boolean yolt$removeDisplayItem(long time, VaultState state, Operation<Boolean> original, @Local(argsOnly = true) BlockState blockState, @Local(argsOnly = true) VaultSharedData sharedData)
	{
		if (blockState.getValue(VaultBlock.OMINOUS))
		{
			sharedData.setDisplayItem(ItemStack.EMPTY);
			return false;
		}

		return original.call(time, state);
	}

	@Redirect(method = "isValidToInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	private static boolean yolt$skipCheckingComponents(ItemStack lhs, ItemStack rhs)
	{
		return ItemStack.isSameItem(lhs, rhs);
	}
}