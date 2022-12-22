package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import io.netty.buffer.Unpooled;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.BlockStateSerializationUtils;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.chiselsandbits.CB2BCConverter;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedChiseledBlockEntity;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ILegacyStorageHandler;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.MutableStatisticsWorkaround;
import mod.flatcoloredblocks.mixin.chiselsandbits.ChiseledBlockEntityAccessor;
import mod.flatcoloredblocks.mixin.chiselsandbits.MutableStatisticsAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.datafix.fixes.BlockStateData;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.zip.InflaterInputStream;

public class LegacyBitStreamBasedStorageHandler implements ILegacyStorageHandler {
    private final ChiseledBlockEntity blockEntity;

    public LegacyBitStreamBasedStorageHandler(ChiseledBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean matches(@NotNull CompoundTag compoundTag) {
        return compoundTag.contains("X");
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        var mutableStatistics = (MutableStatisticsWorkaround) blockEntity.getStatistics();
        var storage = ((ChiseledBlockEntityAccessor) blockEntity).getStorage();

        deserializeNBT(compoundTag, mutableStatistics, storage);
    }

    private StoragePayload deserializeNBTWithPayload(CompoundTag compoundTag, MutableStatisticsWorkaround mutableStatistics, IStateEntryStorage storage) {
        var byteArray = compoundTag.getByteArray("X");

        var inflater = new InflaterInputStream(new ByteArrayInputStream(byteArray));
        var inflatedBuffer = ByteBuffer.allocate(3145728);

        int usedBytes = 0;
        int rv = 0;

        do {
            usedBytes += rv;
            try {
                rv = inflater.read(inflatedBuffer.array(), usedBytes, inflatedBuffer.limit() - usedBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (rv > 0);

        var format = CB2BCConverter.loadCBLegacy(new FriendlyByteBuf(Unpooled.wrappedBuffer(inflatedBuffer)));

        for (int i = 0; i < format.blocks.length; i++) {
            var z = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var x = i & 15;

            var stateId = format.blocks[i];
            var blockId = format.getLegacyIdFromStateId(stateId);
            var metadata = format.getMetadataFromStateId(stateId);

            var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[blockId];
            String serializedBlockState;

            if (legacyFcbName == null) {
                var value = BlockStateData.getTag(((blockId & 255) << 4) | metadata);
                serializedBlockState = ((CompoundTag) value.getValue()).getAsString();
            } else {
                var name = legacyFcbName.contains("transparent0_") ?
                        "flatcoloredblocks:flatcoloredblock_transparent_127" :
                        legacyFcbName.contains("glowing0_") ?
                                "flatcoloredblocks:flatcoloredblock_glowing_255" :
                                "flatcoloredblocks:flatcoloredblock";

                var oldMetadataWorkaround = legacyFcbName
                        .replace("flatcoloredblocks:flatcoloredblock", "")
                        .replace("_transparent0_", "")
                        .replace("_glowing0_", "");

                var offset = Integer.parseInt(oldMetadataWorkaround) * 16;

                serializedBlockState = "{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}";
            }

            var blockState = BlockStateSerializationUtils.deserialize(serializedBlockState).result();

            if (blockState.isEmpty())
                storage.setBlockInformation(x, y, z, IBlockInformation.AIR);
            else
                storage.setBlockInformation(x, y, z, new BlockInformation(blockState.get(), Optional.empty()));
        }

        ((MutableStatisticsAccessor) mutableStatistics).callRecalculate(storage, false);

        return new StoragePayload(storage, mutableStatistics);
    }

    private void deserializeNBT(CompoundTag compoundTag, MutableStatisticsWorkaround mutableStatistics, IStateEntryStorage storage) {
        deserializeNBTWithPayload(compoundTag, mutableStatistics, storage);

        ((MutableStatisticsAccessor) mutableStatistics).callRecalculate(storage, false);

        ((ChiseledBlockEntityAccessor) blockEntity).setStorage(storage);
        ((ExtendedChiseledBlockEntity) blockEntity).setMutableStatistics(mutableStatistics);
    }

    @Override
    public StoragePayload readPayloadOffThread(CompoundTag compoundTag) {
        var storage = new SimpleStateEntryStorage();
        var mutableStatistics = (MutableStatisticsWorkaround) blockEntity.getStatistics();

        return deserializeNBTWithPayload(compoundTag, mutableStatistics, storage);
    }

    @Override
    public void syncPayloadOnGameThread(StoragePayload payload) {
        var blockEntityAccessor = ((ChiseledBlockEntityAccessor) blockEntity);

        blockEntityAccessor.setStorage(payload.storage());
        ((ExtendedChiseledBlockEntity) blockEntity).setMutableStatistics(payload.mutableStatistics());

        if (!blockEntityAccessor.isInitialized()) {
            blockEntity.setChanged();
        }

        blockEntityAccessor.setIsInitialized(true);
    }
}
