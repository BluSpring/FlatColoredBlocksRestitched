package mod.flatcoloredblocks.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import mod.flatcoloredblocks.datafixer.FCBDataFixer;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    @Inject(at = @At("TAIL"), method = "addFixers")
    private static void addFCBDataFixers(DataFixerBuilder dataFixerBuilder, CallbackInfo ci) {
        FCBDataFixer.addFixers(dataFixerBuilder);
    }
}
