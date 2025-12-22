package floris0106.yolt.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.AbsorptionMobEffect;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbsorptionMobEffect.class)
public abstract class AbsorptionMobEffectMixin
{
	@Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
	private void yolt$removeEffect(ServerLevel level, LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(false);
	}

	@Redirect(method = "onEffectStarted", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
	private float yolt$addAbsorption(float lhs, float rhs)
	{
		return lhs + rhs;
	}
}