package nl.patrick.carve_it_up.carving;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingToolClientState.java

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import nl.patrick.carve_it_up.CommonMod;
import nl.patrick.carve_it_up.item.CarvingToolItem;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

import static nl.patrick.carve_it_up.carving.CarvingKeyBinds.CATEGORY_KEY;
import static nl.patrick.carve_it_up.carving.CarvingKeyBinds.SUBMENU_KEY;


public class CarvingToolClientState
{
    // 0 = Mode, 1 = Shape, 2 = Material
    public static int activeCategory = 0;
    
    public static int activeModeIndex = 0;      // Cycles CarvingModelFactory.CarvingMode
    public static int activePatternIndex = 0;   // Cycles CarvingModelFactory.CarvingPattern
    public static int activeMaterialIndex = 0;  // Cycles targeted CarvedData materials
    
    /**
     * Checks if the player is holding the carving tool in their main hand.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isHoldingCarvingTool() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return false;
        
        return stack.getItem() instanceof CarvingToolItem;
    }
    
    public static boolean isCategoryKeyPressed() {
        return CATEGORY_KEY.isDown();
    }
    
    public static boolean isSubmenuKeyPressed() {
        return SUBMENU_KEY.isDown();
    }
    
    /**
     * Retrieves all blocks stored inside the currently targeted CarvedData.
     */
    public static List<Block> getTargetedBlocks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return Collections.emptyList();
        }
        BlockPos   pos  = ((BlockHitResult) mc.hitResult).getBlockPos();
        CarvedData data = CarvingManager.getCarvedData(mc.level, pos);
        if (data != null) {
            return data.getBlocks();
        }
        return Collections.emptyList();
    }
    
    /**
     * Retrieves a fallback block state from the block the player is currently looking at.
     */
    public static Block getFallbackLookedAtBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
            return mc.level.getBlockState(pos).getBlock();
        }
        return Blocks.STONE;
    }
    
    public static CarvingModelFactory.CarvingMode getSelectedMode() {
        CarvingModelFactory.CarvingMode[] modes = CarvingModelFactory.CarvingMode.values();
        return modes[Math.abs(activeModeIndex % modes.length)];
    }
    
    public static CarvingModelFactory.CarvingPattern getSelectedPattern() {
        CarvingModelFactory.CarvingPattern[] patterns = CarvingModelFactory.CarvingPattern.values();
        return patterns[Math.abs(activePatternIndex % patterns.length)];
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
            return true;
        } else {
            direction = -direction; // invert for sub menu
            if (activeCategory == 0) {
                int total = CarvingModelFactory.CarvingMode.values().length;
                activeModeIndex = (activeModeIndex + direction + total) % total;
            } else if (activeCategory == 1) {
                int total = CarvingModelFactory.CarvingPattern.values().length;
                activePatternIndex = (activePatternIndex + direction + total) % total;
            } else if (activeCategory == 2) {
                List<Block> targetedBlocks = getTargetedBlocks();
                if (!targetedBlocks.isEmpty()) {
                    int total = targetedBlocks.size();
                    activeMaterialIndex = (activeMaterialIndex + direction + total) % total;
                } else {
                    activeMaterialIndex = 0;
                }
            }
            return true;
        }
    }
}
