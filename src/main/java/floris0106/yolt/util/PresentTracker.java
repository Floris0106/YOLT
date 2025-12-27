package floris0106.yolt.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public class PresentTracker extends SavedData
{
    private static final String ID = "yolt_present_tracker";
    private static final Codec<PresentTracker> CODEC = BlockPos.CODEC.listOf().xmap(PresentTracker::new, PresentTracker::getPositions);

    @SuppressWarnings("DataFlowIssue")
    public static final SavedDataType<PresentTracker> DATA_TYPE = new SavedDataType<>(ID, PresentTracker::new, CODEC, null);

    private final List<BlockPos> positions = new ArrayList<>();

    private PresentTracker() {}
    private PresentTracker(List<BlockPos> positions)
    {
        this.positions.addAll(positions);
    }

    public List<BlockPos> getPositions()
    {
        return positions;
    }
}