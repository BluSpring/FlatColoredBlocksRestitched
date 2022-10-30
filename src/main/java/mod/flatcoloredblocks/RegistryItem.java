package mod.flatcoloredblocks;

import net.minecraft.resources.ResourceLocation;

public interface RegistryItem {
    default void setRegistryName(String namespace, String path) {
         setRegistryName(new ResourceLocation(namespace, path));
    }

    default void setRegistryName(ResourceLocation location) {
        RegistryHelper.registryNames.put(this, location);
    }

    default ResourceLocation getRegistryName() {
        return RegistryHelper.registryNames.get(this);
    }
}
