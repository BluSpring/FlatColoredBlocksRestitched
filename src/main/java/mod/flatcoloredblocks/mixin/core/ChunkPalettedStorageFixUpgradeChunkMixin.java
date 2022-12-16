package mod.flatcoloredblocks.mixin.core;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.datafixer.chiselsandbits.CB2BCConverter;
import mod.flatcoloredblocks.datafixer.chiselsandbits.ChiselsAndBitsData;
import mod.flatcoloredblocks.duck.ExtendedChunkPalettedStorageFixSection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.InflaterInputStream;

@Mixin(targets = "net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix$UpgradeChunk")
public class ChunkPalettedStorageFixUpgradeChunkMixin {
    @Shadow @Final private Int2ObjectMap<Dynamic<?>> blockEntities;

    @Shadow @Final private Dynamic<?> level;
    @Shadow @Final private int x;
    @Shadow @Final private int z;
    @Unique
    private final List<ChiselsAndBitsData> cbBlockEntities = new ArrayList<>();

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Dynamic;get(Ljava/lang/String;)Lcom/mojang/serialization/OptionalDynamic;", ordinal = 0, shift = At.Shift.BEFORE), method = "<init>")
    public void fcb$convertCB112ToCB119(Dynamic dynamic, CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("chiselsandbits"))
            return;

        this.level.get("TileEntities").asStreamOpt().result().ifPresent((stream) -> {
            stream.forEach((dynamic2) -> {
                if (!dynamic2.get("id").asString("minecraft:air").equals("minecraft:mod.chiselsandbits.tileentitychiseled"))
                    return;

                var blockEntity = dynamic2;

                blockEntity = blockEntity.set("id", blockEntity.createString("chiselsandbits:chiseled"));

                var bX = blockEntity.get("x").asInt(0);
                var bY = blockEntity.get("y").asInt(0);
                var bZ = blockEntity.get("z").asInt(0);

                int i = bX - this.x & 15;
                int j = bY;
                int k = bZ - this.z & 15;
                int l = j << 8 | k << 4 | i;

                cbBlockEntities.add(
                        new ChiselsAndBitsData(
                                new BlockPos(
                                        i, j, k
                                ),
                                blockEntity.get("lv").asInt(0)
                        )
                );

                this.blockEntities.put(l, blockEntity);
            });
        });
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Dynamic;get(Ljava/lang/String;)Lcom/mojang/serialization/OptionalDynamic;", ordinal = 0, shift = At.Shift.BEFORE), method = "<init>")
    public void fcb$convertCBToBC(Dynamic<?> dynamic, CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("bitsandchisels"))
            return;

