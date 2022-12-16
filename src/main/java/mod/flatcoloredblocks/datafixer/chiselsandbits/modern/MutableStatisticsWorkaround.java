package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public interface MutableStatisticsWorkaround extends IMultiStateObjectStatistics, INBTSerializable<CompoundTag>, IPacketBufferSerializable {
    void recalculate(IStateEntryStorage source, boolean mayUpdateWorld);

    static MutableStatisticsWorkaround create(Supplier<LevelAccessor> worldReaderSupplier, Supplier<BlockPos> positionSupplier) {
        try {
            var mutableStatisticsClass = Class.forName("mod.chiselsandbits.block.entities.ChiseledBlockEntity$MutableStatistics");
            var mutableStatisticsConstructor = mutableStatisticsClass.getDeclaredConstructors()[0];
            mutableStatisticsConstructor.setAccessible(true);
            return (MutableStatisticsWorkaround) mutableStatisticsConstructor.newInstance(worldReaderSupplier, positionSupplier);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
