package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ChunkCarveData.java

// todo add a quick light-weight check to see if the chunk has any data at all, if not move on fast. Logic goes somewhere else I guess.
public class ChunkCarvedData
{
    // A sparse map: only entries for blocks that are actually carved.
//    private final Map<BlockPos, CarvedData> carvedBlocks = new HashMap<>();
    private final Map<BlockPos, CarvedDataMapSet> carvedBlocks = new HashMap<>();
    
    /*
     * Not directly using isEmpty() here because this call gets done many times and a stored Boolean is faster than
     * checking if the map is empty. Now it takes a little bit of extra time while performing the carving action, but
     * rendering, loading, checking for collision etc. is faster.
     */
    private boolean hasCarvedBlocks = false;
    
    public void addCarvedData(Level level, BlockPos pos, CarvedData data) {
        this.carvedBlocks.put(pos.immutable(), new CarvedDataMapSet(level, data));
        this.hasCarvedBlocks = true;
        LOGGER.info("Carved data added to " + pos.toShortString());
    }
    
    public void removeCarvedData(BlockPos pos) {
        this.carvedBlocks.remove(pos.immutable());
        if (carvedBlocks.isEmpty())
        {
            hasCarvedBlocks = false;
            LOGGER.info("Carved data removed from " + pos.toShortString());
        }
    }
    
    /**
     * Resets the entire container back to an empty state.
     */
    public void clear() {
        this.carvedBlocks.clear();
        this.hasCarvedBlocks = false;
        LOGGER.info("All carved data wiped");
    }
    
    public CarvedData getCarvedData(BlockPos pos) {
        if (!isPositionCarved(pos)){
            return null;
        }
        CarvedDataMapSet mapSet = this.carvedBlocks.get(pos);
        return mapSet != null ? mapSet.getCarvedData() : null;
    }
    
    public Map<BlockPos, CarvedDataMapSet> getCarvedBlocks(){
        return carvedBlocks;
    }
    
    public boolean isCarved(BlockPos pos) {
        return this.carvedBlocks.containsKey(pos);
    }
    
    public boolean hasCarvedData()
    {
        return hasCarvedBlocks;
    }
    
    public boolean isPositionCarved(BlockPos pos) {
        return this.hasCarvedBlocks && this.carvedBlocks.containsKey(pos);
    }
}
