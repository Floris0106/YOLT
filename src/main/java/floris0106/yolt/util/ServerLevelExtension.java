package floris0106.yolt.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface ServerLevelExtension
{
    void yolt$addPresentPosition(BlockPos pos);
    void yolt$removePresentPosition(BlockPos pos);
    double yolt$getPresentDistance(Vec3 pos);
}