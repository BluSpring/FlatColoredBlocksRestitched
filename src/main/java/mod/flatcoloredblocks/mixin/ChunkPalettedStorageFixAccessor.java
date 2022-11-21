package mod.flatcoloredblocks.mixin;

import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(ChunkPalettedStorageFix.class)
public interface ChunkPalettedStorageFixAccessor {
    @Accessor
    static BitSet getVIRTUAL() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static BitSet getFIX() {
        throw new UnsupportedOperationException();
    }
}
