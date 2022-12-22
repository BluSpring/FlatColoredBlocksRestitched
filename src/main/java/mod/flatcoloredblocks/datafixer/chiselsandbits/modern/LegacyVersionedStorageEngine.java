package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import mod.chiselsandbits.storage.IStorageEngine;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers.StoragePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LegacyVersionedStorageEngine implements IStorageEngine
{
    private final List<ILegacyStorageHandler> handlers;

    public LegacyVersionedStorageEngine(final LinkedList<ILegacyStorageHandler> handlers) {
        this.handlers = Collections.unmodifiableList(handlers);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        throw new IllegalStateException("Legacy storage can not write to NBT");
    }

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        throw new IllegalStateException("Legacy storage can not write to NBT");
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        handlers.stream()
                .filter(handler -> handler.matches(nbt))
                .findFirst()
                .ifPresent(handler -> handler.deserializeNBT(nbt));
    }

    public CompletableFuture<Void> deserializeOffThread(final CompoundTag nbt, Executor ioExecutor, Executor gameExecutor) {
        var handler = handlers.stream()
                .filter(h -> h.matches(nbt))
                .findFirst();

        if (handler.isEmpty())
            return null;

        var storageHandler = handler.get();

        return CompletableFuture.supplyAsync(() -> storageHandler.readPayloadOffThread(nbt), ioExecutor)
                .thenAcceptAsync(storageHandler::syncPayloadOnGameThread, gameExecutor);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        throw new IllegalStateException("Legacy storage can not write to the network");
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        throw new IllegalStateException("Legacy storage can not read from the network");
    }

    @Override
    public Collection<ILegacyStorageHandler> getHandlers()
    {
        return handlers;
    }
}