// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ISerializableChunkDataAccessor.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.nbt.CompoundTag;

/**
 * Accessor interface attached to SerializableChunkData to hold serialized carved block NBT tags.
 */
public interface ISerializableChunkDataAccessor {

    CompoundTag carveitup$getCarvedDataTag();

    void carveitup$setCarvedDataTag(CompoundTag tag);
}
