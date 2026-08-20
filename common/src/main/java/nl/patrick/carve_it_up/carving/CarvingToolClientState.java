// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingToolClientState.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import nl.patrick.carve_it_up.item.CarvingToolItem;

import java.util.ArrayList;
import java.util.List;

import static nl.patrick.carve_it_up.carving.CarvingKeyBinds.CATEGORY_KEY;
import static nl.patrick.carve_it_up.carving.CarvingKeyBinds.SUBMENU_KEY;

public class CarvingToolClientState {

    // 0 = Mode, 1 = Pattern, 2 = Material
    public static int activeCategory = 0;

    public static int activeModeIndex = 0; // Cycles CarvingMode
    public static int activePatternIndex = 0; // Cycles CarvingPattern
    public static int activeMaterialIndex = 0; // Cycles available materials

    public static final int DEFAULT_MULTI_VOXEL_WIDTH = 3;

    /**
     * Checks if the player is holding the carving tool in their main hand.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isHoldingCarvingTool() {
        Minecraft minecraftInstance = Minecraft.getInstance();
        if (minecraftInstance.player == null) {
            return false;
        }
        ItemStack mainHandStack = minecraftInstance.player.getMainHandItem();
        if (mainHandStack.isEmpty()) {
            return false;
        }

        return mainHandStack.getItem() instanceof CarvingToolItem;
    }

    public static boolean isCategoryKeyPressed() {
        return CATEGORY_KEY.isDown();
    }

    public static boolean isSubmenuKeyPressed() {
        return SUBMENU_KEY.isDown();
    }

    /**
     * Retrieves all available blocks for carving:
     * 1. Blocks stored in the custom model (CarvedData.getBlocks()) of the looked-at block.
     * 2. The material attached to the held Carving Tool (if any).
     */
    public static List<Block> getAvailableMaterials() {
        Minecraft minecraftInstance = Minecraft.getInstance();
        List<Block> availableBlocksList = new ArrayList<>();

        // 1. Blocks present in targeted CarvedData or looked-at block
        if (minecraftInstance.level != null && minecraftInstance.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos targetBlockPos = blockHitResult.getBlockPos();
            CarvedData carvedData = CarvingManager.getCarvedData(minecraftInstance.level, targetBlockPos);
            if (carvedData != null) {
                for (Block block : carvedData.getBlocks()) {
                    if (block != null && block != Blocks.AIR && !availableBlocksList.contains(block)) {
                        availableBlocksList.add(block);
                    }
                }
                for (BlockState state : carvedData.getVoxelMaterials().values()) {
                    if (state != null && !state.isAir() && !availableBlocksList.contains(state.getBlock())) {
                        availableBlocksList.add(state.getBlock());
                    }
                }
            } else {
                Block lookedAtBlock = minecraftInstance.level.getBlockState(targetBlockPos).getBlock();
                if (lookedAtBlock != Blocks.AIR && !availableBlocksList.contains(lookedAtBlock)) {
                    availableBlocksList.add(lookedAtBlock);
                }
            }
        }

        // 2. Material loaded onto the held Carving Tool
        if (minecraftInstance.player != null) {
            ItemStack heldTool = minecraftInstance.player.getMainHandItem();
            if (heldTool.getItem() instanceof CarvingToolItem && CarvingToolItem.hasLoadedMaterial(heldTool)) {
                Block loadedBlock = CarvingToolItem.getLoadedMaterial(heldTool);
                if (loadedBlock != Blocks.AIR && !availableBlocksList.contains(loadedBlock)) {
                    availableBlocksList.add(loadedBlock);
                }
            }
        }

        return availableBlocksList;
    }

    /**
     * Retrieves a fallback block from the block the player is currently looking at.
     */
    public static Block getFallbackLookedAtBlock() {
        Minecraft minecraftInstance = Minecraft.getInstance();
        if (minecraftInstance.level != null && minecraftInstance.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos targetBlockPos = blockHitResult.getBlockPos();
            return minecraftInstance.level.getBlockState(targetBlockPos).getBlock();
        }
        return Blocks.AIR;
    }

    public static CarvingMode getSelectedMode() {
        CarvingMode[] carvingModes = CarvingMode.values();
        return carvingModes[Math.abs(activeModeIndex % carvingModes.length)];
    }

    public static CarvingPattern getSelectedPattern() {
        CarvingPattern[] carvingPatterns = CarvingPattern.values();
        return carvingPatterns[Math.abs(activePatternIndex % carvingPatterns.length)];
    }

    public static Block getSelectedMaterialBlock() {
        List<Block> availableMaterials = getAvailableMaterials();
        if (!availableMaterials.isEmpty()) {
            int selectedIndex = Math.abs(activeMaterialIndex % availableMaterials.size());
            return availableMaterials.get(selectedIndex);
        }
        return getFallbackLookedAtBlock();
    }

    /**
     * Dual-axis scrolling router triggered by mouse wheel events.
     * Returns true if input was consumed to cancel vanilla hotbar switching.
     */
    public static boolean handleScroll(double scrollAmount) {
        if (!isHoldingCarvingTool()) {
            return false;
        }

        boolean categoryPressed = isCategoryKeyPressed();
        boolean submenuPressed = isSubmenuKeyPressed();

        if (!categoryPressed && !submenuPressed) {
            return false;
        }

        int direction = scrollAmount > 0 ? -1 : 1;

        if (categoryPressed) {
            activeCategory = (activeCategory + direction + 3) % 3;
            displayStatusOverlay();
            return true;
        } else {
            direction = -direction; // invert for submenu
            if (activeCategory == 0) {
                int total = CarvingMode.values().length;
                activeModeIndex = (activeModeIndex + direction + total) % total;
            } else if (activeCategory == 1) {
                int total = CarvingPattern.values().length;
                activePatternIndex = (activePatternIndex + direction + total) % total;
            } else if (activeCategory == 2) {
                List<Block> availableMaterials = getAvailableMaterials();
                if (!availableMaterials.isEmpty()) {
                    int total = availableMaterials.size();
                    activeMaterialIndex = (activeMaterialIndex + direction + total) % total;
                } else {
                    activeMaterialIndex = 0;
                }
            }
            displayStatusOverlay();
            return true;
        }
    }

    /**
     * Displays a temporary overlay notification on the action bar with current selections.
     */
    public static void displayStatusOverlay() {
        Minecraft minecraftInstance = Minecraft.getInstance();
        if (minecraftInstance.gui == null) {
            return;
        }

        CarvingMode mode = getSelectedMode();
        CarvingPattern pattern = getSelectedPattern();
        Block material = getSelectedMaterialBlock();

        Component statusMessage = Component.literal("Mode: ")
            .append(Component.literal(mode.name()).withStyle(activeCategory == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD))
            .append(Component.literal(" | Pattern: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(pattern.name()).withStyle(activeCategory == 1 ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA))
            .append(Component.literal(" | Material: ").withStyle(ChatFormatting.GRAY))
            .append(Component.translatable(material.getDescriptionId()).withStyle(activeCategory == 2 ? ChatFormatting.GREEN : ChatFormatting.DARK_GREEN));

        minecraftInstance.gui.setOverlayMessage(statusMessage, false);
    }
}