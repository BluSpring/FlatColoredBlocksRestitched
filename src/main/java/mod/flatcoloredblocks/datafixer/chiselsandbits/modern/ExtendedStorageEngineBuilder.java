package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import mod.chiselsandbits.storage.StorageEngineBuilder;

public interface ExtendedStorageEngineBuilder {
    StorageEngineBuilder withLegacies(ILegacyStorageHandler... handlers);
}
