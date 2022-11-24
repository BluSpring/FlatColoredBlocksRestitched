package mod.flatcoloredblocks.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.forge.LegacyForgeBlockParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
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

    @Mixin(LevelStorageSource.LevelStorageAccess.class)
    public static class LevelStorageAccessMixin {
        // This is required as datafixing may also occur in the future.
        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"), method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V", locals = LocalCapture.CAPTURE_FAILHARD)
        public void fcbSaveLegacyForgeData(RegistryAccess registryAccess, WorldData worldData, CompoundTag compoundTag, CallbackInfo ci, File file, CompoundTag compoundTag2, CompoundTag compoundTag3) {
            if (FlatColoredBlocks.legacyForgeBlockParser != null) {
                var legacyIds = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString;

                var idsList = new ListTag();

                for (int i = 0; i < legacyIds.length; i++) {
                    if (legacyIds[i] == null)
                        continue;

                    var idCompound = new CompoundTag();
                    idCompound.putString("K", legacyIds[i]);
                    idCompound.putInt("V", i);

                    idsList.add(idCompound);
                }

                var blocksCompound = new CompoundTag();
                blocksCompound.put("ids", idsList);

                var registriesCompound = new CompoundTag();
                registriesCompound.put("minecraft:blocks", blocksCompound);

                var fmlCompound = new CompoundTag();
                fmlCompound.put("Registries", registriesCompound);

                compoundTag3.put("FML", fmlCompound);
            }
        }
    }
}
