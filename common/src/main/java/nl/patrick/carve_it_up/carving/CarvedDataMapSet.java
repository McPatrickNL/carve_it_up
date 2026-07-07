package nl.patrick.carve_it_up.carving;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvedDataMapSet.java

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


public class CarvedDataMapSet
{
    private final Level      level;
    private final CarvedData carvedData;
    
    public CarvedDataMapSet(Level level, CarvedData carvedData)
    {
        this.level      = level;
        this.carvedData = carvedData;
    }
    
    public Level getLevel()           {return this.level;}
    public CarvedData getCarvedData() {return this.carvedData;}
}
