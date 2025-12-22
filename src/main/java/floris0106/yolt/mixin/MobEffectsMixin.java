package floris0106.yolt.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.AbsorptionMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffects.class)
public abstract class MobEffectsMixin
{
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/AbsorptionMobEffect;addAttributeModifier(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/Identifier;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)Lnet/minecraft/world/effect/MobEffect;"))
	private static MobEffect yolt$removeMaxAbsorptionModifier(AbsorptionMobEffect effect, Holder<Attribute> attribute, Identifier identifier, double value, AttributeModifier.Operation operation)
	{
		return effect;
	}
}