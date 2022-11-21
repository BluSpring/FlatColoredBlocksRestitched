package mod.flatcoloredblocks.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;

// This is basically a direct copy paste from Minecraft's BlockRenameFix class,
// with additional changes for fixing the block states.
public class LegacyFCBBlockFix extends DataFix {
    private final String name;

    public LegacyFCBBlockFix(Schema schema, String string) {
        super(schema, false);
        this.name = string;
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
        Type<Pair<String, String>> type2 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(type, type2)) {
            throw new IllegalStateException("block type is not what was expected.");
        } else {
            TypeRewriteRule typeRewriteRule = this.fixTypeEverywhere(this.name + " for block", type2, dynamicOps -> pair -> pair.mapSecond(this::fixBlock));
            TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped(
                    this.name + " for block_state",
                    this.getInputSchema().getType(References.BLOCK_STATE),
                    typed -> typed.update(DSL.remainderFinder(), dynamic -> {
                        Optional<String> optional = dynamic.get("Name").asString().result();

                        var ref = new Object() {
                            Dynamic<?> dynamic2 = dynamic;
                        };

                        optional.ifPresent(s -> {
                            ref.dynamic2 = ref.dynamic2.set("Name", dynamic.createString(this.fixBlock(s)));

                            var optional2 = ref.dynamic2.get("Properties").result();

                            optional2.ifPresent(d -> {
                                var shade = d.get("shade").asInt(0);

                                d.set("shade", d.createInt(this.fixBlockState(shade, optional.get())));
                                ref.dynamic2 = ref.dynamic2.set("Properties", d);
                            });
                        });

                        return ref.dynamic2;
                    })
            );
            return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2);
        }
    }

    public static Map<String, Integer> legacyFCBNameToOffset = new HashMap<>();

    protected String fixBlock(String string) {
        return string.startsWith("flatcoloredblocks:flatcoloredblock_transparent") ?
                "flatcoloredblocks:flatcoloredblock_transparent" :
                    string.startsWith("flatcoloredblocks:flatcoloredblock_glowing") ?
                            "flatcoloredblocks:flatcoloredblock_glowing" :
                            "flatcoloredblocks:flatcoloredblock";
    }

    public static Map<String, String> mapLegacyFCBNames() {
        var defaultMaxHues = 32;
        var defaultMaxSats = 4;
        var defaultMaxVals = 10;

        var defaultMaxShades = defaultMaxHues * defaultMaxSats * defaultMaxVals + defaultMaxVals;
        var metaScale = 16;

        int defaultMaxBlockShades = (defaultMaxShades + (metaScale - 1)) / metaScale;

        // why do I need this
        if (legacyFCBNameToOffset == null)
            legacyFCBNameToOffset = new HashMap<>();

        var map = new HashMap<String, String>();

        for (int x = 0; x < defaultMaxBlockShades; ++x) {
            int offset = x * metaScale;

            legacyFCBNameToOffset.put("flatcoloredblocks:flatcoloredblock" + x, offset);
            legacyFCBNameToOffset.put("flatcoloredblocks:flatcoloredblock_transparent0_" + x, offset);
            legacyFCBNameToOffset.put("flatcoloredblocks:flatcoloredblock_glowing0_" + x, offset);
        }

        return map;
    }

    protected int fixBlockState(int shade, String name) {
        return legacyFCBNameToOffset.get(name) + shade;
    }
}
