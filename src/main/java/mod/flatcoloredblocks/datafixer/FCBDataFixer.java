package mod.flatcoloredblocks.datafixer;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class FCBDataFixer {
    public static void addFixers(DataFixerBuilder builder) {
        // As long as the schema version is above 1343 from 1.12, it's valid.
        /*Schema legacySchema = builder.addSchema(3000, LegacyFCBSchema::new); // FlatColoredBlocks version 1.12
        // 1.19.2 is 3120, so don't use >=3120 unless the mod is porting to a newer version than 1.19.2.

        builder.addFixer(
                new LegacyFCBBlockFix(
                        legacySchema,
                        "Legacy 1.12 Flat Colored Blocks to Flat Colored Blocks: Restitched format"
                )
        );*/
    }
}
