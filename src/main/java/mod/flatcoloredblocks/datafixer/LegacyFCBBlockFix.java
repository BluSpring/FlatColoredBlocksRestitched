package mod.flatcoloredblocks.datafixer;

import java.util.HashMap;
import java.util.Map;

public class LegacyFCBBlockFix {
    public static Map<String, Integer> legacyFCBNameToOffset = new HashMap<>();

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
}
