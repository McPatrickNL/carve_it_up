// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/GuiMixin.java
package nl.patrick.carve_it_up.mixin;

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
import static nl.patrick.carve_it_up.api.menu_textures.*;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract Font getFont();

    @Unique
    private static final Identifier CARVE_IT_UP$SELECTION_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "hud/hotbar_selection");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.player == null || this.minecraft.level == null) return;
        if (this.minecraft.options.hideGui) return;

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
        int totalWidth = (slotSize * 3) + (spacing * 2); // 70px total width

        // Position the 3-slot hotbar immediately to the left of the offhand slot
        int startX = offhandX - totalWidth - 8;
        int startY = screenHeight - 23; // Aligns perfectly with vanilla hotbar Y level

        // --- MAIN BACKGROUND PANEL ---
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MAIN_MENU.getIdentifier(), startX, startY, totalWidth, slotSize);

        int s1X = startX;
        int s2X = startX + slotSize + spacing;
        int s3X = s2X + slotSize + spacing;

        boolean s1Active = (CarvingToolClientState.activeCategory == 0);
        boolean s2Active = (CarvingToolClientState.activeCategory == 1);
        boolean s3Active = (CarvingToolClientState.activeCategory == 2);

        // --- SLOT 1: CARVING MODE ICON ---
        CarvingMode activeMode = CarvingToolClientState.getSelectedMode();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, activeMode.getIdentifier(), s1X + 3, startY + 3, 16, 16);

        // --- SLOT 2: CARVING SHAPE ICON ---
        CarvingPattern activePattern = CarvingToolClientState.getSelectedPattern();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, activePattern.getIdentifier(), s2X + 3, startY + 3, 16, 16);

        // --- SLOT 3: ACTIVE BLOCK MATERIAL ---
        List<Block> blocks = CarvingToolClientState.getAvailableMaterials();
        Block activeBlock = CarvingToolClientState.getSelectedMaterialBlock();

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

        // Draw the selection frame overlay over the active slot
        int activeSlotX = startX + (CarvingToolClientState.activeCategory * (slotSize + spacing));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, activeSlotX - 1, startY - 1, 24, 23);

        // --- SUBMENU RENDERING (COMPOSED VERTICALLY UPWARDS) ---
        if (CarvingToolClientState.isSubmenuKeyPressed()) {

            // Category 0 Submenu (Modes stack)
            if (s1Active) {
                CarvingMode[] modes = CarvingMode.values();
                int total = modes.length;
                for (int i = 0; i < total; i++) {
                    int subY = startY - 24 - (i * slotSize);
                    boolean isCurrent = (CarvingToolClientState.activeModeIndex == i);

                    Identifier bgTex = carve_it_up$getSubMenuTexture(i, total);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bgTex, s1X, subY, slotSize, slotSize);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, modes[i].getIdentifier(), s1X + 3, subY + 3, 16, 16);

                    if (isCurrent) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s1X - 1, subY - 1, 24, 23);
                    }
                }
            }

            // Category 1 Submenu (Shapes stack)
            if (s2Active) {
                CarvingPattern[] patterns = CarvingPattern.values();
                int total = patterns.length;
                for (int i = 0; i < total; i++) {
                    int subY = startY - 24 - (i * slotSize);
                    boolean isCurrent = (CarvingToolClientState.activePatternIndex == i);

                    Identifier bgTex = carve_it_up$getSubMenuTexture(i, total);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bgTex, s2X, subY, slotSize, slotSize);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, patterns[i].getIdentifier(), s2X + 3, subY + 3, 16, 16);

                    if (isCurrent) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s2X - 1, subY - 1, 24, 23);
                    }
                }
            }

            // Category 2 Submenu (Material stack)
            if (s3Active) {
                if (blocks.isEmpty()) {
                    Block fallback = CarvingToolClientState.getFallbackLookedAtBlock();
                    if (fallback != null) {
                        int subY = startY - 24;
                        Identifier bgTex = SUB_MENU_SINGLE.getIdentifier();
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bgTex, s3X, subY, slotSize, slotSize);

                        ItemStack stack = new ItemStack(fallback);
                        if (!stack.isEmpty()) {
                            graphics.item(this.minecraft.player, stack, s3X + 3, subY + 3, 0);
                            graphics.itemDecorations(font, stack, s3X + 3, subY + 3);
                        }
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CARVE_IT_UP$SELECTION_SPRITE, s3X - 1, subY - 1, 24, 23);
                    }
                } else {
                    int total = blocks.size();
                    for (int i = 0; i < total; i++) {
                        int subY = startY - 24 - (i * slotSize);
                        boolean isCurrent = (CarvingToolClientState.activeMaterialIndex == i);

                        Identifier bgTex = carve_it_up$getSubMenuTexture(i, total);
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bgTex, s3X, subY, slotSize, slotSize);

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

    /**
     * Determines which submenu slot slice to render based on position in the stack.
     */
    @Unique
    private Identifier carve_it_up$getSubMenuTexture(int index, int totalItems) {
        if (totalItems == 1) {
            return SUB_MENU_SINGLE.getIdentifier();
        }
        if (index == 0) {
            return SUB_MENU_BOTTOM.getIdentifier(); // Item closest to the hotbar panel
        }
        if (index == totalItems - 1) {
            return SUB_MENU_TOP.getIdentifier();    // Item sitting at the peak of the stack
        }
        return SUB_MENU_MIDDLE.getIdentifier();     // Sandwich slots with open borders
    }
}
