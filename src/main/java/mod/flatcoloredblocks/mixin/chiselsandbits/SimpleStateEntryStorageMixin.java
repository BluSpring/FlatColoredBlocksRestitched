package mod.flatcoloredblocks.mixin.chiselsandbits;

import mod.chiselsandbits.block.entities.storage.SimpleStateEntryStorage;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleStateEntryStorage.class)
public class SimpleStateEntryStorageMixin {
    @Shadow private boolean isDeserializing;

    @Inject(at = @At("HEAD"), method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", cancellable = true)
    public void fcbr$disableDeserializationIfRunning(CompoundTag nbt, CallbackInfo ci) {
        if (this.isDeserializing)
            ci.cancel();
    }
}
