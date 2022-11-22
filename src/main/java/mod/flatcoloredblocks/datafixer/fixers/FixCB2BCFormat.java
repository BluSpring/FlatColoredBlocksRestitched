package mod.flatcoloredblocks.datafixer.fixers;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class FixCB2BCFormat extends DataFix {
    public FixCB2BCFormat(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return null;
    }
}
