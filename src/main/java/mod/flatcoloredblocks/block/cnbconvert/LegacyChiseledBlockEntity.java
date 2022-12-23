package mod.flatcoloredblocks.block.cnbconvert;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.chiselsandbits.CB2BCConverter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class LegacyChiseledBlockEntity extends BlockEntity {
    public LegacyChiseledBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FlatColoredBlocks.LEGACY_CHISELED_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void load(CompoundTag nbt) {
        if (!FabricLoader.getInstance().isModLoaded("chiselsandbits")) {
            throw new IllegalStateException("Loaded LegacyChiseledBlockEntity without Chisels & Bits installed!");
        }

        var inflatedBuffer = CB2BCConverter.inflate(ByteBuffer.wrap(nbt.getByteArray("X")));
        var format = CB2BCConverter.loadCBLegacy(new FriendlyByteBuf(Unpooled.wrappedBuffer(inflatedBuffer)));

        var palette = new HashMap<Integer, IBlockInformation>();
        palette.put(0, IBlockInformation.AIR);

        var changes = new HashMap<BlockPos, IBlockInformation>();

        for (int i = 0; i < format.blocks.length; i++) {
            var stateId = format.blocks[i];

            if (!palette.containsKey(stateId)) {
                var blockId = format.getLegacyIdFromStateId(stateId);
                var metadata = format.getMetadataFromStateId(stateId);

                var legacyName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[blockId];

                if (legacyName == null) {
                    var dynamic = BlockStateData.getTag(((blockId & 255) << 4) | metadata);

                    if (!dynamic.get("Name").asString().result().get().equals("minecraft:air"))
                        palette.put(stateId, getBlockInformation(dynamic));
                } else {
                    var name = legacyName.contains("transparent0_") ?
                            "flatcoloredblocks:flatcoloredblock_transparent_127" :
                            legacyName.contains("glowing0_") ?
                                    "flatcoloredblocks:flatcoloredblock_glowing_255" :
                                    "flatcoloredblocks:flatcoloredblock";

                    var oldMetadataWorkaround = legacyName
                            .replace("flatcoloredblocks:flatcoloredblock", "")
                            .replace("_transparent0_", "")
                            .replace("_glowing0_", "");

                    var offset = Integer.parseInt(oldMetadataWorkaround) * 16;

                    palette.put(stateId, getBlockInformation(BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}")));
                }
            }

            var z = (i >> 8) & 15;
            var y = (i >> 4) & 15;
            var x = i & 15;

            var blockInformation = palette.get(stateId);

            if (blockInformation != null)
                changes.put(new BlockPos(x, y, z), blockInformation);
        }

        var blockState = ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.get(Material.STONE).get().defaultBlockState();
        var blockEntity = ModBlockEntityTypes.CHISELED.get().create(getBlockPos(), blockState);

        // Initialize the block entity with something that isn't air
        blockEntity.initializeWith(palette.values().stream().filter(a -> !a.isAir()).toList().get(0));

        changes.forEach((blockPos, blockInformation) -> {
            blockEntity.overrideInAreaTarget(blockInformation, new Vec3(blockPos.getX() / 16.0, blockPos.getY() / 16.0, blockPos.getZ() / 16.0));
        });

        this.getLevel().setBlock(getBlockPos(), blockState, 0);
        this.getLevel().setBlockEntity(blockEntity);
    }

    private IBlockInformation getBlockInformation(Dynamic<?> dynamic) {
        return new BlockInformation((CompoundTag) dynamic.getValue());
    }
}
