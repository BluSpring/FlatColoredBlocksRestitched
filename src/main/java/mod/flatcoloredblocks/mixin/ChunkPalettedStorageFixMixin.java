package mod.flatcoloredblocks.mixin;

import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntList;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.LegacyFCBBlockFix;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.ByteBuffer;

@Mixin(targets = "net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix$Section")
public abstract class ChunkPalettedStorageFixMixin {
    private static boolean hasMapped = false;

    @Shadow public abstract void setBlock(int i, Dynamic<?> dynamic);

    @Shadow protected abstract void addFix(int i, int j);

    @Shadow @Final
    IntList update;

    @Inject(method = "upgrade", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void fcb$upgradeBlocks(int i, CallbackInfoReturnable<Integer> cir, ByteBuffer byteBuffer, ChunkPalettedStorageFix.DataLayer dataLayer, ChunkPalettedStorageFix.DataLayer dataLayer2) {
        if (!hasMapped) {
            LegacyFCBBlockFix.mapLegacyFCBNames();
            hasMapped = true;
        }

        // Reimplement ChunkPalettedStorageFix$Section#upgrade, it seems to be the best thing I can do for right now.
        for(int j = 0; j < 4096; ++j) {
            int k = j & 15;
            int l = j >> 8 & 15;
            int m = j >> 4 & 15;

            var legacyId = ((byteBuffer.get(j) & 255) << 4) + (dataLayer2.get(k, l, m) << 8);
            try {
                var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[legacyId];

                if (legacyFcbName != null) {
                    var metadata = dataLayer.get(k, l, m);

                    var name = legacyFcbName.contains("transparent0_") ?
                            "flatcoloredblocks:flatcoloredblock_transparent_127" :
                            legacyFcbName.contains("glowing") ?
                                    "flatcoloredblocks:flatcoloredblock_glowing_255" :
                                    "flatcoloredblocks:flatcoloredblock";

                    var oldMetadataWorkaround = legacyFcbName
                            .replace("flatcoloredblocks:flatcoloredblock", "")
                            .replace("_transparent0_", "")
                            .replace("_glowing0_", "");

                    var offset = Integer.parseInt(oldMetadataWorkaround) * 16;

                    this.setBlock(j, BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}"));

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
