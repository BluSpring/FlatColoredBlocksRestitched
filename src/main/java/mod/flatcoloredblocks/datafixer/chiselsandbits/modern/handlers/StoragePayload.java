package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.flatcoloredblocks.mixin.chiselsandbits.MutableStatisticsAccessor;

public record StoragePayload(IStateEntryStorage storage, MutableStatisticsAccessor mutableStatistics) {
}
