package mod.flatcoloredblocks.mixin;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconBlockEntity.BeaconBeamSection.class)
public interface BeaconBeamSectionAccessor {
    @Invoker
    void callIncreaseHeight();
}
