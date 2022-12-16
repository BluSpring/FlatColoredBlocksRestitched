package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "mod.chiselsandbits.block.entities.ChiseledBlockEntity$MutableStatistics")
public interface MutableStatisticsAccessor {
    @Invoker
    void callRecalculate(IStateEntryStorage source, boolean mayUpdateWorld);
}
