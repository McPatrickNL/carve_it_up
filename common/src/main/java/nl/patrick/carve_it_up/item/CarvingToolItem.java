// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/CarvingToolItem.java
package nl.patrick.carve_it_up.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvingManager;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;
import nl.patrick.carve_it_up.services.Services;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import static nl.patrick.carve_it_up.carving.CarvingManager.debugWipeAllLoadedData;

/**
 * Item used to initialize blocks into carvable structures, configure carving modes,
 * attach/load material blocks from inventory, and perform carving actions in-world.
 */
public class CarvingToolItem extends Item {

    public static final String NBT_LOADED_MATERIAL_KEY = "LoadedMaterial";

    public CarvingToolItem(Properties properties) {
        super(properties);
    }

    // --- LOADED MATERIAL INVENTORY HELPERS ---

    public static boolean hasLoadedMaterial(ItemStack toolStack) {
        if (toolStack == null || toolStack.isEmpty()) {
            return false;
        }
        CustomData customData = toolStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return false;
        }
        return customData.copyTag().contains(NBT_LOADED_MATERIAL_KEY);
    }

    public static Block getLoadedMaterial(ItemStack toolStack) {
        if (!hasLoadedMaterial(toolStack)) {
            return Blocks.AIR;
        }
        CustomData customData = toolStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return Blocks.AIR;
        }
        int blockStateId = customData.copyTag().getIntOr(NBT_LOADED_MATERIAL_KEY, 0);
        BlockState blockState = Block.stateById(blockStateId);
        return blockState != null ? blockState.getBlock() : Blocks.AIR;
    }

    public static void setLoadedMaterial(ItemStack toolStack, Block block) {
        if (toolStack == null || toolStack.isEmpty() || block == null || block == Blocks.AIR) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, toolStack, tag -> {
            tag.putInt(NBT_LOADED_MATERIAL_KEY, Block.getId(block.defaultBlockState()));
        });
    }

    public static void clearLoadedMaterial(ItemStack toolStack) {
        if (toolStack == null || toolStack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, toolStack, tag -> {
            tag.remove(NBT_LOADED_MATERIAL_KEY);
        });
    }

    // --- BUNDLE-LIKE INVENTORY INTERACTIONS ---

    /**
     * Dragging a block item on the cursor and right-clicking the Carving Tool in an inventory slot:
     * Loads the block into the tool and consumes 1 item from the held cursor stack.
     */
    @Override
    public boolean overrideOtherStackedOnMe(
        ItemStack toolStack,
        ItemStack otherStack,
        Slot slot,
        ClickAction action,
        Player player,
        SlotAccess access
    ) {
        if (action == ClickAction.SECONDARY && !otherStack.isEmpty() && otherStack.getItem() instanceof BlockItem blockItem) {
            Block newBlock = blockItem.getBlock();
            if (newBlock != Blocks.AIR) {
                // If the tool already has a material loaded, return the previous material to the player
                if (hasLoadedMaterial(toolStack)) {
                    Block previousBlock = getLoadedMaterial(toolStack);
                    if (previousBlock != Blocks.AIR) {
                        ItemStack returnedStack = new ItemStack(previousBlock);
                        if (!player.getInventory().add(returnedStack)) {
                            player.drop(returnedStack, true);
                        }
                    }
                }

                setLoadedMaterial(toolStack, newBlock);
                otherStack.shrink(1);
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.2F);
                return true;
            }
        }
        return false;
    }

    /**
     * Holding the Carving Tool on the cursor and right-clicking on an inventory slot:
     * Unloads the attached block from the tool and places it into the clicked slot.
     */
    @Override
    public boolean overrideStackedOnOther(
        ItemStack toolStack,
        Slot slot,
        ClickAction action,
        Player player
    ) {
        if (action == ClickAction.SECONDARY && hasLoadedMaterial(toolStack)) {
            Block loadedBlock = getLoadedMaterial(toolStack);
            if (loadedBlock != Blocks.AIR) {
                ItemStack blockStack = new ItemStack(loadedBlock);
                if (slot.getItem().isEmpty()) {
                    slot.set(blockStack);
                    clearLoadedMaterial(toolStack);
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.2F);
                    return true;
                } else if (ItemStack.isSameItemSameComponents(slot.getItem(), blockStack) && slot.getItem().getCount() < slot.getItem().getMaxStackSize()) {
                    slot.getItem().grow(1);
                    clearLoadedMaterial(toolStack);
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.2F);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay display,
        Consumer<Component> tooltipConsumer,
        TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, display, tooltipConsumer, tooltipFlag);
        if (hasLoadedMaterial(stack)) {
            Block loadedBlock = getLoadedMaterial(stack);
            if (loadedBlock != Blocks.AIR) {
                tooltipConsumer.accept(Component.literal("Loaded Material: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(loadedBlock.getDescriptionId()).withStyle(ChatFormatting.GREEN)));
            }
        } else {
            tooltipConsumer.accept(Component.literal("Right-click with a block in inventory to load material").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // --- IN-WORLD RIGHT CLICK BEHAVIOR ---

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level worldLevel = context.getLevel();
        BlockPos targetBlockPos = context.getClickedPos();
        BlockState originalBlockState = worldLevel.getBlockState(targetBlockPos);

        // Prevent carving air, liquids, unbreakable blocks, plants/crops, or chests
        if (originalBlockState.isAir() || !originalBlockState.getFluidState().isEmpty() || originalBlockState.getDestroySpeed(worldLevel, targetBlockPos) < 0.0F
            || CarvingManager.isCarvingDisallowed(originalBlockState)) {
            return InteractionResult.FAIL;
        }

        // 0. CREATIVE + SNEAK + RIGHT CLICK = WIPE ALL DATA (TESTING)
        if (context.getPlayer() != null && context.getPlayer().isCrouching() && context.getPlayer().isCreative()) {
            debugWipeAllLoadedData();
            worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);
            return InteractionResult.SUCCESS;
        }

        // 1. SNEAK + RIGHT CLICK ON ALREADY CARVED BLOCK = REMOVE DATA / REVERT
        if (context.getPlayer() != null && context.getPlayer().isCrouching() && CarvingManager.isCarved(worldLevel, targetBlockPos)) {
            CarvingManager.removeCarvedData(worldLevel, targetBlockPos);
            worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);
            return InteractionResult.SUCCESS;
        }

        // 2. RIGHT CLICK ACTIONS (Replace Base, Copy, Paste)
        if (worldLevel.isClientSide()) {
            nl.patrick.carve_it_up.carving.RightClickMode rightClickMode = nl.patrick.carve_it_up.carving.CarvingToolClientState.getSelectedRightClickMode();

            if (rightClickMode == nl.patrick.carve_it_up.carving.RightClickMode.REPLACE_BASE) {
                if (CarvingManager.isCarved(worldLevel, targetBlockPos)) {
                    Block selectedMaterial = nl.patrick.carve_it_up.carving.CarvingToolClientState.getSelectedMaterialBlock();
                    if (selectedMaterial != null && selectedMaterial != Blocks.AIR) {
                        Services.NETWORK.sendToServer(new nl.patrick.carve_it_up.network.RequestChangeBaseMaterialPayload(targetBlockPos, selectedMaterial.defaultBlockState()));
                        return InteractionResult.SUCCESS;
                    }
                }
            } else if (rightClickMode == nl.patrick.carve_it_up.carving.RightClickMode.COPY) {
                if (CarvingManager.isCarved(worldLevel, targetBlockPos)) {
                    CarvedData cd = CarvingManager.getCarvedData(worldLevel, targetBlockPos);
                    if (cd != null) {
                        nl.patrick.carve_it_up.carving.CarvingToolClientState.setClipboard(cd.getResolution(), cd.getVoxelMaterials());
                        net.minecraft.client.Minecraft.getInstance().gui.setOverlayMessage(
                            Component.literal("Copied carving data to clipboard!").withStyle(ChatFormatting.GREEN),
                            false
                        );
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    net.minecraft.client.Minecraft.getInstance().gui.setOverlayMessage(
                        Component.literal("Cannot copy: Block is not carved!").withStyle(ChatFormatting.RED),
                        false
                    );
                    return InteractionResult.FAIL;
                }
            } else if (rightClickMode == nl.patrick.carve_it_up.carving.RightClickMode.PASTE) {
                if (!nl.patrick.carve_it_up.carving.CarvingToolClientState.hasClipboard()) {
                    net.minecraft.client.Minecraft.getInstance().gui.setOverlayMessage(
                        Component.literal("Clipboard is empty! Copy a carved block first.").withStyle(ChatFormatting.RED),
                        false
                    );
                    return InteractionResult.FAIL;
                } else {
                    var cb = nl.patrick.carve_it_up.carving.CarvingToolClientState.getClipboard();
                    Services.NETWORK.sendToServer(new nl.patrick.carve_it_up.network.RequestPasteCarvingDataPayload(
                        targetBlockPos,
                        cb.resolution(),
                        cb.voxelMaterials()
                    ));
                    net.minecraft.client.Minecraft.getInstance().gui.setOverlayMessage(
                        Component.literal("Pasted carving data!").withStyle(ChatFormatting.GREEN),
                        false
                    );
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 3. NORMAL RIGHT CLICK = ADD CARVED DATA CONTAINER (if not yet carved)
        if (!CarvingManager.isCarved(worldLevel, targetBlockPos)) {
            UUID ownerUuid = context.getPlayer() != null ? context.getPlayer().getUUID() : UUID.randomUUID();
            int gridResolution = 16;

            CarvedData freshCarvedData = new CarvedData(
                originalBlockState,
                ownerUuid,
                gridResolution
            );
            nl.patrick.carve_it_up.carving.CarvingModelFactory.populateFromShape(freshCarvedData, originalBlockState, worldLevel, targetBlockPos);

            CarvingManager.setCarvedData(worldLevel, targetBlockPos, freshCarvedData);
            worldLevel.sendBlockUpdated(targetBlockPos, originalBlockState, originalBlockState, 3);

            // Broadcast the freshly-created carve data to every client tracking this position
            SyncCarvedDataPayload syncPayload = new SyncCarvedDataPayload(
                targetBlockPos,
                freshCarvedData.getOriginalBlockState(),
                freshCarvedData.getOwnerUuid(),
                freshCarvedData.getResolution(),
                freshCarvedData.getVersion(),
                new HashMap<>(freshCarvedData.getVoxelMaterials())
            );
            Services.NETWORK.sendToTrackingClients(worldLevel, targetBlockPos, syncPayload);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}