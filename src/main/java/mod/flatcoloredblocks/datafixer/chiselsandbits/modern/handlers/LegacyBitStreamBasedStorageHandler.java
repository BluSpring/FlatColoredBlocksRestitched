package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.BlockStateSerializationUtils;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.chiselsandbits.CB2BCConverter;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedChiseledBlockEntity;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ILegacyStorageHandler;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.MutableStatisticsWorkaround;
import mod.flatcoloredblocks.mixin.chiselsandbits.ChiseledBlockEntityAccessor;
import mod.flatcoloredblocks.mixin.chiselsandbits.MutableStatisticsAccessor;
import mod.flatcoloredblocks.mixin.chiselsandbits.SimpleStateEntryStorageAccessor;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
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
        var section = new LevelChunkSection(0, BuiltinRegistries.BIOME);

        for (int i = 0; i < format.blocks.length; i++) {
            var z = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var x = i & 15;

            var stateId = format.blocks[i];
            var blockId = format.getLegacyIdFromStateId(stateId);
            var metadata = format.getMetadataFromStateId(stateId);

            var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[blockId];
            CompoundTag serializedBlockState;

            if (legacyFcbName == null) {
                var value = BlockStateData.getTag(((blockId & 255) << 4) | metadata);
                serializedBlockState = ((CompoundTag) value.getValue());
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

                serializedBlockState = (CompoundTag) BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}").getValue();
            }

            var blockState = BlockState.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, serializedBlockState));

            if (blockState.result().isPresent())
                section.setBlockState(x, y, z, blockState.result().get());
            else
                section.setBlockState(x, y, z, Blocks.AIR.defaultBlockState());
        }

        var storage = new SimpleStateEntryStorage();
        ((ChiseledBlockEntityAccessor) blockEntity).setStorage(storage);

        storage.clear();
        ((SimpleStateEntryStorageAccessor) storage).setIsDeserializing(true);
        storage.loadFromChunkSection(section);
        ((SimpleStateEntryStorageAccessor) storage).setIsDeserializing(false);
    }
}
