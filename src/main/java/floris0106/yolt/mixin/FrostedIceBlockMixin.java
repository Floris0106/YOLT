package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import floris0106.yolt.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FrostedIceBlock.class)
public abstract class FrostedIceBlockMixin
{
    @Unique
    private static final int TICK_DELAY_MIN = 4;
    @Unique
    private static final int TICK_DELAY_MAX = TICK_DELAY_MIN * 2;

    @WrapMethod(method = "onPlace")
    private void yolt$startTickingSooner(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl, Operation<Void> original)
    {
        if (!Config.doesFrostedIceMeltFaster())
            original.call(blockState, level, blockPos, blockState2, bl);
        else if (fewerNeigboursThan(level, blockPos, 4))
            level.scheduleTick(blockPos, (FrostedIceBlock) (Object) this, Mth.nextInt(level.getRandom(), TICK_DELAY_MIN, TICK_DELAY_MAX));
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
    private int yolt$tickMoreOften(RandomSource random, int min, int max, Operation<Integer> original)
    {
        if (!Config.doesFrostedIceMeltFaster())
            return original.call(random, min, max);
        else
            return original.call(random, TICK_DELAY_MIN, TICK_DELAY_MAX);
    }

    @Shadow
    protected abstract boolean fewerNeigboursThan(BlockGetter blockGetter, BlockPos blockPos, int i);
}