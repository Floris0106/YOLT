package floris0106.yolt.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class PresentTracker extends SavedData
{
    private static final String ID = "yolt_present_tracker";
    private static final Codec<PresentTracker> CODEC = BlockPos.CODEC.listOf().xmap(PresentTracker::new, PresentTracker::getPositionList);

    @SuppressWarnings("DataFlowIssue")
    public static final SavedDataType<PresentTracker> DATA_TYPE = new SavedDataType<>(ID, PresentTracker::new, CODEC, null);

    private final Set<BlockPos> positions = new ObjectArraySet<>();

    private PresentTracker() {}
    private PresentTracker(List<BlockPos> positions)
    {
        this.positions.addAll(positions);
    }

    public Set<BlockPos> getPositions()
    {
        return positions;
    }
    private List<BlockPos> getPositionList()
    {
        return Lists.newArrayList(positions);
    }
}