// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ClientCarvingCache.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for client-side baked BlockStateModel instances keyed by BlockPos.
 */
public class ClientCarvingCache { // Converted from Allman style brace

    private static final Map<BlockPos, CacheEntry> MODEL_CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves the cached BlockStateModel or bakes a fresh one if cache is invalid or missing.
     *
     * @param blockPos The position of the carved block
     * @param serverVersion The current version of the carved data
     * @param definitions The carved data definition
     * @return The baked BlockStateModel
     */
    public static BlockStateModel getOrCompute(BlockPos blockPos, int serverVersion, CarvedData definitions) {
        CacheEntry entry = MODEL_CACHE.get(blockPos);

        // Cache hit: versions match, bypass rebuilding
        if (entry != null && entry.version() == serverVersion) {
            return entry.stateModel();
        }

        // Cache miss: Build modern 26.1 BlockStateModel
        BlockStateModel newlyBuiltModel = CarvingModelFactory.bakeCustomModel(definitions);

        MODEL_CACHE.put(blockPos.immutable(), new CacheEntry(serverVersion, newlyBuiltModel));
        return newlyBuiltModel;
    }

    /**
     * Invalidates the cached model for a given block position.
     *
     * @param blockPos The position to invalidate
     */
    public static void invalidate(BlockPos blockPos) {
        MODEL_CACHE.remove(blockPos);
    }

    private record CacheEntry(int version, BlockStateModel stateModel) {}
}
