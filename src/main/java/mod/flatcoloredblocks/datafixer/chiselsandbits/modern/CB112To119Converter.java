package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CB112To119Converter {
    private static final int size = 16;

    public static Dynamic<?> convert(List<Dynamic<?>> palette, int[] blocks, int lightLevel) {
        var storage = new SimpleStateEntryStorage();

        // 1.12 to 1.16 format (LegacyChunkSection)
        var chunkSection = new LevelChunkSection(0, BuiltinRegistries.BIOME);
        var compound = new CompoundTag();

        var paletteList = new ListTag();
        for (Dynamic<?> id : palette) {
            var stateCompound = (CompoundTag) id.getValue();

            paletteList.add(stateCompound);
        }

        compound.put("palette", paletteList);
        var bitStorage = new SimpleBitStorage(Math.max(4, Mth.ceillog2(palette.size())), 4096);

        for (int i = 0; i < blocks.length; i++) {
            var pos = calculatePosition(i);
            var index = (pos.getY() << 4 | pos.getZ()) << 4 | pos.getX();
            bitStorage.set(index, blocks[i]);
        }

        compound.putLongArray("blockStates", bitStorage.getRaw());

        ChunkSectionUtils.deserializeNBT(chunkSection, compound);
        storage.loadFromChunkSection(chunkSection);

        // Now we recreate the data
        var nbt = new CompoundTag();

        nbt.put("chiseledData", storage.serializeNBT());

        var total = 0;
        CompoundTag primary = null;

        for (int paletteIndex : blocks) {
            var block = storage.getContainedPalette().get(paletteIndex);

            var serializedBlock = block.serializeNBT();

            if (!block.isAir()) {
                total++;

                if (primary == null)
                    primary = serializedBlock;
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
