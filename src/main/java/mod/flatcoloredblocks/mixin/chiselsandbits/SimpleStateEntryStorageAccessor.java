package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.block.entities.storage.SimpleStateEntryPalette;
import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleStateEntryStorage.class)
public interface SimpleStateEntryStorageAccessor {
    @Accessor
    SimpleStateEntryPalette getPalette();

    @Accessor
    boolean isIsDeserializing();

    @Accessor
    void setIsDeserializing(boolean isDeserializing);
}
