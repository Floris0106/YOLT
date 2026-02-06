package floris0106.yolt.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin
{
	@Inject(method = "tickHeadTurn", at = @At("TAIL"))
	private void yolt$removeArmorStand(float f, CallbackInfo ci)
	{
		ArmorStand armorStand = (ArmorStand) (Object) this;
		if (!armorStand.getTags().contains("yolt_remove_when_on_ground"))
			return;

		Level level = armorStand.level();
		BlockPos belowPos = BlockPos.containing(armorStand.position()).below();
		BlockState belowState = level.getBlockState(belowPos);
		if (belowState.is(BlockTags.LEAVES) || belowState.is(BlockTags.LOGS))
			level.destroyBlock(belowPos, true, armorStand);

		if (armorStand.onGround() || armorStand.isInWater())
			armorStand.remove(Entity.RemovalReason.DISCARDED);
	}
}