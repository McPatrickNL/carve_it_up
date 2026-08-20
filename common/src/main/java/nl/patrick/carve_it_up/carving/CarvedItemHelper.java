// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvedItemHelper.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;
import nl.patrick.carve_it_up.services.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Helper class for attaching carved voxel data to ItemStacks on block break,
 * and restoring carved blocks when placing carved items in the world.
 */
public class CarvedItemHelper {

    public static final String NBT_ITEM_CARVED_ROOT = "CarveItUpData";

    /**
     * Creates an ItemStack representing the broken carved block with all voxel data attached.
     *
     * @param carvedData The carved data structure from the broken block
     * @return ItemStack of the original block with CustomData and "Carved" item name prefix
     */
    public static ItemStack createCarvedBlockDrop(CarvedData carvedData) {
        if (carvedData == null) {
            return ItemStack.EMPTY;
        }

        Block originalBlock = carvedData.getOriginalBlockState().getBlock();
        ItemStack dropStack = new ItemStack(originalBlock);

        CompoundTag rootItemTag = new CompoundTag();
        CompoundTag blockDataTag = new CompoundTag();

        blockDataTag.putInt(ChunkCarvedDataSerializer.NBT_ORIGINAL_STATE_KEY, Block.getId(carvedData.getOriginalBlockState()));
        blockDataTag.putString(ChunkCarvedDataSerializer.NBT_OWNER_KEY, carvedData.getOwnerUuid().toString());
        blockDataTag.putInt(ChunkCarvedDataSerializer.NBT_RESOLUTION_KEY, carvedData.getResolution());
        blockDataTag.putInt(ChunkCarvedDataSerializer.NBT_VERSION_KEY, carvedData.getVersion());

        Map<Integer, BlockState> voxelMaterials = carvedData.getVoxelMaterials();
        int voxelCount = voxelMaterials.size();
        int[] keysArray = new int[voxelCount];
        int[] valuesArray = new int[voxelCount];

        int arrayIndex = 0;
        for (Map.Entry<Integer, BlockState> entry : voxelMaterials.entrySet()) {
            keysArray[arrayIndex] = entry.getKey();
            valuesArray[arrayIndex] = Block.getId(entry.getValue());
            arrayIndex++;
        }

        blockDataTag.putIntArray(ChunkCarvedDataSerializer.NBT_VOXEL_KEYS_KEY, keysArray);
        blockDataTag.putIntArray(ChunkCarvedDataSerializer.NBT_VOXEL_VALUES_KEY, valuesArray);

        rootItemTag.put(NBT_ITEM_CARVED_ROOT, blockDataTag);
        dropStack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootItemTag));

        // Set custom name prefix: "Carved <Original Name>"
        Component originalHoverName = dropStack.getHoverName();
        dropStack.set(DataComponents.ITEM_NAME, Component.literal("Carved ").append(originalHoverName));

        return dropStack;
    }

    /**
     * Checks if an ItemStack has carved block data attached.
     *
     * @param stack The item stack to check
     * @return True if the stack contains carved voxel data
     */
    public static boolean hasCarvedData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return false;
        }
        CompoundTag itemTag = customData.copyTag();
        return itemTag.contains(NBT_ITEM_CARVED_ROOT);
    }

    /**
     * Restores carved voxel data from an ItemStack onto a newly placed block in the world.
     *
     * @param worldLevel The world level where the block was placed
     * @param blockPos The position where the block was placed
     * @param stack The item stack used for placement
     * @return True if carved data was successfully restored
     */
    public static boolean applyCarvedDataFromItem(Level worldLevel, BlockPos blockPos, ItemStack stack) {
        if (!hasCarvedData(stack)) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag rootTag = customData.copyTag();
        CompoundTag blockDataTag = rootTag.getCompoundOrEmpty(NBT_ITEM_CARVED_ROOT);
        if (blockDataTag.isEmpty()) {
            return false;
        }

        int originalStateId = blockDataTag.getIntOr(ChunkCarvedDataSerializer.NBT_ORIGINAL_STATE_KEY, 0);
        BlockState originalBlockState = Block.stateById(originalStateId);
        if (originalBlockState == null || originalBlockState.isAir()) {
            originalBlockState = worldLevel.getBlockState(blockPos);
        }

        String ownerUuidString = blockDataTag.getStringOr(ChunkCarvedDataSerializer.NBT_OWNER_KEY, "");
        UUID ownerUuid;
        try {
            ownerUuid = !ownerUuidString.isEmpty() ? UUID.fromString(ownerUuidString) : UUID.randomUUID();
        } catch (IllegalArgumentException e) {
            ownerUuid = UUID.randomUUID();
        }

        int resolution = blockDataTag.getIntOr(ChunkCarvedDataSerializer.NBT_RESOLUTION_KEY, 16);
        int version = blockDataTag.getIntOr(ChunkCarvedDataSerializer.NBT_VERSION_KEY, 1);

        CarvedData restoredCarvedData = new CarvedData(
            originalBlockState,
            worldLevel,
            blockPos,
            CollisionContext.empty(),
            ownerUuid,
            resolution
        );

        restoredCarvedData.getVoxelMaterials().clear();
        int[] voxelKeys = blockDataTag.getIntArray(ChunkCarvedDataSerializer.NBT_VOXEL_KEYS_KEY).orElse(new int[0]);
        int[] voxelValues = blockDataTag.getIntArray(ChunkCarvedDataSerializer.NBT_VOXEL_VALUES_KEY).orElse(new int[0]);

        int minimumLength = Math.min(voxelKeys.length, voxelValues.length);
        for (int i = 0; i < minimumLength; i++) {
            BlockState voxelState = Block.stateById(voxelValues[i]);
            if (voxelState != null && !voxelState.isAir()) {
                restoredCarvedData.getVoxelMaterials().put(voxelKeys[i], voxelState);
                restoredCarvedData.addBlock(voxelState.getBlock());
            }
        }

        while (restoredCarvedData.getVersion() < version) {
            restoredCarvedData.incrementVersion();
        }

        VoxelShape shape = CarvingModelFactory.calculateCollisionShape(restoredCarvedData);
        restoredCarvedData.setCollisionShape(shape);
        restoredCarvedData.setVisualShape(shape);
        restoredCarvedData.setInteractionShape(shape);

        CarvingManager.addCarvedData(worldLevel, blockPos, restoredCarvedData);

        if (worldLevel.isClientSide()) {
            // Invalidate client model cache and force chunk section dirty update
            ClientCarvingCache.invalidate(blockPos);
            try {
                Minecraft minecraftInstance = Minecraft.getInstance();
                if (minecraftInstance.levelRenderer != null) {
                    int sectionCoordinateX = SectionPos.blockToSectionCoord(blockPos.getX());
                    int sectionCoordinateY = SectionPos.blockToSectionCoord(blockPos.getY());
                    int sectionCoordinateZ = SectionPos.blockToSectionCoord(blockPos.getZ());
                    minecraftInstance.levelRenderer.setSectionDirtyWithNeighbors(sectionCoordinateX, sectionCoordinateY, sectionCoordinateZ);
                }
            } catch (Throwable ignored) {
                // Safeguard for non-client / server calls
            }
        } else {
            SyncCarvedDataPayload syncPayload = new SyncCarvedDataPayload(
                blockPos,
                originalBlockState,
                ownerUuid,
                resolution,
                version,
                new HashMap<>(restoredCarvedData.getVoxelMaterials())
            );
            Services.NETWORK.sendToTrackingClients(worldLevel, blockPos, syncPayload);
        }

        return true;
    }
}
