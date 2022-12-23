package mod.flatcoloredblocks.compat.chiselsandbits;

import com.communi.suggestu.scena.core.fluid.FluidInformation;
import mod.chiselsandbits.api.plugin.ChiselsAndBitsPlugin;
import mod.chiselsandbits.api.variant.state.IStateVariant;
import mod.chiselsandbits.api.variant.state.IStateVariantProvider;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.ItemBlockFlatColored;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ShadeStateVariantProvider implements IStateVariantProvider {

    private final BlockFlatColored block;

    public ShadeStateVariantProvider(BlockFlatColored block) {
        this.block = block;
    }

    @Override
    public Optional<IStateVariant> getStateVariant(BlockState blockState, Optional<BlockEntity> optional) {
        if (blockState.getBlock() != block)
            return Optional.empty();

        return Optional.of(new ShadeStateVariant(((BlockFlatColored) blockState.getBlock()).getShadeNumber(blockState)));
    }

    @Override
    public Optional<IStateVariant> getStateVariant(FluidState fluidState) {
        return Optional.empty();
    }

    @Override
    public Optional<IStateVariant> getStateVariant(BlockState blockState, ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockFlatColored fcbItem) {
            if (fcbItem.getColoredBlock() != block)
                return Optional.empty();

            return Optional.of(new ShadeStateVariant(fcbItem.getColoredBlock().getstateForStack(itemStack).getValue(fcbItem.getColoredBlock().getShade())));
        }

        return Optional.empty();
    }

    @Override
    public Optional<IStateVariant> getStateVariant(FluidInformation fluidInformation) {
        return Optional.empty();
    }

    @Override
    public Collection<IStateVariant> getAllDefaultVariants(BlockState blockState) {
        final Collection<IStateVariant> defaultVariants = new ArrayList<>();
        for (int i = 0; i < block.getMaxShade(); i++) {
            defaultVariants.add(new ShadeStateVariant(i));
        }
        return defaultVariants;
    }

    @Override
    public CompoundTag serializeNBT(IStateVariant iStateVariant) {
        if (!(iStateVariant instanceof ShadeStateVariant shadeStateVariant))
            return new CompoundTag();

        final CompoundTag tag = new CompoundTag();
        tag.putInt("shade", shadeStateVariant.shade());
        return tag;
    }

    @Override
    public IStateVariant deserializeNBT(CompoundTag compoundTag) {
        if (!compoundTag.contains("color"))
            return new ShadeStateVariant(0);

        return new ShadeStateVariant(compoundTag.getInt("shade"));
    }

    @Override
    public void serializeInto(FriendlyByteBuf friendlyByteBuf, IStateVariant iStateVariant) {
        if (!(iStateVariant instanceof ShadeStateVariant shadeStateVariant)) {
            friendlyByteBuf.writeInt(0);
            return;
        }

        friendlyByteBuf.writeInt(shadeStateVariant.shade());
    }

    @Override
    public IStateVariant deserializeFrom(FriendlyByteBuf friendlyByteBuf) {
        return new ShadeStateVariant(friendlyByteBuf.readInt());
    }

    @Override
    public Optional<ItemStack> getItemStack(IStateVariant iStateVariant) {
        if (!(iStateVariant instanceof ShadeStateVariant shadeStateVariant))
            return Optional.empty();

        final ItemStack stack = new ItemStack(block);
        stack.getOrCreateTag().putInt( "Shade", shadeStateVariant.shade());

        return Optional.of(stack);
    }

    @Override
    public Optional<FluidInformation> getFluidInformation(IStateVariant iStateVariant, long l) {
        return Optional.empty();
    }

    @Override
    public Optional<Component> getName(IStateVariant iStateVariant) {
        if (!(iStateVariant instanceof ShadeStateVariant shadeStateVariant))
            return Optional.empty();

        return Optional.of(this.block.getName().append(" (shade " + shadeStateVariant.shade() + ")"));
    }
}