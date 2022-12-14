package mod.flatcoloredblocks.mixin.core;

import mod.flatcoloredblocks.block.BlockFlatColoredTranslucent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @Unique
    private static boolean isInWorkaround = false;

    // To work around a change with beacon beams where they use dye colours instead,
    // we need to essentially reimplement the beacon beam logic for us specifically in order to have custom
    // beam colours. If there's a better way of doing this, please tell me.
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", shift = At.Shift.AFTER), method = "tick", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fcbr$weirdBeaconBeamWorkaround(Level level, BlockPos blockPos, BlockState blockState, BeaconBlockEntity beaconBlockEntity, CallbackInfo ci, int i, int j, int k, BlockPos blockPos2, BeaconBlockEntity.BeaconBeamSection beaconBeamSection, int l, int m, BlockState blockState2) {
        var block = blockState2.getBlock();
        if (block instanceof BlockFlatColoredTranslucent) {
            isInWorkaround = true;

            float[] fs = ((BlockFlatColoredTranslucent) block).getBeaconColorMultiplier(blockState2);
            var checkingBeamSections = ((BeaconBlockEntityAccessor) beaconBlockEntity).getCheckingBeamSections();

            if (checkingBeamSections.size() <= 1) {
                beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(fs);
                checkingBeamSections.add(beaconBeamSection);
            } else if (beaconBeamSection != null) {
                if (Arrays.equals(fs, beaconBeamSection.getColor())) {
                    ((BeaconBeamSectionAccessor) beaconBeamSection).callIncreaseHeight();
                } else {
                    beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(new float[]{(beaconBeamSection.getColor()[0] + fs[0]) / 2.0F, (beaconBeamSection.getColor()[1] + fs[1]) / 2.0F, (beaconBeamSection.getColor()[2] + fs[2]) / 2.0F});
                    checkingBeamSections.add(beaconBeamSection);
                }
            }
        }
    }

    // After the code above, it will fall into the else statement.
    // We do not want that to happen, and so we have a workaround check to ensure that
    // BeaconBeamSection#increaseHeight does not get called.
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity$BeaconBeamSection;increaseHeight()V", ordinal = 1), method = "tick")
    private static void fcbr$preventHeightIncrease(BeaconBlockEntity.BeaconBeamSection instance) {
        if (isInWorkaround) {
            isInWorkaround = false;
        } else {
            ((BeaconBeamSectionAccessor) instance).callIncreaseHeight();
        }
    }

    // However, that check may have a tendency to backfire.
    // Just to be safe, we make sure the workaround check is turned off afterward.
    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"), method = "tick")
    private static void fcbr$clearWorkaround(List instance) {
        if (isInWorkaround)
            isInWorkaround = false;

        instance.clear();
    }
}
