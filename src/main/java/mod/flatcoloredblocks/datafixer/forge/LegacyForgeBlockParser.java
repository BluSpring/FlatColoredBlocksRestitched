package mod.flatcoloredblocks.datafixer.forge;

import mod.flatcoloredblocks.datafixer.LegacyFCBBlockFix;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class LegacyForgeBlockParser {
    private static final Logger logger = LoggerFactory.getLogger(LegacyForgeBlockParser.class);
    public String[] legacyIdsToString = new String[2048];

    public void parseForgeBlockIds(CompoundTag compoundTag) {
        var registries = compoundTag.getCompound("Registries");
        var blockRegistry = registries.getCompound("minecraft:blocks");

        var blockIds = blockRegistry.getList("ids", ListTag.TAG_COMPOUND);
        blockIds.forEach((element) -> {
            // this seems overcomplicated
            element.accept(new TagVisitor() {
                @Override
                public void visitCompound(CompoundTag compoundTag) {
                    var key = compoundTag.getString("K");
                    var value = compoundTag.getInt("V");

                    // Only add the FCB-related blocks
                    if (key.startsWith("flatcoloredblocks:"))
                        legacyIdsToString[value] = key;

                    // B&C support, need to convert legacy C&B format to B&C
                    if (
                            (
                                    FabricLoader.getInstance().isModLoaded("bitsandchisels")
                                    || FabricLoader.getInstance().isModLoaded("chiselsandbits")
                            ) && key.startsWith("chiselsandbits:")
                    ) {
                        legacyIdsToString[value] = key;
                    }
                }

                @Override
                public void visitString(StringTag stringTag) {
                }

                @Override
                public void visitByte(ByteTag byteTag) {
                }

                @Override
                public void visitShort(ShortTag shortTag) {
                }

                @Override
                public void visitInt(IntTag intTag) {
                }

                @Override
                public void visitLong(LongTag longTag) {
                }

                @Override
                public void visitFloat(FloatTag floatTag) {
                }

                @Override
                public void visitDouble(DoubleTag doubleTag) {
                }

                @Override
                public void visitByteArray(ByteArrayTag byteArrayTag) {
                }

                @Override
                public void visitIntArray(IntArrayTag intArrayTag) {
                }

                @Override
                public void visitLongArray(LongArrayTag longArrayTag) {
                }

                @Override
                public void visitList(ListTag listTag) {
                }

                @Override
                public void visitEnd(EndTag endTag) {
                }
            });
        });
    }
}
