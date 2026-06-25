package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingTracker.java

public class CarvingTracker {
    private static final Map<BlockPos, ResourceLocation> CARVED_BLOCKS = new HashMap<>();
    
    public static void carve(BlockPos pos, ResourceLocation customModelTemplate) {
        CARVED_BLOCKS.put(pos, customModelTemplate);
    }
    
    public static boolean isCarved(BlockPos pos) {
        return CARVED_BLOCKS.containsKey(pos);
    }
    
    public static ResourceLocation getCustomModel(BlockPos pos) {
        return CARVED_BLOCKS.get(pos);
    }
}
