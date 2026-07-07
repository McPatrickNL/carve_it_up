package nl.patrick.carve_it_up.carving;

//import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import nl.patrick.carve_it_up.mixin.RenderChunkRegionAccessor;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingManager.java

public class CarvingManager
{
    /**
     * Light-weight, high-performance check to see if a coordinate has any carving data.
     * Perfect for rendering and physics hot-paths.
     */
    public static boolean isCarved(BlockGetter level, BlockPos pos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
        return chunkData != null && chunkData.hasCarvedData() && chunkData.isCarved(pos);
    }
    
    /**
     * Grabs the full structural CarvedData container for a position, if it exists.
     */
    public static CarvedData getCarvedData(BlockGetter level, BlockPos pos) {
        ChunkCarvedData chunkData = getChunkCarvedData(level, pos);
        if (chunkData != null && chunkData.hasCarvedData()) {
            return chunkData.getCarvedData(pos);
        }
        return null;
    }
    
    /**
     * Shared internal bridge to safely locate the ChunkCarvedData capsule across environments.
     */
    private static ChunkCarvedData getChunkCarvedData(BlockGetter level, BlockPos pos) {
        if (level instanceof Level world) {
            var chunk = world.getChunkAt(pos);
            return ((IChunkCarvedDataAccessor) chunk).carveitup$getCarvedData();
        }

// todo re-introduce this, along with the commented import.
//        if (level instanceof RenderChunkRegion renderRegion) {
//            Level world = ((RenderChunkRegionAccessor) renderRegion).carveitup$getLevel();
//            if (world != null) {
//                var chunk = world.getChunkAt(pos);
//                return ((IChunkCarvedDataAccessor) chunk).carveitup$getCarvedData();
//            }
//        }
        return null;
    }
}
