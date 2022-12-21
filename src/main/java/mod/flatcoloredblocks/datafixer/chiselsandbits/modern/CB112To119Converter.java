package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import com.google.common.math.LongMath;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.math.RoundingMode;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CB112To119Converter {
    private static final int size = 16;

    public static Dynamic<?> convert(List<Dynamic<?>> palette, int[] blocks, int lightLevel) {
        var compound = new CompoundTag();

        var paletteList = new ListTag();
        for (Dynamic<?> id : palette) {
            var additionalStateCompound = new CompoundTag();
            var stateCompound = (CompoundTag) id.getValue();

            additionalStateCompound.putString("state", stateCompound.getAsString());

            paletteList.add(additionalStateCompound);
        }

        compound.put("palette", paletteList);

        var entryWidth = LongMath.log2(paletteList.size(), RoundingMode.CEILING);
        var requiredSize = (int) (Math.ceil((16 * 16 * 16 * entryWidth) / (float) Byte.SIZE));
        var bitSet = BitSet.valueOf(new byte[requiredSize]);

        for (int i = 0; i < blocks.length; i++) {
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

        compound.putByteArray("data", bitSet.toByteArray());

        // Now we recreate the data
        var nbt = new CompoundTag();

        nbt.put("chiseledData", compound);

        var total = 0;
        CompoundTag primary = null;

        for (int paletteIndex : blocks) {
            var block = palette.get(paletteIndex);

            if (!block.get("Name").asString("minecraft:air").equals("minecraft:air")) {
                total++;

                if (primary == null) {
                    var additionalState = new CompoundTag();
                    additionalState.putString("state", ((CompoundTag) block.getValue()).getAsString());

                    primary = additionalState;
                }
            }
        }

        var statistics = new CompoundTag();
        statistics.put("primary_block_information", primary);
        statistics.putInt("blockCount", total);
        statistics.putInt("blockShouldCheckWeakPowerCount", 0);
        statistics.putInt("totalLightLevel", lightLevel);

        nbt.put("statistics", statistics);

        return new Dynamic<>(NbtOps.INSTANCE, nbt);
    }

    private static Vec3i calculatePosition(int index) {
        int x = index / (size * size);
        int y = (index - x * size * size) / size;
        int z = index - x * size * size - y * size;
        return new Vec3i(x, y, z);
    }

    /*
    // statistics
                // a lot of data is intentionally left out, because otherwise it would take too long to reimplement.
                var statisticsDynamic = new Dynamic<>(NbtOps.INSTANCE);

                statisticsDynamic = statisticsDynamic.set("primary_block_information", new Dynamic<>(NbtOps.INSTANCE)
                        .set("state", statisticsDynamic.createString(primaryBlock))
                );

                statisticsDynamic = statisticsDynamic.set("blockCount", statisticsDynamic.createInt(totalTimes.values().stream().mapToInt(Integer::intValue).sum() - totalAir));
                statisticsDynamic = statisticsDynamic.set("blockShouldCheckWeakPowerCount", statisticsDynamic.createInt(0));
                statisticsDynamic = statisticsDynamic.set("totalLightLevel", statisticsDynamic.createInt(blockEntity.get("lv").asInt(0)));

                compressedDataDynamic = compressedDataDynamic.set("statistics", statisticsDynamic);
     */
}
