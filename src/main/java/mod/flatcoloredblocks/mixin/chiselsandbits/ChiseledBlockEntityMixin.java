package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.storage.IStorageHandler;
import mod.chiselsandbits.storage.StorageEngineBuilder;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedChiseledBlockEntity;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedStorageEngineBuilder;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers.LegacyBitStreamBasedStorageHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChiseledBlockEntity.class, remap = false)
public class ChiseledBlockEntityMixin implements ExtendedChiseledBlockEntity {
    @Override
    public void setMutableStatistics(MutableStatisticsAccessor statistics) {
        try {
            var field = ChiseledBlockEntity.class.getDeclaredField("mutableStatistics");
            field.setAccessible(true);
            field.set(this, statistics);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmod/chiselsandbits/storage/StorageEngineBuilder;with(Lmod/chiselsandbits/storage/IStorageHandler;)Lmod/chiselsandbits/storage/StorageEngineBuilder;"), method = "createStorageEngine", remap = false)
    public StorageEngineBuilder fcbr$createLegacyStorageEngine(StorageEngineBuilder instance, IStorageHandler<?> handler) {
        var blockEntity = (ChiseledBlockEntity) (Object) this;

        return ((ExtendedStorageEngineBuilder) instance)
                .withLegacies(
                        new LegacyBitStreamBasedStorageHandler(blockEntity)
                )
                .with(handler);
    }
}
