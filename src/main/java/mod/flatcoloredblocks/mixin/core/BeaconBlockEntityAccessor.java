package mod.flatcoloredblocks.mixin.core;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
    @Accessor
    List<BeaconBlockEntity.BeaconBeamSection> getCheckingBeamSections();

    @Accessor
    int getLastCheckY();
}
