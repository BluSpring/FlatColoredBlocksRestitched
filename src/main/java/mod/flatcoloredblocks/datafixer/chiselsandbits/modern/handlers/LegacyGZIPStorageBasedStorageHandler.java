package mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.ILegacyStorageHandler;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class LegacyGZIPStorageBasedStorageHandler implements ILegacyStorageHandler {
    private ChiseledBlockEntity blockEntity;

    public LegacyGZIPStorageBasedStorageHandler(ChiseledBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean matches(@NotNull CompoundTag compoundTag) {
        return true;
    }

    @Override
    public StoragePayload readPayloadOffThread(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void syncPayloadOnGameThread(StoragePayload payload) {

    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }
}
