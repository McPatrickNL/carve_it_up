package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/GuiMixin.java

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import nl.patrick.carve_it_up.carving.CarvingMode;
import nl.patrick.carve_it_up.carving.CarvingPattern;
import nl.patrick.carve_it_up.carving.CarvingToolClientState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;


@Mixin(Gui.class)
public abstract class GuiMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;
    
    @Shadow public abstract Font getFont();
    
    // todo maybe make this a map with helper methods to add textures and fill it later in another common class?
    @Unique
    private static final Identifier CARVE_IT_UP$SELECTION_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "hud/hotbar_selection");
    
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
        
        // --- SLOT 1: CARVING MODE ---
        int s1X = startX;
        boolean s1Active = (CarvingToolClientState.activeCategory == 0);
        carve_it_up$drawSlotBackground(graphics, s1X, startY, slotSize, s1Active);
        
        // Draw current Mode Icon
        CarvingMode activeMode = CarvingToolClientState.getSelectedMode();
        Identifier modeIconLoc = Identifier.fromNamespaceAndPath(
                "carve_it_up", "textures/gui/icons/mode_" + activeMode.name().toLowerCase() + ".png"
                                                                            );
        graphics.blit(RenderPipelines.GUI_TEXTURED, modeIconLoc, s1X + 3, startY + 3, 0.0f, 0.0f, 16, 16, 16, 16);
        
        // --- SLOT 2: CARVING SHAPE ---
        int s2X = startX + slotSize + spacing;
        boolean s2Active = (CarvingToolClientState.activeCategory == 1);
        carve_it_up$drawSlotBackground(graphics, s2X, startY, slotSize, s2Active);
        
        // Draw current Shape Icon
        CarvingPattern activePattern = CarvingToolClientState.getSelectedPattern();
        Identifier shapeIconLoc = Identifier.fromNamespaceAndPath(
                "carve_it_up", "textures/gui/icons/shape_" + activePattern.name().toLowerCase() + ".png"
                                                                             );
        graphics.blit(RenderPipelines.GUI_TEXTURED, shapeIconLoc, s2X + 3, startY + 3, 0.0f, 0.0f, 16, 16, 16, 16);
        
        // --- SLOT 3: ACTIVE BLOCK MATERIAL ---
        int s3X = s2X + slotSize + spacing;
        boolean s3Active = (CarvingToolClientState.activeCategory == 2);
        carve_it_up$drawSlotBackground(graphics, s3X, startY, slotSize, s3Active);
        
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
                graphics.text(font, questionComp, s3X + 11 - font.width(questionComp) / 2, startY + 7, 0xFFFFFFFF, false);
            }
        }
        
        // Draw actual vanilla selection frame sprite over the active category slot
        int activeSlotX = startX + (CarvingToolClientState.activeCategory * (slotSize + spacing));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, activeSlotX - 1, startY - 1, 24, 23);
        
        // --- SUBMENU RENDERING (EXTENDING UPWARDS - SHOWN ONLY ON KEY HELD) ---
        if (CarvingToolClientState.isSubmenuKeyPressed()) {
            
            // Category 0 Submenu (Mode options)
            if (s1Active) {
                CarvingMode[] modes = CarvingMode.values();
                for (int i = 0; i < modes.length; i++) {
                    int subY = startY - 24 - (i * 24);
                    boolean isCurrent = (CarvingToolClientState.activeModeIndex == i);
                    carve_it_up$drawSlotBackground(graphics, s1X, subY, slotSize, isCurrent);
                    
                    Identifier modeIcon = Identifier.fromNamespaceAndPath(
                            "carve_it_up", "textures/gui/icons/mode_" + modes[i].name().toLowerCase() + ".png"
                                                                                     );
                    graphics.blit(RenderPipelines.GUI_TEXTURED, modeIcon, s1X + 3, subY + 3, 0.0f, 0.0f, 16, 16, 16, 16);
                    
                    if (isCurrent) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s1X - 1, subY - 1, 24, 23);
                    }
                }
            }
            
            // Category 1 Submenu (Shape options)
            if (s2Active) {
                CarvingPattern[] patterns = CarvingPattern.values();
                for (int i = 0; i < patterns.length; i++) {
                    int subY = startY - 24 - (i * 24);
                    boolean isCurrent = (CarvingToolClientState.activePatternIndex == i);
                    carve_it_up$drawSlotBackground(graphics, s2X, subY, slotSize, isCurrent);
                    
                    Identifier shapeIcon = Identifier.fromNamespaceAndPath(
                            "carve_it_up", "textures/gui/icons/shape_" + patterns[i].name().toLowerCase() + ".png"
                                                                                      );
                    graphics.blit(RenderPipelines.GUI_TEXTURED, shapeIcon, s2X + 3, subY + 3, 0.0f, 0.0f, 16, 16, 16, 16);
                    
                    if (isCurrent) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s2X - 1, subY - 1, 24, 23);
                    }
                }
            }
            
            // Category 2 Submenu (Material blocks)
            if (s3Active) {
                if (blocks.isEmpty()) {
                    Block fallback = CarvingToolClientState.getFallbackLookedAtBlock();
                    if (fallback != null) {
                        int subY = startY - 24;
                        carve_it_up$drawSlotBackground(graphics, s3X, subY, slotSize, true);
                        ItemStack stack = new ItemStack(fallback);
                        if (!stack.isEmpty()) {
                            graphics.item(this.minecraft.player, stack, s3X + 3, subY + 3, 0);
                            graphics.itemDecorations(font, stack, s3X + 3, subY + 3);
                        }
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s3X - 1, subY - 1, 24, 23);
                    }
                } else {
                    for (int i = 0; i < blocks.size(); i++) {
                        int subY = startY - 24 - (i * 24);
                        boolean isCurrent = (CarvingToolClientState.activeMaterialIndex == i);
                        carve_it_up$drawSlotBackground(graphics, s3X, subY, slotSize, isCurrent);
                        
                        ItemStack stack = new ItemStack(blocks.get(i));
                        if (!stack.isEmpty()) {
                            graphics.item(this.minecraft.player, stack, s3X + 3, subY + 3, 0);
                            graphics.itemDecorations(font, stack, s3X + 3, subY + 3);
                        }
                        
                        if (isCurrent) {
                            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s3X - 1, subY - 1, 24, 23);
                        }
                    }
                }
            }
        }
    }
    
    @Unique
    private void carve_it_up$drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y, int size, boolean active) {
        // Procedurally draws a perfect recreation of the default hotbar slot texture.
        // Inactive: Translucent dark grey body. Active: Brighter grey body with highlighted borders.
        int bg = active ? 0xBF3C3C3C : 0x80111111;
        int border = active ? 0xFF6A6A6A : 0x803E3E3E;
        carve_it_up$drawBorderedRect(graphics, x, y, size, size, bg, border);
    }
    
    @Unique
    private void carve_it_up$drawBorderedRect(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int bgColor, int borderColor) {
        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.fill(x, y, x + w, y + 1, borderColor);       // Top border
        graphics.fill(x, y + h - 1, x + w, y + h, borderColor); // Bottom border
        graphics.fill(x, y, x + 1, y + h, borderColor);       // Left border
        graphics.fill(x + w - 1, y, x + w, y + h, borderColor); // Right border
    }
}
