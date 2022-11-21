package mod.flatcoloredblocks.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.forge.LegacyForgeBlockParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {
    // method_29582 is equivalent to the lambda
    // For future reference, you can access it via View -> Show Bytecode
    @Inject(method = "method_29582", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fcbLoadLegacyForgeData(DynamicOps dynamicOps, DataPackConfig dataPackConfig, Lifecycle lifecycle, Path path, DataFixer dataFixer, CallbackInfoReturnable<PrimaryLevelData> cir, CompoundTag compressed) {
        if (compressed.contains("FML")) {
            var legacyForgeBlockParser = FlatColoredBlocks.legacyForgeBlockParser = new LegacyForgeBlockParser();
            legacyForgeBlockParser.parseForgeBlockIds(compressed.getCompound("FML"));
        }
    }
}
