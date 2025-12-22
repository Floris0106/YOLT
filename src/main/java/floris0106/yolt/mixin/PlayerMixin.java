package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import floris0106.yolt.config.Config;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;

@Mixin(Player.class)
public abstract class PlayerMixin
{
	@WrapOperation(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/PlayerTeam;formatNameForTeam(Lnet/minecraft/world/scores/Team;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"))
	private MutableComponent yolt$overrideNameColor(Team team, Component component, Operation<MutableComponent> original)
	{
		if (this instanceof ServerPlayerExtension extension)
			return component.copy().withStyle(Config.getColorByLives(extension.yolt$getLives()));
		return original.call(team, component);
	}

	@Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
	private void yolt$preventSleepBeforeMidnight(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir)
	{
		if (((Player) (Object) this).level().getDayTime() % 24000 != 18000)
			cir.setReturnValue(Either.left(new Player.BedSleepingProblem(Language.translatable("block.minecraft.bed.not_tired"))));
	}
}