package floris0106.yolt.mixin;

import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SculkShriekerBlockEntity.class)
public class SculkShriekerBlockEntityMixin
{
    @Redirect(method = "canRespond", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable<Boolean> yolt$alwaysAllowWardenSpawning(BlockState instance, Property<Boolean> property)
    {
        return true;
    }
}