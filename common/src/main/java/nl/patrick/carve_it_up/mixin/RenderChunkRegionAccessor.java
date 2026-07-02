package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/RenderChunkRegionAccessor.java

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderChunkRegion.class)
public interface RenderChunkRegionAccessor
{
    @Accessor("level")
    Level carveitup$getLevel();
}
