// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/ChunkCarvedDataSerializer.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.UUID;

/**
 * Utility responsible for serializing and deserializing ChunkCarvedData containers
 * to and from Minecraft CompoundTag structures for world save persistence.
 */
public class ChunkCarvedDataSerializer {

    public static final String NBT_ROOT_KEY = "CarveItUpData";
    public static final String NBT_BLOCKS_KEY = "CarvedBlocks";
    public static final String NBT_POS_KEY = "Pos";
    public static final String NBT_ORIGINAL_STATE_KEY = "OriginalStateId";
    public static final String NBT_OWNER_KEY = "OwnerUuid";
    public static final String NBT_RESOLUTION_KEY = "Resolution";
    public static final String NBT_VERSION_KEY = "Version";
    public static final String NBT_VOXEL_KEYS_KEY = "VoxelKeys";
    public static final String NBT_VOXEL_VALUES_KEY = "VoxelValues";

    /**
     * Serializes all carved blocks in a ChunkCarvedData container into a CompoundTag.
     *
     * @param chunkCarvedData The data container to serialize
     * @return CompoundTag containing all serialized carved blocks, or empty tag if none
     */
    public static CompoundTag saveToTag(ChunkCarvedData chunkCarvedData) {
        CompoundTag rootCompoundTag = new CompoundTag();
        if (chunkCarvedData == null || !chunkCarvedData.hasCarvedData()) {
            return rootCompoundTag;
        }

        ListTag blocksListTag = new ListTag();
        for (Map.Entry<BlockPos, CarvedDataMapSet> entry : chunkCarvedData.getCarvedBlocks().entrySet()) {
            BlockPos blockPosition = entry.getKey();
            CarvedData carvedData = entry.getValue().getCarvedData();
            if (carvedData == null) {
                continue;
            }

            CompoundTag blockCompoundTag = new CompoundTag();
            blockCompoundTag.putLong(NBT_POS_KEY, blockPosition.asLong());
            blockCompoundTag.putInt(NBT_ORIGINAL_STATE_KEY, Block.getId(carvedData.getOriginalBlockState()));
            blockCompoundTag.putString(NBT_OWNER_KEY, carvedData.getOwnerUuid().toString());
            blockCompoundTag.putInt(NBT_RESOLUTION_KEY, carvedData.getResolution());
            blockCompoundTag.putInt(NBT_VERSION_KEY, carvedData.getVersion());

            Map<Integer, BlockState> voxelMaterialsMap = carvedData.getVoxelMaterials();
            int voxelCount = voxelMaterialsMap.size();
            int[] voxelIndexKeys = new int[voxelCount];
            int[] voxelBlockStateIds = new int[voxelCount];

            int arrayIndex = 0;
            for (Map.Entry<Integer, BlockState> voxelEntry : voxelMaterialsMap.entrySet()) {
                voxelIndexKeys[arrayIndex] = voxelEntry.getKey();
                voxelBlockStateIds[arrayIndex] = Block.getId(voxelEntry.getValue());
                arrayIndex++;
            }

            blockCompoundTag.putIntArray(NBT_VOXEL_KEYS_KEY, voxelIndexKeys);
            blockCompoundTag.putIntArray(NBT_VOXEL_VALUES_KEY, voxelBlockStateIds);

            blocksListTag.add(blockCompoundTag);
        }

        rootCompoundTag.put(NBT_BLOCKS_KEY, blocksListTag);
        return rootCompoundTag;
    }

    /**
     * Deserializes carved blocks from a CompoundTag into a ChunkCarvedData container.
     *
     * @param targetChunkCarvedData The destination container
     * @param worldLevel The world level context
     * @param rootCompoundTag The NBT tag to read from
     */
    public static void loadFromTag(ChunkCarvedData targetChunkCarvedData, Level worldLevel, CompoundTag rootCompoundTag) {
        if (targetChunkCarvedData == null || rootCompoundTag == null || !rootCompoundTag.contains(NBT_BLOCKS_KEY)) {
            return;
        }

        ListTag blocksListTag = rootCompoundTag.getListOrEmpty(NBT_BLOCKS_KEY);
        for (int listIndex = 0; listIndex < blocksListTag.size(); listIndex++) {
            CompoundTag blockCompoundTag = blocksListTag.getCompoundOrEmpty(listIndex);

            long packedBlockPos = blockCompoundTag.getLongOr(NBT_POS_KEY, 0L);
            BlockPos blockPosition = BlockPos.of(packedBlockPos);
            int originalStateId = blockCompoundTag.getIntOr(NBT_ORIGINAL_STATE_KEY, 0);
            BlockState originalBlockState = Block.stateById(originalStateId);
            if (originalBlockState == null || originalBlockState.isAir()) {
                originalBlockState = Blocks.OAK_LOG.defaultBlockState();
            }

            String ownerUuidString = blockCompoundTag.getStringOr(NBT_OWNER_KEY, "");
            UUID ownerUuid;
            try {
                ownerUuid = !ownerUuidString.isEmpty() ? UUID.fromString(ownerUuidString) : UUID.randomUUID();
            } catch (IllegalArgumentException illegalArgumentException) {
                ownerUuid = UUID.randomUUID();
            }

            int resolution = blockCompoundTag.getIntOr(NBT_RESOLUTION_KEY, 16);
            int version = blockCompoundTag.getIntOr(NBT_VERSION_KEY, 1);

            CarvedData loadedCarvedData = new CarvedData(
                originalBlockState,
                ownerUuid,
                resolution
            );

            loadedCarvedData.getVoxelMaterials().clear();
            int[] voxelIndexKeys = blockCompoundTag.getIntArray(NBT_VOXEL_KEYS_KEY).orElse(new int[0]);
            int[] voxelBlockStateIds = blockCompoundTag.getIntArray(NBT_VOXEL_VALUES_KEY).orElse(new int[0]);

            int minimumLength = Math.min(voxelIndexKeys.length, voxelBlockStateIds.length);
            for (int voxelIndex = 0; voxelIndex < minimumLength; voxelIndex++) {
                BlockState voxelState = Block.stateById(voxelBlockStateIds[voxelIndex]);
                if (voxelState != null && !voxelState.isAir()) {
                    loadedCarvedData.getVoxelMaterials().put(voxelIndexKeys[voxelIndex], voxelState);
                    loadedCarvedData.addBlock(voxelState.getBlock());
                }
            }

            while (loadedCarvedData.getVersion() < version) {
                loadedCarvedData.incrementVersion();
            }

            // Recompute collision, visual, and interaction shapes from loaded voxels
            VoxelShape computedShape = CarvingModelFactory.calculateCollisionShape(loadedCarvedData);
            loadedCarvedData.setCollisionShape(computedShape);
            loadedCarvedData.setVisualShape(computedShape);
            loadedCarvedData.setInteractionShape(computedShape);

            targetChunkCarvedData.addCarvedData(worldLevel, blockPosition, loadedCarvedData);
        }
    }
}
