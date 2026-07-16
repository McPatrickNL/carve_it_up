package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/GuiMixin.java

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import nl.patrick.carve_it_up.carving.CarvingModelFactory;
import nl.patrick.carve_it_up.carving.CarvingToolClientState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(Gui.class)
public abstract class GuiMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;
    
    @Shadow public abstract Font getFont();
    
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.player == null || this.minecraft.level == null) return;
        
        // Render our system overlays only when the carving tool is held in hand
        if (!CarvingToolClientState.isHoldingCarvingTool()) {
            return;
        }
        
        // Advance to a clean drawing layer
        graphics.nextStratum();
        
        Font font = this.getFont();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        
        // Dynamically compute the offhand position depending on player hand side
        int offhandX;
        if (this.minecraft.player.getMainArm() == HumanoidArm.RIGHT) {
            offhandX = screenWidth / 2 - 91 - 29;
        } else {
            offhandX = screenWidth / 2 + 91 + 7;
        }
        
        int slotSize = 22;
        int spacing = 2;
        int totalWidth = (slotSize * 3) + (spacing * 2);
        
        // Position the 3-slot hotbar immediately to the left of the offhand slot
        int startX = offhandX - totalWidth - 8;
        int startY = screenHeight - 23; // Aligns perfectly with vanilla hotbar Y level
        
        // --- SLOT 1: CARVING MODE (BLUE) ---
        int s1X = startX;
        boolean s1Active = (CarvingToolClientState.activeCategory == 0);
        int s1Bg = 0x80001530;
        int s1Border = s1Active ? 0xFF00A0FF : 0x800050A0;
        drawBorderedRect(graphics, s1X, startY, slotSize, slotSize, s1Bg, s1Border);
        
        String modeLabelStr = CarvingToolClientState.getSelectedMode().getName();
        Component modeLabel = Component.literal(modeLabelStr);
        graphics.text(font, modeLabel, s1X + 11 - font.width(modeLabel) / 2, startY + 7, 0xFFFFFF, false);
        
        // --- SLOT 2: CARVING SHAPE (GREEN) ---
        int s2X = startX + slotSize + spacing;
        boolean s2Active = (CarvingToolClientState.activeCategory == 1);
        int s2Bg = 0x80003015;
        int s2Border = s2Active ? 0xFF00FF50 : 0x8000A030;
        drawBorderedRect(graphics, s2X, startY, slotSize, slotSize, s2Bg, s2Border);
        
        String shapeLabelStr = CarvingToolClientState.getSelectedPattern().getName();
        Component shapeLabel = Component.literal(shapeLabelStr);
        graphics.text(font, shapeLabel, s2X + 11 - font.width(shapeLabel) / 2, startY + 7, 0xFFFFFF, false);
        
        // --- SLOT 3: ACTIVE BLOCK MATERIAL (PURPLE) ---
        int s3X = s2X + slotSize + spacing;
        boolean s3Active = (CarvingToolClientState.activeCategory == 2);
        int s3Bg = 0x80250035;
        int s3Border = s3Active ? 0xFFA000FF : 0x805000A0;
        drawBorderedRect(graphics, s3X, startY, slotSize, slotSize, s3Bg, s3Border);
        
        // Fetch blocks present in the targeted CarvedData
        List<Block> blocks = CarvingToolClientState.getTargetedBlocks();
        Block activeBlock;
        if (!blocks.isEmpty()) {
            int idx = Math.abs(CarvingToolClientState.activeMaterialIndex % blocks.size());
            activeBlock = blocks.get(idx);
        } else {
            activeBlock = CarvingToolClientState.getFallbackLookedAtBlock();
        }
        
        if (activeBlock != null) {
            ItemStack stack = new ItemStack(activeBlock);
            if (!stack.isEmpty()) {
                graphics.item(this.minecraft.player, stack, s3X + 3, startY + 3, 0);
                graphics.itemDecorations(font, stack, s3X + 3, startY + 3);
            } else {
                Component questionComp = Component.literal("?");
                graphics.text(font, questionComp, s3X + 11 - font.width(questionComp) / 2, startY + 7, 0xFFFFFF, false);
            }
        }
        
        // Draw vanilla-like white selection frame over the active category slot
        int activeSlotX = startX + (CarvingToolClientState.activeCategory * (slotSize + spacing));
        drawBorderedRect(graphics, activeSlotX - 1, startY - 1, slotSize + 2, slotSize + 2, 0x00000000, 0xFFFFFFFF);
        
        // --- SUBMENU RENDERING (EXTENDING UPWARDS ON SCROLL HIGHLIGHT) ---
        
        // Category 0 Submenu (Mode options: REMOVE, ADD, REPLACE)
        if (s1Active) {
            CarvingModelFactory.CarvingMode[] modes = CarvingModelFactory.CarvingMode.values();
            int subW = 56;
            int subH = 18;
            int subX = s1X - (subW - slotSize) / 2;
            for (int i = 0; i < modes.length; i++) {
                int subY = startY - 22 - (i * 20);
                boolean isCurrent = (CarvingToolClientState.activeModeIndex == i);
                int bg = isCurrent ? 0xBF003060 : 0x80001530;
                int border = isCurrent ? 0xFF00A0FF : 0x500050A0;
                drawBorderedRect(graphics, subX, subY, subW, subH, bg, border);
                
                Component textComp = Component.literal(modes[i].name());
                graphics.text(font, textComp, subX + subW / 2 - font.width(textComp) / 2, subY + 5, isCurrent ? 0xFFFFFF : 0xAAAAAA, false);
            }
        }
        
        // Category 1 Submenu (Shape options: VOXEL, MULTI_VOXEL, LINE, FACE)
        if (s2Active) {
            CarvingModelFactory.CarvingPattern[] patterns = CarvingModelFactory.CarvingPattern.values();
            int subW = 76;
            int subH = 18;
            int subX = s2X - (subW - slotSize) / 2;
            for (int i = 0; i < patterns.length; i++) {
                int subY = startY - 22 - (i * 20);
                boolean isCurrent = (CarvingToolClientState.activePatternIndex == i);
                int bg = isCurrent ? 0xBF006030 : 0x80003015;
                int border = isCurrent ? 0xFF00FF50 : 0x5000A030;
                drawBorderedRect(graphics, subX, subY, subW, subH, bg, border);
                
                Component textComp = Component.literal(patterns[i].name());
                graphics.text(font, textComp, subX + subW / 2 - font.width(textComp) / 2, subY + 5, isCurrent ? 0xFFFFFF : 0xAAAAAA, false);
            }
        }
        
        // Category 2 Submenu (Material blocks cycled dynamically)
        if (s3Active) {
            if (blocks.isEmpty()) {
                Block fallback = CarvingToolClientState.getFallbackLookedAtBlock();
                if (fallback != null) {
                    int subY = startY - 24;
                    int bg = 0xBF500080;
                    int border = 0xFFA000FF;
                    drawBorderedRect(graphics, s3X, subY, slotSize, slotSize, bg, border);
                    ItemStack stack = new ItemStack(fallback);
                    if (!stack.isEmpty()) {
                        graphics.item(this.minecraft.player, stack, s3X + 3, subY + 3, 0);
                        graphics.itemDecorations(font, stack, s3X + 3, subY + 3);
                    }
                }
            } else {
                for (int i = 0; i < blocks.size(); i++) {
                    int subY = startY - 24 - (i * 24);
                    boolean isCurrent = (CarvingToolClientState.activeMaterialIndex == i);
                    int bg = isCurrent ? 0xBF500080 : 0x80250035;
                    int border = isCurrent ? 0xFFA000FF : 0x505000A0;
                    drawBorderedRect(graphics, s3X, subY, slotSize, slotSize, bg, border);
                    
                    ItemStack stack = new ItemStack(blocks.get(i));
                    if (!stack.isEmpty()) {
                        graphics.item(this.minecraft.player, stack, s3X + 3, subY + 3, 0);
                        graphics.itemDecorations(font, stack, s3X + 3, subY + 3);
                    }
                }
            }
        }
    }
    
    private void drawBorderedRect(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int bgColor, int borderColor) {
        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.fill(x, y, x + w, y + 1, borderColor);       // Top border
        graphics.fill(x, y + h - 1, x + w, y + h, borderColor); // Bottom border
        graphics.fill(x, y, x + 1, y + h, borderColor);       // Left border
        graphics.fill(x + w - 1, y, x + w, y + h, borderColor); // Right border
    }
    
//    private String getShortModeName(CarvingModelFactory.CarvingMode mode) {
//        if (mode == null) return "?";
//        return switch (mode) {
//            case REMOVE -> "REM";
//            case ADD -> "ADD";
//            case REPLACE -> "REP";
//        };
//    }
//
//    private String getShortShapeName(CarvingModelFactory.CarvingPattern pattern) {
//        if (pattern == null) return "?";
//        return switch (pattern) {
//            case VOXEL -> "VOX";
//            case MULTI_VOXEL -> "MUL";
//            case LINE -> "LIN";
//            case FACE -> "FAC";
//        };
//    }
}
