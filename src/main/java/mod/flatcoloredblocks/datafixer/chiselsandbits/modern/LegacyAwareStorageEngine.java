package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import com.google.common.collect.ImmutableList;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.storage.IStorageHandler;
import mod.chiselsandbits.storage.IThreadAwareStorageEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LegacyAwareStorageEngine implements IThreadAwareStorageEngine {
    private final LegacyVersionedStorageEngine legacyVersionedStorageEngine;
    private final VersionedStorageEngine versionedStorageEngine;

    private final Collection<IStorageHandler<?>> handlers;

    public LegacyAwareStorageEngine(final LegacyVersionedStorageEngine legacyVersionedStorageEngine, final VersionedStorageEngine versionedStorageEngine) {
        this.legacyVersionedStorageEngine = legacyVersionedStorageEngine;
        this.versionedStorageEngine = versionedStorageEngine;

        handlers = ImmutableList.<IStorageHandler<?>>builder()
                .addAll(legacyVersionedStorageEngine.getHandlers())
                .addAll(versionedStorageEngine.getHandlers())
                .build();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return versionedStorageEngine.serializeNBT();
    }

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        versionedStorageEngine.serializeNBTInto(tag);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        if (nbt.contains(NbtConstants.VERSION)) {
            //This is considered a versioned implementation.
            versionedStorageEngine.deserializeNBT(nbt);
            return;
        }

        legacyVersionedStorageEngine.deserializeNBT(nbt);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        versionedStorageEngine.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        versionedStorageEngine.deserializeFrom(packetBuffer);
    }

    @Override
    public Collection<? extends IStorageHandler> getHandlers()
    {
        return handlers;
    }

    public HandlerWithData getThreadAwareStorageHandler(CompoundTag tag) {
        if (tag.contains(NbtConstants.VERSION)) {
            //This is considered a versioned implementation.
            return versionedStorageEngine.getThreadAwareStorageHandler(tag);
        }

        return null;
    }

    @Override
    public CompletableFuture<Void> deserializeOffThread(CompoundTag compoundTag, Executor ioExecutor, Executor gameExecutor) {
        var handlerData = getThreadAwareStorageHandler(compoundTag);

        if (handlerData == null)
            return CompletableFuture.completedFuture(null);

        var handler = handlerData.handler();

        return doDeserializeOffThread(handler, compoundTag, ioExecutor, gameExecutor);
    }

    private <P> CompletableFuture<Void> doDeserializeOffThread(IStorageHandler<P> handler, CompoundTag tag, Executor ioExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(
                () -> handler.readPayloadOffThread(tag.getCompound(NbtConstants.DATA)),
                ioExecutor
        ).thenAcceptAsync(handler::syncPayloadOnGameThread, gameExecutor);
    }
}
