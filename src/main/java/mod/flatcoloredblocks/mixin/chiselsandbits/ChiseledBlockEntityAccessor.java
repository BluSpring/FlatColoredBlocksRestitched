package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChiseledBlockEntity.class, remap = false)
public interface ChiseledBlockEntityAccessor {
    @Accessor
    IStateEntryStorage getStorage();

    @Accessor(remap = false, value = "isInitialized")
    boolean isInitialized();

    @Accessor(remap = false)
    void setStorage(IStateEntryStorage storage);

    @Accessor(remap = false)
    void setIsInitialized(boolean isInitialized);
}
