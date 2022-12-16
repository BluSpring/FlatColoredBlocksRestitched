package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.storage.IThreadAwareStorageEngine;
import mod.chiselsandbits.storage.StorageEngineBuilder;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ExtendedStorageEngineBuilder;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ILegacyStorageHandler;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.LegacyAwareStorageEngine;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.LegacyVersionedStorageEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.LinkedList;

@Mixin(value = StorageEngineBuilder.class, remap = false)
public class StorageEngineBuilderMixin implements ExtendedStorageEngineBuilder {
    @Unique
    LinkedList<ILegacyStorageHandler> legacyHandlers = new LinkedList<>();

    @Inject(at = @At("RETURN"), method = "buildThreadAware", remap = false, cancellable = true)
    public void fcbr$recreateLegacyStorageEngine(CallbackInfoReturnable<IThreadAwareStorageEngine> cir) {
        cir.setReturnValue(new LegacyAwareStorageEngine(
                new LegacyVersionedStorageEngine(legacyHandlers),
                cir.getReturnValue()
        ));
    }

    @Unique
    @Override
    public StorageEngineBuilder withLegacies(ILegacyStorageHandler... handlers) {
        legacyHandlers.addAll(Arrays.asList(handlers));

        return (StorageEngineBuilder) (Object) this;
    }
}