        this.level.get("TileEntities").asStreamOpt().result().ifPresent((stream) -> {
            stream.forEach((dynamic2) -> {
                if (!dynamic2.get("id").asString("minecraft:air").equals("minecraft:mod.chiselsandbits.tileentitychiseled"))
                    return;

                var buffer = dynamic2.get("X").asByteBuffer();

                var inflater = new InflaterInputStream(new ByteArrayInputStream(buffer.array()));
                var inflatedBuffer = ByteBuffer.allocate(3145728);

                int usedBytes = 0;
                int rv = 0;

                do {
                    usedBytes += rv;
                    try {
                        rv = inflater.read(inflatedBuffer.array(), usedBytes, inflatedBuffer.limit() - usedBytes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } while (rv > 0);

                var format = CB2BCConverter.loadCBLegacy(new FriendlyByteBuf(Unpooled.wrappedBuffer(inflatedBuffer)));

                var palette = new ArrayList<Dynamic<?>>();
                var seen = new HashMap<Integer, Integer>();

                var blocks = new byte[8192];

                var blockEntity = dynamic2;

                for (int i = 0; i < format.blocks.length; i++) {
                    var stateId = format.blocks[i];
                    var blockId = format.getLegacyIdFromStateId(stateId);
                    var metadata = format.getMetadataFromStateId(stateId);

                    var legacyFcbName = FlatColoredBlocks.legacyForgeBlockParser.legacyIdsToString[blockId];

                    var z = (i >> 8) & 15;
                    var y = (i >> 4) & 15;
                    var x = i & 15;

                    var pos = ((x << 8) | (y << 4) | z) * 2;

                    if (seen.containsKey(stateId)) {
                        var index = seen.get(stateId).shortValue();

                        blocks[pos] = (byte) (index & 0xff);
                        blocks[pos + 1] = (byte) ((index >> 8) & 0xff);

                        continue;
                    }

                    if (legacyFcbName == null) { // Probably a Minecraft block
                        palette.add(BlockStateData.getTag(((blockId & 255) << 4) | metadata));
                    } else { // Very likely to be Flat Colored Blocks
                        var name = legacyFcbName.contains("transparent0_") ?
                                "flatcoloredblocks:flatcoloredblock_transparent_127" :
                                legacyFcbName.contains("glowing0_") ?
                                        "flatcoloredblocks:flatcoloredblock_glowing_255" :
                                        "flatcoloredblocks:flatcoloredblock";

                        var oldMetadataWorkaround = legacyFcbName
                                .replace("flatcoloredblocks:flatcoloredblock", "")
                                .replace("_transparent0_", "")
                                .replace("_glowing0_", "");

                        var offset = Integer.parseInt(oldMetadataWorkaround) * 16;

                        palette.add(BlockStateData.parse("{Name:'" + name + "',Properties:{shade:'" + (offset + metadata) + "'}}"));
                    }

                    seen.put(stateId, palette.size() - 1);
                    blocks[pos] = (byte) ((palette.size() - 1) & 0xff);
                    blocks[pos + 1] = (byte) (((palette.size() - 1) >> 8) & 0xff);
                }

                blockEntity = blockEntity.remove("X");
                blockEntity = blockEntity.remove("s");
                blockEntity = blockEntity.remove("nc");
                blockEntity = blockEntity.remove("b");

                blockEntity = blockEntity.set("id", blockEntity.createString("bitsandchisels:bits_block_entity"));
                blockEntity = blockEntity.set("keepPacked", blockEntity.createBoolean(false));
                blockEntity = blockEntity.set("palette", blockEntity.createList(palette.stream()));

                var byteBuffer = ByteBuffer.allocate(8192);
                byteBuffer.put(blocks);
                blockEntity = blockEntity.set("bits_v2", blockEntity.createByteList(byteBuffer));

                int i = blockEntity.get("x").asInt(0) - this.x & 15;
                int j = blockEntity.get("y").asInt(0);
                int k = blockEntity.get("z").asInt(0) - this.z & 15;
                int l = j << 8 | k << 4 | i;

                cbBlockEntities.add(
                        new ChiselsAndBitsData(
                                new BlockPos(
                                        i, j, k
                                ),
                                blockEntity.get("lv").asInt(0)
                        )
                );

                blockEntity = blockEntity.remove("lv");

                this.blockEntities.put(l, blockEntity);
            });
        });
    }

    // This is for the TileEntities forEach
    @Redirect(method = "method_15665", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;"))
    public Object fcb$preventLoadOfBCorCBBlockEntities(Int2ObjectMap<Dynamic<?>> instance, int i, Object o) {
        var id = ((Dynamic<?>) o).get("id").asString("minecraft:air");

        if (id.equals("minecraft:mod.chiselsandbits.tileentitychiseled"))
            return null;

        // This shouldn't happen, but I ain't gonna exclude the fact that it might.
        if (id.equals("bitsandchisels:bits_block_entity"))
            return null;

        if (id.equals("chiselsandbits:chiseled"))
            return null;

        return instance.put(i, (Dynamic<?>) o);
    }

    // This is for the Sections forEach
    @Inject(method = "method_15656", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/fixes/ChunkPalettedStorageFix$Section;upgrade(I)I", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void fcb$convertChiselsAndBits(Dynamic<?> dynamic, CallbackInfo ci, ChunkPalettedStorageFix.Section section) {
        if (!FabricLoader.getInstance().isModLoaded("bitsandchisels") && !FabricLoader.getInstance().isModLoaded("chiselsandbits"))
            return;

        var extended = ((ExtendedChunkPalettedStorageFixSection) section);
        extended.setChiselsAndBitsData(this.cbBlockEntities);
    }
}
