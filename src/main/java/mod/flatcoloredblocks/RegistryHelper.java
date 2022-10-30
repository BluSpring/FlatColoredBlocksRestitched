package mod.flatcoloredblocks;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RegistryHelper {
    public static HashMap<RegistryItem, ResourceLocation> registryNames = new HashMap<>();

    public static Collection<Item> getItemsFromTag(ResourceLocation tagKey) {
        return getItemsFromTag(TagKey.create(Registry.ITEM_REGISTRY, tagKey));
    }

    public static Collection<Item> getItemsFromTag(TagKey<Item> tagKey) {
        try {
            return MappedRegistry.ITEM.getTag(tagKey).get().stream().map(a -> a.unwrap().right().get()).collect(Collectors.toList());
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
