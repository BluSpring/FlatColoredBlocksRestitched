package mod.flatcoloredblocks.mixin.chiselsandbits;

import com.google.common.collect.Table;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(targets = "mod.chiselsandbits.block.entities.ChiseledBlockEntity$MutableStatistics")
public interface MutableStatisticsAccessor {
    @Invoker
    void callRecalculate(IStateEntryStorage source, boolean mayUpdateWorld);

    @Accessor
    Supplier<LevelAccessor> getWorldReaderSupplier();

    @Mutable
    @Accessor
    void setWorldReaderSupplier(Supplier<LevelAccessor> worldReaderSupplier);

    @Accessor
    Supplier<BlockPos> getPositionSupplier();

    @Mutable
    @Accessor
    void setPositionSupplier(Supplier<BlockPos> positionSupplier);

    @Accessor
    Map<IBlockInformation, Integer> getCountMap();

    @Mutable
    @Accessor
    void setCountMap(Map<IBlockInformation, Integer> countMap);

    @Accessor
    Map<CollisionType, BitSet> getCollisionData();

    @Mutable
    @Accessor
    void setCollisionData(Map<CollisionType, BitSet> collisionData);

    @Accessor
    IBlockInformation getPrimaryState();

    @Accessor
    void setPrimaryState(IBlockInformation primaryState);

    @Accessor
    int getTotalUsedBlockCount();

    @Accessor
    void setTotalUsedBlockCount(int totalUsedBlockCount);

    @Accessor
    int getTotalUsedChecksWeakPowerCount();

    @Accessor
    void setTotalUsedChecksWeakPowerCount(int totalUsedChecksWeakPowerCount);

    @Accessor
    int getTotalLightLevel();

    @Accessor
    void setTotalLightLevel(int totalLightLevel);

    @Accessor
    int getTotalLightBlockLevel();

    @Accessor
    void setTotalLightBlockLevel(int totalLightBlockLevel);

    @Accessor
    boolean isRequiresRecalculation();

    @Accessor
    void setRequiresRecalculation(boolean requiresRecalculation);

    @Invoker
    void callRecalculate(IStateEntryStorage source);
}
