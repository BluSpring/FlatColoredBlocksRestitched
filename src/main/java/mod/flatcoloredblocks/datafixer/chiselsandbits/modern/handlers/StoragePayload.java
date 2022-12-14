package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import mod.chiselsandbits.api.block.storage.IStateEntryStorage;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.MutableStatisticsWorkaround;

public record StoragePayload(IStateEntryStorage storage, MutableStatisticsWorkaround mutableStatistics) {
}
