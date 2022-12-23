package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.math.LongMath;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.LZ4DataCompressionUtils;
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

    public static Dynamic<?> convert(List<Dynamic<?>> palette, int[] blocks, int lightLevel, Dynamic<?> primaryDynamic) {
        var compound = new CompoundTag();

        var paletteList = new ListTag();
        var airIndex = -1;

        var totalMapping = new int[palette.size()];

        for (Dynamic<?> id : palette) {
            var additionalStateCompound = new CompoundTag();
            var stateCompound = (CompoundTag) id.getValue();

            if (stateCompound.getString("Name").equals("minecraft:air"))
                airIndex = paletteList.size();

            additionalStateCompound.putString("state", stateCompound.getAsString());

            paletteList.add(additionalStateCompound);
            totalMapping[paletteList.size() - 1] = 0;
        }

        if (airIndex != -1) {
            var oldZeroIndex = paletteList.get(0);
            var air = paletteList.get(airIndex);
            paletteList.set(airIndex, oldZeroIndex);
            paletteList.set(0, air);
        }

        compound.put("palette", paletteList);

        var entryWidth = LongMath.log2(paletteList.size(), RoundingMode.CEILING);
        var requiredSize = (int) (Math.ceil((16 * 16 * 16 * entryWidth) / (float) Byte.SIZE));
        var bitSet = new BitSet(requiredSize);

        var total = 0;

        Table<Integer, Integer, CompoundTag> table = HashBasedTable.create();

        for (int i = 0; i < blocks.length; i++) {
            var x = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var z = i & 15;

            var pos = x * 16 * 16 + y * 16 + z;
            var bitOffset = pos * entryWidth;

            bitSet.clear(bitOffset, bitOffset + entryWidth);

            var paletteIndex = blocks[i];
            if (paletteIndex == 0 && airIndex != -1)
                paletteIndex = airIndex;
            else if (paletteIndex == airIndex)
                paletteIndex = 0;

            for (int j = 0; j < entryWidth; ++j) {
                var isSet = ((paletteIndex >> j) & 1) != 0;

                bitSet.set(bitOffset + j, isSet);
            }

            if (paletteIndex != 0)
                total++;

            totalMapping[paletteIndex]++;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                var skylightBlocking = new BitSet(16);
                var noneAir = new BitSet(16);
                short highestBit = 0;
                float highestBitFriction = 0.6F;
                boolean canPropagateSkylightDown = true;
                boolean lowestBitCanSustainGrass = false;

                for (int y = 0; y < 16; y++) {
                    var paletteIndex = blocks[(x << 8) | (y << 4) | z];
                    if (paletteIndex == 0 && airIndex != -1)
                        paletteIndex = airIndex;
                    else if (paletteIndex == airIndex)
                        paletteIndex = 0;

                    var blockType = palette.get(paletteIndex);
                    var blockTypeNbt = (CompoundTag) blockType.getValue();

                    if (!blockTypeNbt.getString("Name").equals("minecraft:air")) {
                        noneAir.set(y, true);
                        skylightBlocking.set(y, true);

                        if (y >= highestBit) {
                            highestBit = (short) y;
                        }
                    }
                }

                var tag = new CompoundTag();

                tag.putBoolean("can_propagate_skylight_down", canPropagateSkylightDown);
                tag.putShort("highestBit", highestBit);
                tag.putFloat("highestBitFriction", highestBitFriction);
                tag.putBoolean("lowest_bit_can_sustain_grass", lowestBitCanSustainGrass);
                tag.putByteArray("none_air_bits", noneAir.toByteArray());
                tag.putByteArray("skylight_blocking_bits", skylightBlocking.toByteArray());

                table.put(x, z, tag);
            }
        }

        compound.putByteArray("data", bitSet.toByteArray());

        // Now we recreate the data
        var primary = new CompoundTag();
        primary.putString("state", ((CompoundTag) primaryDynamic.getValue()).getAsString());

        var statistics = new CompoundTag();
        statistics.put("primary_block_information", primary);
        statistics.putInt("blockCount", total);
        statistics.putInt("blockShouldCheckWeakPowerCount", total);
        statistics.putInt("totalLightLevel", lightLevel);
        //statistics.putInt("totalLightBlockLevel", total * 15);

        var columnStatistics = new CompoundTag();
        var rows = table.rowMap();
        for (int x = 0; x < 16; x++) {
            var columns = new CompoundTag();
            var row = rows.get(x);

            if (row == null) {
                for (int z = 0; z < 16; z++) {
                    var tag = new CompoundTag();

                    tag.putBoolean("can_propagate_skylight_down", true);
                    tag.putShort("highestBit", (short) 0);
                    tag.putFloat("highestBitFriction", 0.6F);
                    tag.putBoolean("lowest_bit_can_sustain_grass", true);
                    tag.putByteArray("none_air_bits", new BitSet(16).toByteArray());
                    tag.putByteArray("skylight_blocking_bits", new BitSet(16).toByteArray());

                    columns.put(Integer.toString(z), tag);
                }

                columnStatistics.put(Integer.toString(x), columns);
                continue;
            }

            for (int z = 0; z < 16; z++) {
                var column = row.get(z);

                if (column == null) {
                    var tag = new CompoundTag();

                    tag.putBoolean("can_propagate_skylight_down", true);
                    tag.putShort("highestBit", (short) 0);
                    tag.putFloat("highestBitFriction", 0.6F);
                    tag.putBoolean("lowest_bit_can_sustain_grass", true);
                    tag.putByteArray("none_air_bits", new BitSet(16).toByteArray());
                    tag.putByteArray("skylight_blocking_bits", new BitSet(16).toByteArray());

                    columns.put(Integer.toString(z), tag);
                } else {
                    columns.put(Integer.toString(z), column);
                }
            }

            columnStatistics.put(Integer.toString(x), columns);
        }

        statistics.put("column_statistics", columnStatistics);

        var totalData = new ListTag();
        for (int i = 0; i < totalMapping.length; i++) {
            if (i == 0)
                continue;

            var blockInfo = paletteList.get(i);

            var totalCompound = new CompoundTag();
            totalCompound.put("block_information", blockInfo);
            totalCompound.putInt("count", totalMapping[i]);

            totalData.add(totalCompound);
        }

        statistics.put("blockStates", totalData);

        // All Collision
        var allCollisions = new BitSet(16);

        // Collidable Only (not air and no fluid)
        var collidableOnlyCollisions = new BitSet(16);

        // None Air Collision (not air)
        var noneAirCollisions = new BitSet(16);

        for (int i = 0; i < blocks.length; i++) {
            var x = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var z = i & 15;

            var paletteIndex = blocks[i];
            if (paletteIndex == 0 && airIndex != -1)
                paletteIndex = airIndex;
            else if (paletteIndex == airIndex)
                paletteIndex = 0;

            var pos = (x * (16 * 16)) + (y * 16) + z;

            allCollisions.set(pos, true);

            if (paletteIndex != 0) {
                collidableOnlyCollisions.set(pos, true);
                noneAirCollisions.set(pos, true);
            }
        }

        var collisions = new CompoundTag();
        collisions.putLongArray("ALL", allCollisions.toLongArray());
        collisions.putLongArray("COLLIDEABLE_ONLY", collidableOnlyCollisions.toLongArray());
        collisions.putLongArray("NONE_AIR", noneAirCollisions.toLongArray());

        statistics.put("collision_data", collisions);

        var compressed = LZ4DataCompressionUtils.compress((compoundTag) -> {
            compoundTag.put("chiseledData", compound);
            compoundTag.put("statistics", statistics);
        });

        return new Dynamic<>(NbtOps.INSTANCE, compressed);
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
