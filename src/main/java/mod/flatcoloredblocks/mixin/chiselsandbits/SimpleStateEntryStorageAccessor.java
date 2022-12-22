package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.block.entities.storage.SimpleStateEntryPalette;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(SimpleStateEntryStorage.class)
public interface SimpleStateEntryStorageAccessor {
    @Accessor
    int getSize();

    @Mutable
    @Accessor
    void setSize(int size);

    @Accessor
    SimpleStateEntryPalette getPalette();

    @Mutable
    @Accessor
    void setPalette(SimpleStateEntryPalette palette);

    @Accessor
    BitSet getData();

    @Accessor
    void setData(BitSet data);

    @Accessor
    int getEntryWidth();

    @Accessor
    void setEntryWidth(int entryWidth);

    @Accessor
    boolean isIsDeserializing();

    @Accessor
    void setIsDeserializing(boolean isDeserializing);
}
