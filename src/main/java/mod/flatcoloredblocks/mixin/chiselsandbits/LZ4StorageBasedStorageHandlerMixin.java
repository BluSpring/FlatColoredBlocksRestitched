package mod.flatcoloredblocks.mixin.chiselsandbits;

import com.google.common.math.LongMath;
import com.mojang.serialization.Dynamic;
import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.chiselsandbits.CB2BCConverter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.datafix.fixes.BlockStateData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;

@Mixin(targets = "mod.chiselsandbits.block.entities.ChiseledBlockEntity$LZ4StorageBasedStorageHandler", remap = false)
public abstract class LZ4StorageBasedStorageHandlerMixin {
    @Shadow @Final private ChiseledBlockEntity this$0;

    @Inject(at = @At("HEAD"), method = "readPayloadOffThread(Lnet/minecraft/nbt/CompoundTag;)Lmod/chiselsandbits/block/entities/ChiseledBlockEntity$LZ4StorageBasedStorageHandler$Payload;", cancellable = true, remap = false)
    public void fcbr$overrideDeserializeIfLegacy(CompoundTag nbt, CallbackInfoReturnable<Object> cir) {
        if (nbt.contains("X")) {
            var format = CB2BCConverter.deflateAndLoadCBLegacy(ByteBuffer.wrap(nbt.getByteArray("X")));
            var palette = new ArrayList<IBlockInformation>();
            var blocks = format.blocks;

            for (int stateId : format.palette) {
                var blockId = format.getLegacyIdFromStateId(stateId);
                var metadata = format.getMetadataFromStateId(stateId);

                var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[blockId];

                Dynamic<?> dynamic;
                if (legacyFcbName == null) { // Probably a Minecraft block
                    dynamic = BlockStateData.getTag(((blockId & 255) << 4) | metadata);
                } else { // Very likely to be Flat Colored Blocks
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

                    dynamic = BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}");
                }

                palette.add(new BlockInformation((CompoundTag) dynamic.getValue()));
            }

            var entryWidth = LongMath.log2(palette.size(), RoundingMode.CEILING);
            var requiredSize = (int) (Math.ceil((16 * 16 * 16 * entryWidth) / (float) Byte.SIZE));
            var bitSet = BitSet.valueOf(new byte[requiredSize]);

            for (int i = 0; i < format.blocks.length; i++) {
                var x = (i >> 8) & 15;
                var y = (i >> 4) & 15;
                var z = i & 15;

                var pos = x * 16 * 16 + y * 16 + z;
                var bitOffset = pos * entryWidth;

                bitSet.clear(bitOffset, bitOffset + entryWidth);

                for (int j = 0; j < entryWidth; ++j) {
                    var isSet = ((blocks[i] >> j) & 1) != 0;

                    bitSet.set(bitOffset + j, isSet);
                }
            }

            var regularStorage = ((ChiseledBlockEntityAccessor) this$0).getStorage();
            var storage = ((SimpleStateEntryStorageAccessor) regularStorage);

            regularStorage.clear();
            storage.setIsDeserializing(true);

            for (IBlockInformation blockInformation : palette) {
                storage.getPalette().getIndex(blockInformation);
            }

            storage.setData(bitSet);
            storage.setIsDeserializing(false);

            try {
                var mutableStatisticsField = this$0.getClass().getDeclaredField("mutableStatistics");
                mutableStatisticsField.setAccessible(true);

                var mutableStatistics = mutableStatisticsField.get(this$0);
                var recalculateMethod = mutableStatistics.getClass().getDeclaredMethod("recalculate", IStateEntryStorage.class, Boolean.class);

                recalculateMethod.setAccessible(true);
                recalculateMethod.invoke(mutableStatistics, regularStorage, false);

                var recordClass = Class.forName("mod.chiselsandbits.block.entities.ChiseledBlockEntity$LZ4StorageBasedStorageHandler$Payload");
                var recordConstructor = recordClass.getDeclaredConstructors()[0];
                recordConstructor.setAccessible(true);

                cir.setReturnValue(
                        recordConstructor.newInstance(regularStorage, mutableStatistics)
                );
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                     ClassNotFoundException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
