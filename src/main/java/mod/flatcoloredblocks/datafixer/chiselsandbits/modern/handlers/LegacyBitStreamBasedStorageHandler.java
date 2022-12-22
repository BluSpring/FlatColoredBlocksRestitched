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
import mod.flatcoloredblocks.mixin.chiselsandbits.ChiseledBlockEntityAccessor;
import mod.flatcoloredblocks.mixin.chiselsandbits.MutableStatisticsAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.datafix.fixes.BlockStateData;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
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
        var mutableStatistics = (MutableStatisticsAccessor) blockEntity.getStatistics();
        var storage = ((ChiseledBlockEntityAccessor) blockEntity).getStorage();

        deserializeNBT(compoundTag, mutableStatistics, storage);
    }

    private StoragePayload deserializeNBTWithPayload(CompoundTag compoundTag, MutableStatisticsAccessor mutableStatistics, IStateEntryStorage storage) {
        var byteArray = compoundTag.getByteArray("X");

        var format = CB2BCConverter.deflateAndLoadCBLegacy(ByteBuffer.wrap(byteArray));
        var primaryId = compoundTag.getInt("b");
        IBlockInformation primary = IBlockInformation.AIR;

        var totalBlocks = 0;
        var paletteMapping = new HashMap<Integer, IBlockInformation>();

        for (int i = 0; i < format.blocks.length; i++) {
            var z = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var x = i & 15;

            var stateId = format.blocks[i];

            if (!paletteMapping.containsKey(stateId)) {
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

                if (blockState.isEmpty()) {
                    storage.setBlockInformation(x, y, z, IBlockInformation.AIR);
                    paletteMapping.put(stateId, IBlockInformation.AIR);
                } else {
                    var blockInfo = new BlockInformation(blockState.get(), Optional.empty());
                    storage.setBlockInformation(x, y, z, blockInfo);
                    paletteMapping.put(stateId, blockInfo);

                    if (!blockState.get().isAir())
                        totalBlocks++;
                }
            } else {
                storage.setBlockInformation(x, y, z, paletteMapping.get(stateId));
            }

            if (primaryId == stateId) {
                primary = storage.getBlockInformation(x, y, z);
            }
        }

        mutableStatistics.getCountMap().clear();
        mutableStatistics.setPrimaryState(primary);

        mutableStatistics.setTotalLightLevel(compoundTag.getInt("lv"));

        mutableStatistics.setRequiresRecalculation(true);

        mutableStatistics.setTotalUsedBlockCount(totalBlocks);
        mutableStatistics.setTotalUsedChecksWeakPowerCount(totalBlocks);
        mutableStatistics.setTotalLightBlockLevel(0);

        return new StoragePayload(storage, mutableStatistics);
    }

    private void deserializeNBT(CompoundTag compoundTag, MutableStatisticsAccessor mutableStatistics, IStateEntryStorage storage) {
        deserializeNBTWithPayload(compoundTag, mutableStatistics, storage);

        ((ChiseledBlockEntityAccessor) blockEntity).setStorage(storage);
        ((ExtendedChiseledBlockEntity) blockEntity).setMutableStatistics(mutableStatistics);
    }

    @Override
    public StoragePayload readPayloadOffThread(CompoundTag compoundTag) {
        var storage = new SimpleStateEntryStorage();
        var mutableStatistics = (MutableStatisticsAccessor) blockEntity.getStatistics();

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
