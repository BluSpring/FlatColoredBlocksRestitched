package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedChiseledBlockEntity;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ILegacyStorageHandler;
import mod.flatcoloredblocks.mixin.chiselsandbits.ChiseledBlockEntityAccessor;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;

/*public final class LegacyChunkSectionBasedStorageHandler implements ILegacyStorageHandler {
    private ChiseledBlockEntity blockEntity;

    public LegacyChunkSectionBasedStorageHandler(ChiseledBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean matches(final @NotNull CompoundTag compoundTag)
    {
        return compoundTag.contains(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt) {
        var mutableStatistics = (MutableStatisticsWorkaround) blockEntity.getStatistics();
        var storage = ((ChiseledBlockEntityAccessor) blockEntity).getStorage();

        deserializeNBT(nbt, mutableStatistics, storage);
    }

    private void deserializeNBT(CompoundTag nbt, MutableStatisticsWorkaround mutableStatistics, IStateEntryStorage storage) {
        final CompoundTag chiselBlockData = nbt.getCompound(NbtConstants.CHISEL_BLOCK_ENTITY_DATA);
        final CompoundTag compressedSectionData = chiselBlockData.getCompound(NbtConstants.COMPRESSED_STORAGE);

        final LevelChunkSection chunkSection = new LevelChunkSection(0, BuiltinRegistries.BIOME);
        ChunkSectionUtils.deserializeNBT(
                chunkSection,
                compressedSectionData
        );

        storage.loadFromChunkSection(chunkSection);

        if (chiselBlockData.contains(NbtConstants.STATISTICS)) {
            final CompoundTag statisticsData = chiselBlockData.getCompound(NbtConstants.STATISTICS);
            mutableStatistics.deserializeNBT(statisticsData);
        } else {
            mutableStatistics.recalculate(storage, false);
        }
    }

    @Override
    public StoragePayload readPayloadOffThread(CompoundTag compoundTag) {
        var storage = new SimpleStateEntryStorage();
        var mutableStatistics = MutableStatisticsWorkaround.create(blockEntity::getLevel, blockEntity::getBlockPos);

        deserializeNBT(compoundTag, mutableStatistics, storage);

        return new StoragePayload(storage, mutableStatistics);
    }

    @Override
    public void syncPayloadOnGameThread(StoragePayload payload) {
        ((ExtendedChiseledBlockEntity) blockEntity).setMutableStatistics(payload.mutableStatistics());
        ((ChiseledBlockEntityAccessor) blockEntity).setStorage(payload.storage());

        if (!((ChiseledBlockEntityAccessor) blockEntity).isInitialized()) {
            blockEntity.setChanged();
        }

        ((ChiseledBlockEntityAccessor) blockEntity).setIsInitialized(true);
    }
}
*/