package nl.patrick.carve_it_up.carving;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingKeyBinds.java

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import nl.patrick.carve_it_up.CommonMod;
import org.lwjgl.glfw.GLFW;


public class CarvingKeyBinds
{
    public static final KeyMapping.Category CIU_CARVING_MENU = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, "key.categories.carve_it_up"));
    
    // Registered KeyMappings allowing customization in Minecraft's Controls menu
    public static final KeyMapping CATEGORY_KEY = new KeyMapping(
            "carve_it_up.key.category_key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CIU_CARVING_MENU
    );
    
    public static final KeyMapping SUBMENU_KEY = new KeyMapping(
            "carve_it_up.key.submenu_key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CIU_CARVING_MENU
    );
}
