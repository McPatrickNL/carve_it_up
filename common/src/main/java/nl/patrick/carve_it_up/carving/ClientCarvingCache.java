package nl.patrick.carve_it_up.carving;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.state.BlockState;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ClientCarvingCache.java

public class ClientCarvingCache
{
    private static final Map<BlockPos, CacheEntry> MODEL_CACHE = new ConcurrentHashMap<>();
    
    
    // Fetching the Compiled 26.1 BlockStateModel for a given state:
    // BlockStateModel vanillaModel = Minecraft.getInstance()
    //                                     .getModelManager()
    //                                     .getBlockStateModelSet()
    //                                     .get(blockState);
    
    public static BlockStateModel getOrCompute(BlockPos pos, int serverVersion, CarvedData definitions) {
        CacheEntry entry = MODEL_CACHE.get(pos);
        
        // Cache hit: versions match, bypass rebuilding
        if (entry != null && entry.version() == serverVersion) {
            return entry.stateModel();
        }
        
        // Cache miss: Build your modern 26.1 BlockStateModel here
        BlockStateModel newlyBuiltModel = CarvingModelFactory.bakeCustomModel(definitions);
        
        MODEL_CACHE.put(pos.immutable(), new CacheEntry(serverVersion, newlyBuiltModel));
        return newlyBuiltModel;
    }
    
    public static void invalidate(BlockPos pos) {
        MODEL_CACHE.remove(pos);
    }
    
    private record CacheEntry(int version, BlockStateModel stateModel) {}
}

