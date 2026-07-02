package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ChunkCarveData.java

// todo add a quick light-weight check to see if the chunk has any data at all, if not move on fast. Logic goes somewhere else I guess.
public class ChunkCarvedData
{
    // A sparse map: only entries for blocks that are actually carved.
    private final Map<BlockPos, CarvedData> carvedBlocks = new HashMap<>();
    
    /*
     * Not directly using isEmpty() here because this call gets done many times and a stored Boolean is faster than
     * checking if the map is empty. Now it takes a little bit of extra time while performing the carving action, but
     * rendering, loading, checking for collision etc. is faster.
     */
    private boolean hasCarvedBlocks = false;
    
    public void addCarvedData(BlockPos pos, CarvedData data) {
        this.carvedBlocks.put(pos.immutable(), data);
        this.hasCarvedBlocks = true;
    }
    
    public void removeCarvedData(BlockPos pos) {
        this.carvedBlocks.remove(pos.immutable());
        if (carvedBlocks.isEmpty())
        {
            hasCarvedBlocks = false;
        }
    }
    
    public CarvedData getCarvedData(BlockPos pos) {
        return this.carvedBlocks.get(pos);
    }
    
    public boolean isCarved(BlockPos pos) {
        return this.carvedBlocks.containsKey(pos);
    }
    
    public boolean hasCarvedData()
    {
        return hasCarvedBlocks;
    }
}
