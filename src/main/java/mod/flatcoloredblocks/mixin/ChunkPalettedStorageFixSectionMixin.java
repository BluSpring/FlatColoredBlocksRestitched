package mod.flatcoloredblocks.mixin;

import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntList;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.LegacyFCBBlockFix;
import mod.flatcoloredblocks.datafixer.chiselsandbits.ChiselsAndBitsData;
import mod.flatcoloredblocks.duck.ExtendedChunkPalettedStorageFixSection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

@Mixin(targets = "net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix$Section")
public abstract class ChunkPalettedStorageFixSectionMixin implements ExtendedChunkPalettedStorageFixSection {
    @Unique
    private List<ChiselsAndBitsData> fcb$blockEntities = null;

    private static boolean hasMapped = false;

    @Shadow public abstract void setBlock(int i, Dynamic<?> dynamic);

    @Shadow protected abstract void addFix(int i, int j);

    @Shadow @Final
    IntList update;

    @Shadow @Final public int y;

    @Override
    public List<ChiselsAndBitsData> getChiselsAndBitsData() {
        return this.fcb$blockEntities;
    }

    @Override
    public void setChiselsAndBitsData(List<ChiselsAndBitsData> chiselsAndBitsDataList) {
        this.fcb$blockEntities = chiselsAndBitsDataList;
    }

    @Inject(method = "upgrade", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void fcb$upgradeBlocks(int i, CallbackInfoReturnable<Integer> cir, ByteBuffer byteBuffer, ChunkPalettedStorageFix.DataLayer dataLayer, ChunkPalettedStorageFix.DataLayer dataLayer2) {
        if (!hasMapped) {
            LegacyFCBBlockFix.mapLegacyFCBNames();
            hasMapped = true;
        }

        var blockEntities = new HashMap<Integer, ChiselsAndBitsData>();

        if (this.fcb$blockEntities != null)
            for (var blockEntity : this.fcb$blockEntities) {
                var blockPos = blockEntity.blockPos();
                if ((blockPos.getY() / 16) == this.y) {
                    blockEntities.put(
                            (16 * (blockPos.getX() % 16)) + (16 * 16 * (blockPos.getZ() % 16)) + (blockPos.getY() % 16),
                            blockEntity
                    );
                }
            }

        // Reimplement ChunkPalettedStorageFix$Section#upgrade, it seems to be the best thing I can do for right now.
        for(int j = 0; j < 4096; ++j) {
            int k = j & 15;
            int l = j >> 8 & 15;
            int m = j >> 4 & 15;

            var legacyId = ((byteBuffer.get(j) & 255)) + (dataLayer2.get(k, l, m) << 8);

            try {
                var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[legacyId];

                if (
                        legacyFcbName != null
                                && legacyFcbName.startsWith("chiselsandbits:")
                                && FabricLoader.getInstance().isModLoaded("bitsandchisels")
                ) { // Convert Chisels and Bits to Bits and Chisels
                    var blockName = "bitsandchisels:bits_block";
                    // This is a formula I had managed to come up with at school
                    // that gets the index based off of XYZ positions.
                    // This is not to be used directly for the B&C format, as it may
                    // be severely different.
                    // The formula is: i = wx + dhz + y
                    var index = (16 * k) + (16 * 16 * m) + l;

                    var blockEntity = blockEntities.get(index);

                    if (blockEntity == null) // Double check to make sure it actually exists.
                        continue;

                    var parsed = BlockStateData.parse("{Name:'" + blockName + "',Properties:{light_level:'" + blockEntity.lightLevel() + "',waterlogged:'false'}}");
                    this.setBlock(j, parsed);

                    continue;
                } else if (legacyFcbName != null && legacyFcbName.startsWith("flatcoloredblocks:")) {
                    var metadata = dataLayer.get(k, l, m);

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

                    var parsed = BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}");
                    this.setBlock(j, parsed);

                    continue;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }

            int n = dataLayer2.get(k, l, m) << 12 | (byteBuffer.get(j) & 255) << 4 | dataLayer.get(k, l, m);

            if (ChunkPalettedStorageFixAccessor.getFIX().get(n >> 4)) {
                this.addFix(n >> 4, j);
            }

            if (ChunkPalettedStorageFixAccessor.getVIRTUAL().get(n >> 4)) {
                int o = ChunkPalettedStorageFix.getSideMask(k == 0, k == 15, m == 0, m == 15);
                if (o == 0) {
                    this.update.add(j);
                } else {
                    i |= o;
                }
            }

            this.setBlock(j, BlockStateData.getTag(n));
        }

        cir.setReturnValue(i);
    }
}
