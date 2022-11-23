package mod.flatcoloredblocks.datafixer.chiselsandbits;

import net.minecraft.core.BlockPos;

public record ChiselsAndBitsData(
        BlockPos blockPos,
        int lightLevel
) {}
