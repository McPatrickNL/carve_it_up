package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/LevelChunkMixin.java

import net.minecraft.world.level.chunk.LevelChunk;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin(LevelChunk.class)
public class LevelChunkMixin implements IChunkCarvedDataAccessor
{
    @Unique
    private final ChunkCarvedData carveitup$carvedData = new ChunkCarvedData();
    
    @Override
    public ChunkCarvedData carveitup$getCarvedData() {
        return this.carveitup$carvedData;
    }
}
