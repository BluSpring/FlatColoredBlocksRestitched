package mod.flatcoloredblocks.compat.chiselsandbits;

import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.plugin.IChiselsAndBitsPlugin;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.flatcoloredblocks.block.BlockFlatColored;

@ChiselsAndBitsPlugin
public class FCBRCBCompat implements IChiselsAndBitsPlugin {
    @Override
    public String getId() {
        return "flatcoloredblocks_restitched";
    }

    @Override
    public void onConstruction() {
        BlockFlatColored.getAllBlocks().forEach((block) -> {
            IStateVariantManager.getInstance().registerProvider(() -> block, new ShadeStateVariantProvider(block));
        });
    }
}
