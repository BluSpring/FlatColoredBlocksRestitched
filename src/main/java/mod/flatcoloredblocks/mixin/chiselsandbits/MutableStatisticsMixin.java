package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.MutableStatisticsWorkaround;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "mod.chiselsandbits.block.entities.ChiseledBlockEntity$MutableStatistics", remap = false)
public abstract class MutableStatisticsMixin implements MutableStatisticsWorkaround {
}
