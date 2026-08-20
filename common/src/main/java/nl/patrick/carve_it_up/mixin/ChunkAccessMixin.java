// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ChunkAccessMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import nl.patrick.carve_it_up.carving.ChunkCarvedData;
import nl.patrick.carve_it_up.carving.IChunkCarvedDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin onto ChunkAccess to attach a ChunkCarvedData container to all chunks (ProtoChunk and LevelChunk).
 */
@Mixin(ChunkAccess.class)
public class ChunkAccessMixin implements IChunkCarvedDataAccessor {

    @Unique
    private final ChunkCarvedData carveitup$carvedData = new ChunkCarvedData();

    @Override
    public ChunkCarvedData carveitup$getCarvedData() {
        return this.carveitup$carvedData;
    }
}
