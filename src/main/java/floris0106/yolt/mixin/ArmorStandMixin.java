package floris0106.yolt.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
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
		if (armorStand.getTags().contains("yolt_remove_when_on_ground") && (armorStand.onGround() || armorStand.isInWater()))
			armorStand.remove(Entity.RemovalReason.DISCARDED);
	}
}