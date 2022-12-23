package mod.flatcoloredblocks.compat.chiselsandbits;

import mod.chiselsandbits.api.variant.state.IStateVariant;
import org.jetbrains.annotations.NotNull;

public record ShadeStateVariant(int shade) implements IStateVariant {
    @Override
    public int compareTo(@NotNull IStateVariant iStateVariant) {
        if (!(iStateVariant instanceof ShadeStateVariant))
            return -1;

        return shade - ((ShadeStateVariant) iStateVariant).shade;
    }

    @Override
    public IStateVariant createSnapshot() {
        return this;
    }
}
